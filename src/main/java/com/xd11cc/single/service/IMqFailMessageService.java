package com.xd11cc.single.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xd11cc.single.entity.domain.MqFailMessageDO;
import com.xd11cc.single.entity.vo.MqFailMessageQueryVO;

import java.util.List;

/**
 * MQ 失败消息服务。
 *
 * @author xd11cc
 */
public interface IMqFailMessageService extends IService<MqFailMessageDO> {

    /**
     * 条件查询失败消息列表（配合 PageUtils 分页）。
     */
    List<MqFailMessageDO> getList(MqFailMessageQueryVO queryVO);

    /**
     * 重新投递指定失败消息。
     *
     * @param id 失败记录ID
     */
    void replay(Long id);

    /**
     * 编辑消息体后重新投递。
     * <p>
     * 覆写原记录的消息体并置为已重放，再用新内容投递。
     * 仅待处理状态允许该操作。
     * </p>
     *
     * @param id   失败记录ID
     * @param data 修改后的消息体 JSON
     */
    void replayWithEdit(Long id, String data);

    /**
     * 忽略指定失败消息。
     *
     * @param id 失败记录ID
     */
    void ignore(Long id);
}
