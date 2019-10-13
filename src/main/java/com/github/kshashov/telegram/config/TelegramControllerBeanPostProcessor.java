package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.HandlerMethodContainer;
import com.github.kshashov.telegram.RequestMappingInfo;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Searches for {@link com.github.kshashov.telegram.config.TelegramMvcController} inheritors marked with {@link
 * BotController} annotation, then searches for {@link com.github.kshashov.telegram.api.bind.annotation.BotRequest} annotations in methods and store the meta information
 * into {@link com.github.kshashov.telegram.HandlerMethodContainer}
 */
@Slf4j
public class TelegramControllerBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton {
    private final Set<Class<?>> nonAnnotatedClasses =
            Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    final private HandlerMethodContainer botHandlerMethodContainer;

    public TelegramControllerBeanPostProcessor(HandlerMethodContainer botHandlerMethodContainer) {
        this.botHandlerMethodContainer = botHandlerMethodContainer;
    }

    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        return bean;
    }

    @Override
    @Nullable
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (!nonAnnotatedClasses.contains(targetClass)) {
            if (AnnotationUtils.findAnnotation(targetClass, BotController.class) != null) {
                Map<Method, RequestMappingInfo> annotatedMethods = findAnnotatedMethodsBotRequest(targetClass);
                if (annotatedMethods.isEmpty()) {
                    nonAnnotatedClasses.add(targetClass);
                    if (log.isTraceEnabled()) {
                        log.trace("No @BotRequest annotations found on bean class: {}", bean.getClass());
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

                    return new RequestMappingInfo(Sets.newHashSet(requestMapping.path()), Sets.newHashSet(requestMapping.type()));
                });
    }

    @Override
    public void afterSingletonsInstantiated() {
        nonAnnotatedClasses.clear();
    }

}
