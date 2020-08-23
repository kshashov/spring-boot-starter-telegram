package com.github.kshashov.telegram.handler;


import com.github.kshashov.telegram.TelegramControllerBeanPostProcessor;
import com.github.kshashov.telegram.handler.processor.HandlerMethod;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-Unsafe helper entity which is used to accumulate handlers during the {@link TelegramControllerBeanPostProcessor} processing.
 */
public class HandlerMethodContainer {
    private final Map<String, List<RequestMapping>> handlers = new HashMap<>();
    private RequestMappingsMatcherStrategy matcherStrategy;

    @NotNull
    public HandlerLookupResult lookupHandlerMethod(@NotNull TelegramEvent telegramEvent) {
        if (matcherStrategy == null) throw new IllegalStateException("MatcherStrategy is not set");

        List<RequestMapping> botMethods = handlers.get(telegramEvent.getToken());
        if (botMethods != null) {
            for (RequestMapping botMappings : botMethods) {
                RequestMappingInfo info = botMappings.getMappingInfo();
                // Check token
                if (!info.getToken().equals(telegramEvent.getToken())) {
                    continue;
                }

                // Is matched
                if (matcherStrategy.isMatched(telegramEvent, info)) {
                    Map<String, String> variables = matcherStrategy.extractPatternVariables(telegramEvent.getText(), info);
                    return new HandlerLookupResult(botMappings.getHandlerMethod(), info.getPattern(), variables);
                }
            }
        }
        return new HandlerLookupResult();
    }

    public HandlerMethod registerController(@NotNull Object bean, @NotNull Method method, @NotNull List<RequestMappingInfo> mappingInfo) {
        if (mappingInfo.isEmpty()) return null;
        HandlerMethod handlerMethod = new HandlerMethod(bean, method);
        List<RequestMapping> botHandlers = handlers.computeIfAbsent(mappingInfo.get(0).getToken(), (k) -> new ArrayList<>());
        mappingInfo.forEach(info -> botHandlers.add(new RequestMapping(info, handlerMethod)));
        return handlerMethod;
    }

    public void setMatcherStrategy(@NotNull RequestMappingsMatcherStrategy matcherStrategy) {
        this.matcherStrategy = matcherStrategy;
        handlers.replaceAll((key, value) -> matcherStrategy.postProcess(value));
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandlerLookupResult {
        @Nullable
        private HandlerMethod handlerMethod;
        @Nullable
        private String basePattern;
        @Nullable
        private Map<String, String> templateVariables;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RequestMapping {
        private RequestMappingInfo mappingInfo;
        private HandlerMethod handlerMethod;
    }
}
