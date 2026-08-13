package com.xd11cc.single.config.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionContextHolderTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // ==================== setContext + getContext round-trip ====================

    @Test
    void setAndGet_权限上下文_往返一致() {
        mockRequestAttributes();

        PermissionContextHolder.setContext("system:user:add");
        String value = PermissionContextHolder.getContext();

        assertThat(value).isEqualTo("system:user:add");
    }

    // ==================== setContext 空值 ====================

    @Test
    void setAndGet_null权限_返回null() {
        mockRequestAttributes();

        PermissionContextHolder.setContext(null);
        String value = PermissionContextHolder.getContext();

        assertThat(value).isNull();
    }

    // ==================== getContext 未设置 ====================

    @Test
    void getContext_未设置_返回null() {
        mockRequestAttributes();

        // 不调用 setContext，直接 get
        String value = PermissionContextHolder.getContext();

        assertThat(value).isNull();
    }

    // ==================== setContext 覆盖 ====================

    @Test
    void setContext_覆盖已有值() {
        mockRequestAttributes();

        PermissionContextHolder.setContext("first");
        PermissionContextHolder.setContext("second");

        String value = PermissionContextHolder.getContext();

        assertThat(value).isEqualTo("second");
    }

    // ==================== 辅助方法 ====================

    private void mockRequestAttributes() {
        Map<String, Object> storage = new HashMap<>();
        RequestAttributes requestAttributes = new InMemoryRequestAttributes(storage);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    /**
     * 基于内存的 RequestAttributes 实现，用于单测
     */
    private static class InMemoryRequestAttributes implements RequestAttributes {

        private final Map<String, Object> storage;

        InMemoryRequestAttributes(Map<String, Object> storage) {
            this.storage = storage;
        }

        @Override
        public String[] getAttributeNames(int scope) {
            return storage.keySet().toArray(new String[0]);
        }

        @Override
        public Object getAttribute(String name, int scope) {
            return storage.get(name);
        }

        @Override
        public void setAttribute(String name, Object value, int scope) {
            storage.put(name, value);
        }

        @Override
        public void removeAttribute(String name, int scope) {
            storage.remove(name);
        }

        @Override
        public void registerDestructionCallback(String name, Runnable callback, int scope) {
            // no-op for test
        }

        @Override
        public Object resolveReference(String key) {
            return null;
        }

        @Override
        public String getSessionId() {
            return null;
        }

        @Override
        public Object getSessionMutex() {
            return this;
        }
    }
}
