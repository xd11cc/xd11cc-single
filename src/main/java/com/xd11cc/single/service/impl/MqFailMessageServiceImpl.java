package com.xd11cc.single.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.enums.MqFailStatus;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.common.mq.MqMessageSender;
import com.xd11cc.single.common.mq.enums.MqQueueEnum;
import com.xd11cc.single.entity.domain.MqFailMessageDO;
import com.xd11cc.single.entity.vo.MqFailMessageQueryVO;
import com.xd11cc.single.mapper.MqFailMessageMapper;
import com.xd11cc.single.service.IMqFailMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * MQ 失败消息服务实现。
 *
 * @author xd11cc
 */
@Slf4j
@Service
public class MqFailMessageServiceImpl extends ServiceImpl<MqFailMessageMapper, MqFailMessageDO>
        implements IMqFailMessageService {

    /**
     * 状态：待处理
     */
    private static final String STATUS_PENDING = MqFailStatus.PENDING.getCode();

    /**
     * 状态：已重放
     */
    private static final String STATUS_REPLAYED = MqFailStatus.REPLAYED.getCode();

    /**
     * 状态：已忽略
     */
    private static final String STATUS_IGNORED = MqFailStatus.IGNORED.getCode();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Jackson 类型标识消息头，消费端据此确定反序列化目标类型
     */
    private static final String TYPE_ID_HEADER = "__TypeId__";

    /**
     * 消息信封字段名，与 {@code MqMessage} 保持一致
     */
    private static final String MESSAGE_ID_FIELD = "messageId";

    private static final String TENANT_ID_FIELD = "tenantId";

    @Override
    public List<MqFailMessageDO> getList(MqFailMessageQueryVO queryVO) {
        LambdaQueryWrapper<MqFailMessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryVO.getMessageId()),
                        MqFailMessageDO::getMessageId, queryVO.getMessageId())
                .eq(StringUtils.isNotBlank(queryVO.getQueueName()),
                        MqFailMessageDO::getQueueName, queryVO.getQueueName())
                .eq(queryVO.getStatus() != null, MqFailMessageDO::getStatus, queryVO.getStatus())
                .orderByDesc(MqFailMessageDO::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replay(Long id) {
        MqFailMessageDO record = loadPending(id);
        publish(record, record.getData());
        markReplayed(id);
        log.info("[MQ] 重放失败消息 id={}, messageId={}, queue={}",
                id, record.getMessageId(), record.getQueueName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replayWithEdit(Long id, String data) {
        MqFailMessageDO record = loadPending(id);

        // 只允许改消息体：校验非空且是合法 JSON，避免把非法内容投递给消费者
        String edited = data == null ? "" : data.trim();
        if (edited.isEmpty()) {
            throw new ServiceException(SystemErrorEnum.MQ_FAIL_DATA_EMPTY);
        }
        if (!JSON.isValid(edited)) {
            throw new ServiceException(SystemErrorEnum.MQ_FAIL_DATA_INVALID);
        }

        // 覆写原记录的消息体，保留修改后的内容供后续排查
        MqFailMessageDO update = new MqFailMessageDO();
        update.setId(id);
        update.setData(edited);
        update.setStatus(STATUS_REPLAYED);
        baseMapper.updateById(update);

        publish(record, edited);
        log.info("[MQ] 编辑后重放失败消息 id={}, messageId={}, queue={}",
                id, record.getMessageId(), record.getQueueName());
    }

    /**
     * 加载待处理的失败记录，并校验队列已登记。
     */
    private MqFailMessageDO loadPending(Long id) {
        MqFailMessageDO record = baseMapper.selectById(id);
        if (record == null) {
            throw new ServiceException(SystemErrorEnum.MQ_FAIL_RECORD_NOT_FOUND);
        }
        // 注意：status 已是 String，必须用 equals 比较。
        // 若写成 != 会退化为引用比较、条件恒真，导致所有记录都无法重投。
        if (!Objects.equals(record.getStatus(), STATUS_PENDING)) {
            throw new ServiceException(SystemErrorEnum.MQ_FAIL_REPLAY_NOT_ALLOWED);
        }
        return record;
    }

    /**
     * 以记录中的租户身份把消息体投递回原队列。
     * <p>
     * 注意：body 是 JSON 字符串，若直接发送，Jackson 转换器会把 {@code __TypeId__}
     * 写成 {@code java.lang.String}，消费端 {@code @RabbitHandler} 按类型分派时
     * 找不到方法。因此落表时保存了原始 {@code __TypeId__}，这里用它还原。
     * </p>
     *
     * @param record 失败记录（提供队列、租户、类型、消息ID）
     * @param body   实际投递的消息体
     */
    private void publish(MqFailMessageDO record, String body) {
        MqQueueEnum queue = Arrays.stream(MqQueueEnum.values())
                .filter(q -> q.getQueue().equals(record.getQueueName()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(SystemErrorEnum.MQ_QUEUE_UNDEFINED.getErrorCode(),
                        "队列未定义，无法重放: " + record.getQueueName()));

        final String messageId = record.getMessageId();
        final Long tenantId = record.getTenantId();
        final String typeId = record.getMessageType();

        // body 是 JSON 文本，直接以 String 发送会被 Jackson 二次序列化成 JSON 字符串字面量
        // （消费端报 "no String-argument constructor"）。这里解析成对象再发送。
        // 同时补齐信封字段：人工编辑的消息体往往缺少 messageId / tenantId，
        // 而消费端需要它们做幂等与租户上下文，不补会导致幂等失效。
        JSONObject payload = JSON.parseObject(body);
        payload.putIfAbsent(MESSAGE_ID_FIELD, messageId);
        payload.putIfAbsent(TENANT_ID_FIELD, tenantId);

        TenantContextHolder.runAsTenant(tenantId, () -> rabbitTemplate.convertAndSend(
                queue.getExchange(), queue.getRoutingKey(), payload, msg -> {
                    MessageProperties props = msg.getMessageProperties();
                    props.setMessageId(messageId);
                    props.setHeader(MqMessageSender.HEADER_TENANT_ID, tenantId);
                    props.setHeader(MqMessageSender.HEADER_MESSAGE_ID, messageId);
                    if (typeId != null && !typeId.isEmpty()) {
                        props.setHeader(TYPE_ID_HEADER, typeId);
                    }
                    props.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
                    props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    return msg;
                }));
    }

    private void markReplayed(Long id) {
        MqFailMessageDO update = new MqFailMessageDO();
        update.setId(id);
        update.setStatus(STATUS_REPLAYED);
        baseMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignore(Long id) {
        MqFailMessageDO record = baseMapper.selectById(id);
        if (record == null) {
            throw new ServiceException(SystemErrorEnum.MQ_FAIL_RECORD_NOT_FOUND);
        }
        MqFailMessageDO update = new MqFailMessageDO();
        update.setId(id);
        update.setStatus(STATUS_IGNORED);
        baseMapper.updateById(update);
    }
}
