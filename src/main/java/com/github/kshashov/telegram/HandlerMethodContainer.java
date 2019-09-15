package com.github.kshashov.telegram;


import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.TelegramRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper entity which is used to accumulate handlers during the {@link TelegramControllerBeanPostProcessor} work
 */
class HandlerMethodContainer {
    private final Map<RequestMappingInfo, HandlerMethod> mappingLookup = new LinkedHashMap<>();

    public HandlerMethod lookupHandlerMethod(TelegramRequest telegramRequest) {
        for (RequestMappingInfo requestMappingInfo : mappingLookup.keySet()) {
            if (!requestMappingInfo.getMessageTypes().contains(telegramRequest.getMessageType())
                    && !requestMappingInfo.getMessageTypes().contains(MessageType.ANY)) {
                continue;
            }

            if (requestMappingInfo.getPatterns().isEmpty()) {
                return mappingLookup.get(requestMappingInfo);
            }

            List<String> matches = requestMappingInfo.getMatchingPatterns(telegramRequest.getText());
            if (!matches.isEmpty()) {
                Map<String, String> variables = requestMappingInfo.getPathMatcher().extractUriTemplateVariables(matches.get(0), telegramRequest.getText());
                telegramRequest.setBasePattern(matches.get(0));
                telegramRequest.setTemplateVariables(variables);
                return mappingLookup.get(requestMappingInfo);
            }
        }
        return null;
    }

    public void registerController(Object bean, Method method, RequestMappingInfo mappingInfo) {
        HandlerMethod handlerMethod = new HandlerMethod(bean, method);
        mappingLookup.put(mappingInfo, handlerMethod);
    }
}
