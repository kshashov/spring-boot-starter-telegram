package com.github.kshashov.telegram;


import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.config.TelegramControllerBeanPostProcessor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.method.HandlerMethod;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper entity which is used to accumulate handlers during the {@link TelegramControllerBeanPostProcessor} work
 */
public class HandlerMethodContainer {
    private final Map<RequestMappingInfo, HandlerMethod> mappingLookup = new LinkedHashMap<>();
    private PathMatcher pathMatcher = new AntPathMatcher();

    public HandlerMethod lookupHandlerMethod(@NotNull TelegramRequest telegramRequest) {
        for (RequestMappingInfo requestMappingInfo : mappingLookup.keySet()) {
            if (!requestMappingInfo.getMessageTypes().contains(telegramRequest.getMessageType())
                    && !requestMappingInfo.getMessageTypes().contains(MessageType.ANY)) {
                continue;
            }

            if (requestMappingInfo.getPatterns().isEmpty()) {
                return mappingLookup.get(requestMappingInfo);
            }

            String text = telegramRequest.getText();
            if (text == null) {
                text = "";
            }
            List<String> matches = getMatchingPatterns(requestMappingInfo, text);
            if (!matches.isEmpty()) {
                Map<String, String> variables = pathMatcher.extractUriTemplateVariables(matches.get(0), text);
                telegramRequest.setBasePattern(matches.get(0));
                telegramRequest.setTemplateVariables(variables);
                return mappingLookup.get(requestMappingInfo);
            }
        }
        return null;
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
}
