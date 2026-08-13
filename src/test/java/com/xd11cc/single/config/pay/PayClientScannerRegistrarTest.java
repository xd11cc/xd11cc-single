package com.xd11cc.single.config.pay;

import com.xd11cc.single.config.annotation.PayClientCode;
import com.xd11cc.single.config.annotation.PayClientScan;
import com.xd11cc.single.config.pay.impl.PayClientFactoryImpl;
import com.xd11cc.single.config.pay.impl.test.TestPayClientImpl;
import com.xd11cc.single.enums.PayChannelEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * PayClientScannerRegistrar 单测
 * <p>
 * 测试 seam：{@link PayClientScannerRegistrar#registerBeanDefinitions}。
 * <p>
 * 生产代码将扫描器创建和类加载分别抽成 {@link #createScanner()} 和 {@link #loadClass(String)}，
 * 测试通过匿名子类覆写这两个方法注入 mock，避免使用 {@code MockedConstruction} /
 * {@code MockedStatic<Class>}（与 mockito-inline 4.5.1 兼容性不佳）。
 * </p>
 * <p>
 * 测试策略：通过反射验证 {@code PayClientFactoryImpl.clientClass} map 的状态，
 * 确保扫描器返回的候选类被正确注册。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class PayClientScannerRegistrarTest {

    // ==================== 生命周期 ====================

    @BeforeEach
    void setUp() throws Exception {
        resetStaticClientClassMap();
    }

    // ==================== registerBeanDefinitions - 扫描到 PayClientCode 注册 ====================

    @Test
    void registerBeanDefinitions_扫描到PayClientCode_注册到clientClassMap() throws Exception {
        String basePackage = "com.xd11cc.single.config.pay.impl.test";
        String testClassName = TestPayClientImpl.class.getName();

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("basePackages", new String[]{basePackage});

        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        given(metadata.getAnnotationAttributes(PayClientScan.class.getName())).willReturn(attrs);

        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);

        BeanDefinition beanDef = mock(BeanDefinition.class);
        given(beanDef.getBeanClassName()).willReturn(testClassName);

        ClassPathScanningCandidateComponentProvider scanner = mockScanner();
        given(scanner.findCandidateComponents(anyString()))
                .willReturn(Collections.singleton(beanDef));

        // Act - 通过匿名子类覆写 createScanner 和 loadClass 注入 mock
        PayClientScannerRegistrar registrar = newScanner(scanner, testClassName, TestPayClientImpl.class);

        registrar.registerBeanDefinitions(metadata, registry);

        assertClientClassMapContains(PayChannelEnum.WALLET, TestPayClientImpl.class);
    }

    // ==================== registerBeanDefinitions - 候选类缺少 PayClientCode 注解 ====================

    @Test
    void registerBeanDefinitions_候选类无PayClientCode_跳过不注册() throws Exception {
        String fakeClassName = "com.example.NoAnnotationClass";

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("basePackages", new String[]{"com.example.pkg"});

        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        given(metadata.getAnnotationAttributes(PayClientScan.class.getName())).willReturn(attrs);

        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);

        BeanDefinition beanDef = mock(BeanDefinition.class);
        given(beanDef.getBeanClassName()).willReturn(fakeClassName);

        ClassPathScanningCandidateComponentProvider scanner = mockScanner();
        given(scanner.findCandidateComponents(anyString()))
                .willReturn(Collections.singleton(beanDef));

        // 用真实 Class 对象（无 @PayClientCode）代替 mock(Class.class)
        Class<?> noAnnotationClass = Object.class;

        PayClientScannerRegistrar registrar = newScanner(scanner, fakeClassName, noAnnotationClass);

        registrar.registerBeanDefinitions(metadata, registry);

        assertClientClassMapEmpty();
    }

    // ==================== registerBeanDefinitions - Class.forName 加载失败 ====================

    @Test
    void registerBeanDefinitions_ClassNotFoundException_记录日志并继续() throws Exception {
        String brokenClassName = "com.example.BrokenClass";

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("basePackages", new String[]{"com.example.other"});

        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        given(metadata.getAnnotationAttributes(PayClientScan.class.getName())).willReturn(attrs);

        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);

        BeanDefinition beanDef = mock(BeanDefinition.class);
        given(beanDef.getBeanClassName()).willReturn(brokenClassName);

        ClassPathScanningCandidateComponentProvider scanner = mockScanner();
        given(scanner.findCandidateComponents(anyString()))
                .willReturn(Collections.singleton(beanDef));

        PayClientScannerRegistrar registrar = newScannerThrowing(scanner, brokenClassName,
                new ClassNotFoundException(brokenClassName));

        // 不应抛异常，内部 catch 了
        registrar.registerBeanDefinitions(metadata, registry);

        assertClientClassMapEmpty();
    }

    // ==================== registerBeanDefinitions - 无扫描结果 ====================

    @Test
    void registerBeanDefinitions_无扫描结果_不注册() throws Exception {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("basePackages", new String[]{"com.example.empty"});

        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        given(metadata.getAnnotationAttributes(PayClientScan.class.getName())).willReturn(attrs);

        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);

        ClassPathScanningCandidateComponentProvider scanner = mockScanner();
        given(scanner.findCandidateComponents(anyString()))
                .willReturn(Collections.emptySet());

        PayClientScannerRegistrar registrar = newScanner(scanner, "unused", Object.class);

        registrar.registerBeanDefinitions(metadata, registry);

        assertClientClassMapEmpty();
    }

    // ==================== registerBeanDefinitions - 多个包 ====================

    @Test
    void registerBeanDefinitions_多个basePackage_全部扫描() throws Exception {
        String pkg1 = "com.example.pkg1";
        String pkg2 = "com.example.pkg2";
        String class1 = "com.example.pkg1.Client1";
        String class2 = "com.example.pkg2.Client2";

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("basePackages", new String[]{pkg1, pkg2});

        AnnotationMetadata metadata = mock(AnnotationMetadata.class);
        given(metadata.getAnnotationAttributes(PayClientScan.class.getName())).willReturn(attrs);

        BeanDefinitionRegistry registry = mock(BeanDefinitionRegistry.class);

        BeanDefinition def1 = mock(BeanDefinition.class);
        given(def1.getBeanClassName()).willReturn(class1);
        BeanDefinition def2 = mock(BeanDefinition.class);
        given(def2.getBeanClassName()).willReturn(class2);

        ClassPathScanningCandidateComponentProvider scanner = mockScanner();
        given(scanner.findCandidateComponents(pkg1))
                .willReturn(Collections.singleton(def1));
        given(scanner.findCandidateComponents(pkg2))
                .willReturn(Collections.singleton(def2));

        // 用真实 Class 对象（无 @PayClientCode）代替 mock(Class.class)
        Class<?> client1Class = Object.class;
        Class<?> client2Class = String.class;

        PayClientScannerRegistrar registrar = newScannerForMulti(scanner, class1, client1Class, class2, client2Class);

        registrar.registerBeanDefinitions(metadata, registry);

        // 两个候选类都没有 @PayClientCode，所以 clientClass map 仍为空
        assertClientClassMapEmpty();
    }

    // ==================== 工厂方法 ====================

    private static ClassPathScanningCandidateComponentProvider mockScanner() {
        return mock(ClassPathScanningCandidateComponentProvider.class);
    }

    /**
     * 创建测试用 Registrar，覆写 createScanner 和 loadClass。
     */
    private static PayClientScannerRegistrar newScanner(
            ClassPathScanningCandidateComponentProvider scanner,
            String loadClassName,
            Class<?> loadResult) {
        return new PayClientScannerRegistrar() {
            @Override
            protected ClassPathScanningCandidateComponentProvider createScanner() {
                return scanner;
            }

            @Override
            protected Class<?> loadClass(String className) throws ClassNotFoundException {
                if (className.equals(loadClassName)) {
                    return loadResult;
                }
                // 生产行为兜底（理论上单测不会走到这里）
                return Class.forName(className);
            }
        };
    }

    /**
     * 创建测试用 Registrar，loadClass 在匹配特定类名时抛异常。
     */
    private static PayClientScannerRegistrar newScannerThrowing(
            ClassPathScanningCandidateComponentProvider scanner,
            String loadClassName,
            ClassNotFoundException exception) {
        return new PayClientScannerRegistrar() {
            @Override
            protected ClassPathScanningCandidateComponentProvider createScanner() {
                return scanner;
            }

            @Override
            protected Class<?> loadClass(String className) throws ClassNotFoundException {
                if (className.equals(loadClassName)) {
                    throw exception;
                }
                return Class.forName(className);
            }
        };
    }

    /**
     * 创建测试用 Registrar，支持多个类名 -> Class 的映射。
     */
    private static PayClientScannerRegistrar newScannerForMulti(
            ClassPathScanningCandidateComponentProvider scanner,
            String className1, Class<?> class1,
            String className2, Class<?> class2) {
        return new PayClientScannerRegistrar() {
            @Override
            protected ClassPathScanningCandidateComponentProvider createScanner() {
                return scanner;
            }

            @Override
            protected Class<?> loadClass(String className) throws ClassNotFoundException {
                if (className.equals(className1)) {
                    return class1;
                }
                if (className.equals(className2)) {
                    return class2;
                }
                return Class.forName(className);
            }
        };
    }

    // ==================== 辅助方法 ====================

    /**
     * 通过反射重置 PayClientFactoryImpl 的静态 {@code clientClass} map，
     * 避免用例间状态污染。
     */
    private static void resetStaticClientClassMap() throws Exception {
        java.lang.reflect.Field field = PayClientFactoryImpl.class.getDeclaredField("clientClass");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>> map =
                (ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>>) field.get(null);
        map.clear();
    }

    /**
     * 断言 clientClass map 包含指定 entry
     */
    private static void assertClientClassMapContains(PayChannelEnum key,
                                                     Class<? extends PayClient<?>> value) throws Exception {
        java.lang.reflect.Field field = PayClientFactoryImpl.class.getDeclaredField("clientClass");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>> map =
                (ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>>) field.get(null);
        assertThat(map).containsEntry(key, value);
    }

    /**
     * 断言 clientClass map 为空
     */
    private static void assertClientClassMapEmpty() throws Exception {
        java.lang.reflect.Field field = PayClientFactoryImpl.class.getDeclaredField("clientClass");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>> map =
                (ConcurrentHashMap<PayChannelEnum, Class<? extends PayClient<?>>>) field.get(null);
        assertThat(map).isEmpty();
    }
}
