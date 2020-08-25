package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.RequestMappingInfo;
import com.github.kshashov.telegram.handler.processor.HandlerMethod;
import com.github.kshashov.telegram.metrics.MetricsService;
import com.google.common.collect.Lists;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Searches for {@link TelegramMvcController} inheritors marked with {@link BotController} annotation, then searches for
 * {@link com.github.kshashov.telegram.api.bind.annotation.BotRequest} annotations in methods and store the meta
 * information into {@link HandlerMethodContainer}.
 */
@Slf4j
public class TelegramControllerBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton {
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private final HandlerMethodContainer botHandlerMethodContainer;
    private final MetricsService metricsService;

    public TelegramControllerBeanPostProcessor(@NotNull HandlerMethodContainer botHandlerMethodContainer, @NotNull MetricsService metricsService) {
        this.botHandlerMethodContainer = botHandlerMethodContainer;
        this.metricsService = metricsService;
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
                Map<Method, List<RequestMappingInfo>> annotatedMethods = findAnnotatedMethodsBotRequest(controller.getToken(), targetClass);
                if (annotatedMethods.isEmpty()) {
                    nonAnnotatedClasses.add(targetClass);
                    if (log.isTraceEnabled()) {
                        log.warn("No @BotRequest annotations found on bean class: {}", bean.getClass());
                    }
                } else {
                    // Non-empty set of methods
                    annotatedMethods.forEach((method, mappingInfos) -> {
                        Method invocableMethod = AopUtils.selectInvocableMethod(method, targetClass);
                        HandlerMethod handlerMethod = botHandlerMethodContainer.registerController(bean, invocableMethod, mappingInfos);
                        metricsService.registerHandlerMethod(handlerMethod);
                    });
                }
            } else {
                nonAnnotatedClasses.add(targetClass);
            }
        }
        return bean;
    }

    private Map<Method, List<RequestMappingInfo>> findAnnotatedMethodsBotRequest(String token, Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass, (MethodIntrospector.MetadataLookup<List<RequestMappingInfo>>) method -> {
            BotRequest requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, BotRequest.class);
            if (requestMapping == null) return null;
            HashSet<MessageType> types = Sets.newHashSet(requestMapping.type());

            if (requestMapping.path().length == 0) {
                return Lists.newArrayList(new RequestMappingInfo(token, null, Integer.MAX_VALUE, types));
            }

            return Arrays.stream(requestMapping.path())
                    .map(path -> new RequestMappingInfo(token, path, requestMapping.path().length, types))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void afterSingletonsInstantiated() {
        nonAnnotatedClasses.clear();
    }

}
