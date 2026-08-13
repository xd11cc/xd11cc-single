package com.xd11cc.single.config.pay;

import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.config.annotation.PayClientScan;
import com.xd11cc.single.config.pay.impl.PayClientFactoryImpl;
import com.xd11cc.single.enums.PayChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xd11cc
 * @date 2026-06-09 17:32:37
 * @description
 */
@Slf4j
public class PayClientScannerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        // 1. 从 @PayClientScan 注解中读取 basePackage
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(PayClientScan.class.getName());
        String[] basePackages = (String[]) attrs.get("basePackages");

        // 2. 构造扫描器 -- 仅匹配 @PayClientCode
        ClassPathScanningCandidateComponentProvider scanner = createScanner();

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                try {
                    Class<?> clazz = loadClass(candidate.getBeanClassName());
                    PayClientCode annotation = clazz.getAnnotation(PayClientCode.class);
                    if (annotation == null) continue;

                    PayClientFactoryImpl.registerClientClass(annotation.value(), clazz);
                    log.debug("[PayClientScan][注册 {} -> {}]", annotation.value().getCode(), clazz.getSimpleName());
                } catch (ClassNotFoundException e) {
                    log.error("[PayClientScan][加载失败：{}]", candidate.getBeanClassName(), e);
                }
            }
        }
    }

    /**
     * 按类名加载类，供子类覆写以在单测中注入 mock。
     */
    protected Class<?> loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * 创建扫描器，供子类覆写以在单测中替换 mock。
     */
    protected ClassPathScanningCandidateComponentProvider createScanner() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(PayClientCode.class));
        return scanner;
    }
}
