package com.github.kshashov.telegram.handler;


import com.github.kshashov.telegram.TelegramControllerBeanPostProcessor;
import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.handler.processor.HandlerMethod;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

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
    private final Map<String, Map<RequestMappingInfo, HandlerMethod>> handlers = new HashMap<>();
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @NotNull
    public HandlerLookupResult lookupHandlerMethod(@NotNull TelegramEvent telegramEvent) {
        Map<RequestMappingInfo, HandlerMethod> botMethods = handlers.get(telegramEvent.getToken());
        if (botMethods != null) {
            for (Map.Entry<RequestMappingInfo, HandlerMethod> botMappings : botMethods.entrySet()) {
                RequestMappingInfo mapping = botMappings.getKey();
                // Check token
                if (!mapping.getToken().equals(telegramEvent.getToken())) {
                    continue;
                }
                // Check message type
                if (!mapping.getMessageTypes().contains(telegramEvent.getMessageType())
                        && !mapping.getMessageTypes().contains(MessageType.ANY)) {
                    continue;
                }
                // Empty patterns list allows all requests
                if (mapping.getPatterns().isEmpty()) {
                    return new HandlerLookupResult(botMappings.getValue(), "", new HashMap<>());
                }

                String text = telegramEvent.getText();
                if (text == null) {
                    text = "";
                }

                // Check patterns
                List<String> matches = getMatchingPatterns(mapping, text);
                if (!matches.isEmpty()) {
                    Map<String, String> variables = pathMatcher.extractUriTemplateVariables(matches.get(0), text);
                    return new HandlerLookupResult(botMappings.getValue(), matches.get(0), variables);
                }
            }
        }
        return new HandlerLookupResult();
    }

    public void registerController(@NotNull Object bean, @NotNull Method method, @NotNull RequestMappingInfo mappingInfo) {
        HandlerMethod handlerMethod = new HandlerMethod(bean, method);
        Map<RequestMappingInfo, HandlerMethod> botHandlers = handlers.computeIfAbsent(mappingInfo.getToken(), (k) -> new HashMap<>());
        botHandlers.put(mappingInfo, handlerMethod);
    }

    private List<String> getMatchingPatterns(@NotNull RequestMappingInfo mappingInfo, @NotNull String lookupPath) {
        List<String> matches = new ArrayList<>();
        for (String pattern : mappingInfo.getPatterns()) {
            if (pathMatcher.match(pattern, lookupPath)) {
                matches.add(pattern);
            }
        }
        matches.sort(pathMatcher.getPatternComparator(lookupPath));
        return matches;
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
}
