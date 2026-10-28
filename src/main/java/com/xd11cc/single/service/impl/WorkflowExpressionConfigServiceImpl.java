package com.xd11cc.single.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xd11cc.single.infrastructure.redis.RedisCache;
import com.xd11cc.single.common.context.TenantContextHolder;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.domain.WorkflowExpressionConfigDO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigAddVO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigQueryVO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.mapper.WorkflowExpressionConfigMapper;
import com.xd11cc.single.service.IWorkflowExpressionConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WorkflowExpressionConfigServiceImpl extends ServiceImpl<WorkflowExpressionConfigMapper, WorkflowExpressionConfigDO>
        implements IWorkflowExpressionConfigService {

    /**
     * 表达式选项缓存有效期（分钟）
     */
    private static final long EXPRESSION_OPTIONS_CACHE_MINUTES = 30L;

    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_.-]{0,127}$");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([a-zA-Z][a-zA-Z0-9_.-]*)}}") ;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]*$");
    private static final Map<String, Set<String>> TYPE_RETURN_TYPES = new HashMap<>();
    static {
        TYPE_RETURN_TYPES.put("ASSIGNEE", new HashSet<>(java.util.Collections.singletonList("USER_ID")));
        TYPE_RETURN_TYPES.put("CANDIDATE_USERS", new HashSet<>(java.util.Collections.singletonList("USER_LIST")));
        TYPE_RETURN_TYPES.put("CANDIDATE_GROUPS", new HashSet<>(java.util.Collections.singletonList("GROUP_LIST")));
        TYPE_RETURN_TYPES.put("MULTI_INSTANCE_USERS", new HashSet<>(java.util.Collections.singletonList("USER_LIST")));
        TYPE_RETURN_TYPES.put("SEQUENCE_CONDITION", new HashSet<>(java.util.Collections.singletonList("BOOLEAN")));
        TYPE_RETURN_TYPES.put("COMPLETION_CONDITION", new HashSet<>(java.util.Collections.singletonList("BOOLEAN")));
        TYPE_RETURN_TYPES.put("LISTENER_EXPRESSION", new HashSet<>(java.util.Arrays.asList("VOID", "STRING")));
    }
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 表达式配置选项的缓存键，必须按租户隔离。
     * <p>
     * 该缓存存的是“下拉选项列表”，若所有租户共用一个 key，
     * 先查询的租户会把结果写入缓存，其他租户随后直接命中，
     * 从而看到别人租户的表达式配置（跨租户数据泄露）。
     * </p>
     */
    private String cacheKey() {
        Long tenantId = TenantContextHolder.getTenantId();
        return CacheConstants.WORKFLOW_EXPRESSION_CONFIG_OPTIONS + ":" + (tenantId == null ? "none" : tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(WorkflowExpressionConfigAddVO vo) {
        validate(vo.getConfigKey(), vo.getExpressionType(), vo.getReturnType(), vo.getExpressionTemplate(), vo.getParameterSchema());
        WorkflowExpressionConfigDO data = new WorkflowExpressionConfigDO();
        data.setConfigKey(vo.getConfigKey()); data.setConfigName(vo.getConfigName());
        data.setExpressionType(vo.getExpressionType()); data.setExpressionTemplate(vo.getExpressionTemplate());
        data.setParameterSchema(vo.getParameterSchema()); data.setReturnType(vo.getReturnType());
        data.setStatus(StringUtils.defaultIfBlank(vo.getStatus(), SystemStatusEnum.NORMAL.getCode())); data.setRemark(vo.getRemark());
        try { int row = baseMapper.insert(data); redisCache.removeCacheObject(cacheKey()); return row; }
        catch (DuplicateKeyException e) { throw new ServiceException(SystemErrorEnum.EXPRESSION_CONFIG_KEY_EXISTS); }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        int row = baseMapper.deleteBatchIds(ids);
        if (row > 0) redisCache.removeCacheObject(cacheKey());
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int modifyById(WorkflowExpressionConfigUpdateVO vo) {
        validate(vo.getConfigKey(), vo.getExpressionType(), vo.getReturnType(), vo.getExpressionTemplate(), vo.getParameterSchema());
        WorkflowExpressionConfigDO data = new WorkflowExpressionConfigDO();
        data.setId(vo.getId()); data.setConfigKey(vo.getConfigKey()); data.setConfigName(vo.getConfigName());
        data.setExpressionType(vo.getExpressionType()); data.setExpressionTemplate(vo.getExpressionTemplate());
        data.setParameterSchema(vo.getParameterSchema()); data.setReturnType(vo.getReturnType());
        data.setStatus(vo.getStatus()); data.setRemark(vo.getRemark());
        try { int row = baseMapper.updateById(data); redisCache.removeCacheObject(cacheKey()); return row; }
        catch (DuplicateKeyException e) { throw new ServiceException(SystemErrorEnum.EXPRESSION_CONFIG_KEY_EXISTS); }
    }

    @Override
    public List<WorkflowExpressionConfigDO> getList(WorkflowExpressionConfigQueryVO vo) {
        LambdaQueryWrapper<WorkflowExpressionConfigDO> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.isNotBlank(vo.getConfigKey()), WorkflowExpressionConfigDO::getConfigKey, vo.getConfigKey())
                .like(StringUtils.isNotBlank(vo.getConfigName()), WorkflowExpressionConfigDO::getConfigName, vo.getConfigName())
                .eq(StringUtils.isNotBlank(vo.getExpressionType()), WorkflowExpressionConfigDO::getExpressionType, vo.getExpressionType())
                .eq(StringUtils.isNotBlank(vo.getStatus()), WorkflowExpressionConfigDO::getStatus, vo.getStatus())
                .orderByDesc(WorkflowExpressionConfigDO::getId);
        return baseMapper.selectList(w);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WorkflowExpressionConfigDO> getOptions(String expressionType) {
        List<WorkflowExpressionConfigDO> list = redisCache.getCacheObject(cacheKey());
        if (list == null) {
            LambdaQueryWrapper<WorkflowExpressionConfigDO> w = new LambdaQueryWrapper<>();
            w.eq(WorkflowExpressionConfigDO::getStatus, SystemStatusEnum.NORMAL.getCode())
                    .orderByDesc(WorkflowExpressionConfigDO::getId);
            list = baseMapper.selectList(w);
            // 加 TTL：避免配置变更后缓存长期不失效导致选项陈旧（写操作虽会主动清除，但兜底更稳）
            redisCache.setCacheObject(cacheKey(), list, EXPRESSION_OPTIONS_CACHE_MINUTES, TimeUnit.MINUTES);
        }
        if (StringUtils.isBlank(expressionType)) return list;
        return list.stream().filter(x -> expressionType.equals(x.getExpressionType())).collect(Collectors.toList());
    }

    @Override
    public WorkflowExpressionConfigDO getEnabledByKey(String configKey) {
        LambdaQueryWrapper<WorkflowExpressionConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowExpressionConfigDO::getConfigKey, configKey)
                .eq(WorkflowExpressionConfigDO::getStatus, SystemStatusEnum.NORMAL.getCode())
                .last("LIMIT 1");
        WorkflowExpressionConfigDO config = baseMapper.selectOne(wrapper);
        if (config == null) throw new ServiceException(SystemErrorEnum.EXPRESSION_CONFIG_NOT_FOUND);
        return config;
    }

    @Override
    public String resolveExpression(String configKey, String parametersJson) {
        WorkflowExpressionConfigDO config = getEnabledByKey(configKey);
        try {
            JsonNode schema = objectMapper.readTree(config.getParameterSchema());
            JsonNode values = objectMapper.readTree(StringUtils.defaultIfBlank(parametersJson, "{}"));
            String result = config.getExpressionTemplate();
            for (JsonNode parameter : schema) {
                String key = parameter.path("key").asText();
                String type = parameter.path("type").asText();
                boolean required = parameter.path("required").asBoolean(false);
                JsonNode value = values.get(key);
                if (required && (value == null || value.isNull() || value.asText().trim().isEmpty())) {
                    throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_MISSING, new Object[]{key});
                }
                String replacement = formatParameter(type, value);
                result = result.replace("{{" + key + "}}", replacement);
            }
            if (PLACEHOLDER_PATTERN.matcher(result).find()) throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_UNRESOLVED);
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_FORMAT_ERROR);
        }
    }

    private String formatParameter(String type, JsonNode value) {
        if (value == null || value.isNull()) return "";
        String text = value.asText();
        switch (type) {
            case "NUMBER":
                try { return new java.math.BigDecimal(text).stripTrailingZeros().toPlainString(); }
                catch (Exception e) { throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_NUMBER_ERROR); }
            case "BOOLEAN":
                if (!"true".equalsIgnoreCase(text) && !"false".equalsIgnoreCase(text)) throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_BOOLEAN_ERROR);
                return text.toLowerCase();
            case "PROCESS_VARIABLE":
                if (!VARIABLE_PATTERN.matcher(text).matches()) throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_VARIABLE_ERROR);
                return text;
            case "STRING":
            default:
                return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
        }
    }

    private void validate(String key, String expressionType, String returnType, String template, String schema) {
        if (!KEY_PATTERN.matcher(key).matches()) throw new ServiceException(SystemErrorEnum.EXPRESSION_CONFIG_KEY_INVALID);
        if (!TYPE_RETURN_TYPES.containsKey(expressionType)) throw new ServiceException(SystemErrorEnum.EXPRESSION_TYPE_UNSUPPORTED);
        if (!TYPE_RETURN_TYPES.get(expressionType).contains(returnType)) throw new ServiceException(SystemErrorEnum.EXPRESSION_RETURN_TYPE_MISMATCH);
        if (!template.trim().startsWith("${") || !template.trim().endsWith("}")) throw new ServiceException(SystemErrorEnum.EXPRESSION_TEMPLATE_INVALID);
        try {
            JsonNode node = objectMapper.readTree(schema);
            if (!node.isArray()) throw new IllegalArgumentException();
            if ("LISTENER_EXPRESSION".equals(expressionType) && node.size() > 0) throw new ServiceException(SystemErrorEnum.LISTENER_EXPRESSION_NO_PARAM);
            Set<String> parameterKeys = new HashSet<>();
            for (JsonNode parameter : node) {
                String parameterKey = parameter.path("key").asText();
                if (StringUtils.isBlank(parameterKey) || !parameterKeys.add(parameterKey)) {
                    throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_KEY_DUPLICATE);
                }
            }
            Set<String> placeholderKeys = new HashSet<>();
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
            while (matcher.find()) placeholderKeys.add(matcher.group(1));
            if (!placeholderKeys.equals(parameterKeys)) throw new ServiceException(SystemErrorEnum.EXPRESSION_PLACEHOLDER_MISMATCH);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(SystemErrorEnum.EXPRESSION_PARAM_SCHEMA_INVALID);
        }
    }
}
