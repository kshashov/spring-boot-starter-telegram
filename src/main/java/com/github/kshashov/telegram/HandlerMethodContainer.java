package com.github.kshashov.telegram;


import com.github.kshashov.telegram.api.TelegramRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class HandlerMethodContainer {
    private final Map<RequestMappingInfo, HandlerMethod> mappingLookup = new LinkedHashMap<>();

    public HandlerMethod lookupHandlerMethod(TelegramRequest telegramRequest) {
        for (RequestMappingInfo requestMappingInfo : mappingLookup.keySet()) {
            if(!requestMappingInfo.getMessageTypes().contains(telegramRequest.getMessageType())){
               continue;
            }
            RequestMappingInfo matchingCondition = requestMappingInfo.getMatchingCondition(telegramRequest.getText());
            if (matchingCondition != null) {
                Set<String> patterns = matchingCondition.getPatterns();
                if (!patterns.isEmpty()) {
                    String basePattern = patterns.iterator().next();
                    Map<String, String> templateVariables =
                            matchingCondition.getPathMatcher().extractUriTemplateVariables(basePattern, telegramRequest.getText());
                    telegramRequest.setBasePattern(basePattern);
                    telegramRequest.setTemplateVariables(templateVariables);
                }
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
