package com.xd11cc.single.config.flowable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Component
public class WorkflowHandlerFactory {

    private final Map<String, AbstractWorkflowHandler> handlerMap = new HashMap<>();

    @Autowired
    public WorkflowHandlerFactory(List<AbstractWorkflowHandler> handlers) {
        for (AbstractWorkflowHandler handler : handlers) {
            handlerMap.put(handler.getProcessDefinitionKey(), handler);
        }
    }

    public AbstractWorkflowHandler getHandler(String processDefinitionKey) {
        AbstractWorkflowHandler handler = handlerMap.get(processDefinitionKey);
        if (handler != null) return handler;
        return new AbstractWorkflowHandler() {
            @Override public String getProcessDefinitionKey() { return processDefinitionKey; }
            @Override public String getStartActivityId() { return null; }
        };
    }
}
