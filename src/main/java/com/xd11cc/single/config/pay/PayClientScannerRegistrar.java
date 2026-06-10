package com.xd11cc.single.config.pay;

import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.config.annotation.PayClientScan;
import com.xd11cc.single.config.pay.impl.PayClientFactoryImpl;
import com.xd11cc.single.enums.PayChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
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
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(PayClientCode.class));

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                try {
                    Class<?> clazz = Class.forName(candidate.getBeanClassName());
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
}
