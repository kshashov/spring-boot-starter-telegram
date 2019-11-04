package com.github.kshashov.telegram.handler;


import com.github.kshashov.telegram.TelegramControllerBeanPostProcessor;
import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Helper entity which is used to accumulate handlers during the {@link TelegramControllerBeanPostProcessor} work
 */
public class HandlerMethodContainer {
    private final Map<RequestMappingInfo, HandlerMethod> mappingLookup = new LinkedHashMap<>();
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public HandlerLookupResult lookupHandlerMethod(@NotNull TelegramEvent telegramEvent) {
        for (RequestMappingInfo requestMappingInfo : mappingLookup.keySet()) {
            // Check token
            if (!requestMappingInfo.getToken().equals(telegramEvent.getToken())) {
                continue;
            }
            // Check message type
            if (!requestMappingInfo.getMessageTypes().contains(telegramEvent.getMessageType())
                    && !requestMappingInfo.getMessageTypes().contains(MessageType.ANY)) {
                continue;
            }
            // Check pattern
            if (requestMappingInfo.getPatterns().isEmpty()) {
                return new HandlerLookupResult(mappingLookup.get(requestMappingInfo), "", new HashMap<>());
            }

            String text = telegramEvent.getText();
            if (text == null) {
                text = "";
            }
            List<String> matches = getMatchingPatterns(requestMappingInfo, text);
            if (!matches.isEmpty()) {
                Map<String, String> variables = pathMatcher.extractUriTemplateVariables(matches.get(0), text);
                return new HandlerLookupResult(mappingLookup.get(requestMappingInfo), matches.get(0), variables);
            }
        }
        return new HandlerLookupResult();
    }

    public void registerController(@NotNull Object bean, @NotNull Method method, @NotNull RequestMappingInfo mappingInfo) {
        HandlerMethod handlerMethod = new HandlerMethod(bean, method);
        mappingLookup.put(mappingInfo, handlerMethod);
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
        private String basePattern;
        private Map<String, String> templateVariables;

        public boolean hasResolvedMethod() {
            return handlerMethod != null;
        }
    }
}
