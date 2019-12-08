package com.github.kshashov.telegram.autoconfigure;

import com.github.kshashov.telegram.autoconfigure.annotation.BotController;
import com.github.kshashov.telegram.autoconfigure.annotation.BotRequest;
import com.github.kshashov.telegram.autoconfigure.api.TelegramMvcController;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.RequestMappingInfo;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Searches for {@link TelegramMvcController} inheritors marked with {@link BotController} annotation, then searches for
 * {@link BotRequest} annotations in methods and store the meta
 * information into {@link HandlerMethodContainer}.
 */
@Slf4j
public class TelegramControllerBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton {
    private final Set<Class<?>> nonAnnotatedClasses =
            Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    final private HandlerMethodContainer botHandlerMethodContainer;

    public TelegramControllerBeanPostProcessor(@NotNull HandlerMethodContainer botHandlerMethodContainer) {
        this.botHandlerMethodContainer = botHandlerMethodContainer;
    }

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        return bean;
    }

    @Override
    @Nullable
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (!nonAnnotatedClasses.contains(targetClass)) {
            if ((TelegramMvcController.class.isAssignableFrom(targetClass))
                    && (AnnotationUtils.findAnnotation(targetClass, BotController.class) != null)) {
                TelegramMvcController controller = (TelegramMvcController) bean;
                Map<Method, RequestMappingInfo> annotatedMethods = findAnnotatedMethodsBotRequest(controller.getToken(), targetClass);
                if (annotatedMethods.isEmpty()) {
                    nonAnnotatedClasses.add(targetClass);
                    if (log.isTraceEnabled()) {
                        log.warn("No @BotRequest annotations found on bean class: {}", bean.getClass());
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

    private Map<Method, RequestMappingInfo> findAnnotatedMethodsBotRequest(String token, Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                    BotRequest requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, BotRequest.class);
                    if (requestMapping == null) return null;

                    return new RequestMappingInfo(token, Sets.newHashSet(requestMapping.path()), Sets.newHashSet(requestMapping.type()));
                });
    }

    @Override
    public void afterSingletonsInstantiated() {
        nonAnnotatedClasses.clear();
    }

}
