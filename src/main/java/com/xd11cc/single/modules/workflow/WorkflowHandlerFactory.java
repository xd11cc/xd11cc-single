package com.xd11cc.single.modules.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流业务处理器工厂（策略模式）。
 * <p>
 * 按 {@code processDefinitionKey} 查找对应的 {@link AbstractWorkflowHandler}；
 * 未注册时返回一个“空实现”兜底对象，保证核心流转不被阻断。
 * </p>
 *
 * @author xd11cc
 * @date 2026-05-29
 */
@Slf4j
@Component
public class WorkflowHandlerFactory {

    /**
     * 兜底处理器：缓存单例，避免每次查找都创建匿名对象。
     * <p>
     * 兜底处理器不会执行业务回调，{@code startActivityId} 返回 {@code null}——
     * 调用方（如驳回逻辑）必须容忍该情况，按流程结构自行推断发起节点。
     * </p>
     */
    private static final AbstractWorkflowHandler DEFAULT_HANDLER = new AbstractWorkflowHandler() {
        @Override
        public String getProcessDefinitionKey() {
            return null;
        }

        @Override
        public String getStartActivityId() {
            return null;
        }
    };

    private final Map<String, AbstractWorkflowHandler> handlerMap = new HashMap<>();

    @Autowired
    public WorkflowHandlerFactory(List<AbstractWorkflowHandler> handlers) {
        if (handlers != null) {
            for (AbstractWorkflowHandler handler : handlers) {
                String key = handler.getProcessDefinitionKey();
                if (key == null || key.trim().isEmpty()) {
                    log.warn("[工作流] Handler {} 未返回 processDefinitionKey，已跳过注册",
                            handler.getClass().getName());
                    continue;
                }
                AbstractWorkflowHandler previous = handlerMap.put(key, handler);
                if (previous != null) {
                    log.warn("[工作流] processDefinitionKey={} 存在重复 Handler：{} 覆盖 {}",
                            key, handler.getClass().getSimpleName(), previous.getClass().getSimpleName());
                }
            }
        }
        log.info("[工作流] 已注册业务处理器 {} 个：{}", handlerMap.size(), handlerMap.keySet());
    }

    /**
     * 获取处理器。
     * <p>
     * 未注册时返回单例兜底对象并打印 debug 日志。这里不抛 {@code HANDLER_NOT_FOUND}，
     * 是因为流程的流转本身由 BPMN 驱动，Handler 只承载可选业务回调；
     * 强行抛异常会让“未写 Handler 的流程”完全不可用。
     * </p>
     *
     * @param processDefinitionKey 流程定义 key
     * @return 对应的处理器，永不为 {@code null}
     */
    public AbstractWorkflowHandler getHandler(String processDefinitionKey) {
        AbstractWorkflowHandler handler = handlerMap.get(processDefinitionKey);
        if (handler != null) {
            return handler;
        }
        if (log.isDebugEnabled()) {
            log.debug("[工作流] processDefinitionKey={} 未注册 Handler，使用兜底实现", processDefinitionKey);
        }
        return DEFAULT_HANDLER;
    }

    /**
     * 是否存在已注册的业务处理器。
     */
    public boolean contains(String processDefinitionKey) {
        return handlerMap.containsKey(processDefinitionKey);
    }
}
