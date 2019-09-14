package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.BotPathVariable;
import com.github.kshashov.telegram.api.TelegramRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

@Component
public class BotRequestMethodPathArgumentResolver implements BotHandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return parameter.hasParameterAnnotation(BotPathVariable.class)
                && String.class.isAssignableFrom(paramType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, TelegramRequest telegramRequest) {
        Class<?> paramType = parameter.getParameterType();

        if (String.class.isAssignableFrom(paramType)) {
            if (telegramRequest.getTemplateVariables() != null) {
                BotPathVariable annotation = parameter.getParameterAnnotation(BotPathVariable.class);
                if (annotation != null) {
                    String value = telegramRequest.getTemplateVariables().get(annotation.value());
                    if (!paramType.isInstance(value)) {
                        throw new IllegalStateException(
                                "Current request is not of type [" + paramType.getName() + "]: " + value + "");
                    }
                    return value;
                }
            }
        }

        return null;
    }

}
