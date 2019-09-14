package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.BotController;
import com.github.kshashov.telegram.api.BotRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Бин пост процессор, ищет классы помеченные анотацией {@link BotController}, далее ищет анотацию  {@link BotRequest}
 * в методах и передает мета информацию в {@link HandlerMethodContainer}
 */
public class TelegramControllerBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton {
    private static final Logger logger = LoggerFactory.getLogger(TelegramControllerBeanPostProcessor.class);
    private HandlerMethodContainer botHandlerMethodContainer;

    private final Set<Class<?>> nonAnnotatedClasses =
            Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    public TelegramControllerBeanPostProcessor(HandlerMethodContainer botHandlerMethodContainer) {
        this.botHandlerMethodContainer = botHandlerMethodContainer;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (!nonAnnotatedClasses.contains(targetClass)) {
            if (AnnotationUtils.findAnnotation(targetClass, BotController.class) != null) {
                Map<Method, RequestMappingInfo> annotatedMethods = findAnnotatedMethodsBotRequest(targetClass);
                if (annotatedMethods.isEmpty()) {
                    nonAnnotatedClasses.add(targetClass);
                    if (logger.isTraceEnabled()) {
                        logger.trace("No @BotRequest annotations found on bean class: {}", bean.getClass());
                    }
                } else {
                    // Non-empty set of methods
                    annotatedMethods.forEach((method, mappingInfo) -> {
                        Method invocableMethod = AopUtils.selectInvocableMethod(method, targetClass);
                        botHandlerMethodContainer.registerController(bean, invocableMethod, mappingInfo);
                    });
                }
            } else {
                nonAnnotatedClasses.add(targetClass);
            }
        }
        return bean;
    }

    private Map<Method, RequestMappingInfo> findAnnotatedMethodsBotRequest(Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                    BotRequest requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, BotRequest.class);
                    if (requestMapping == null) return null;

                    return RequestMappingInfo
                            .newBuilder()
                            .path(requestMapping.path())
                            .messageType(requestMapping.messageType())
                            .build();

                });
    }

    @Override
    public void afterSingletonsInstantiated() {
        nonAnnotatedClasses.clear();
    }

}
