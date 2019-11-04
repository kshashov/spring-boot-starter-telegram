package com.github.kshashov.telegram.handler.processor.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Add support for {@link String} arguments marked by {@link BotPathVariable} annotation
 */
@Slf4j
public class BotRequestMethodPathArgumentResolver implements BotHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return parameter.hasParameterAnnotation(BotPathVariable.class)
                && (String.class.isAssignableFrom(paramType)
                || Integer.class.isAssignableFrom(paramType)
                || Long.class.isAssignableFrom(paramType)
                || Double.class.isAssignableFrom(paramType)
                || Float.class.isAssignableFrom(paramType)
                || BigInteger.class.isAssignableFrom(paramType)
                || BigDecimal.class.isAssignableFrom(paramType));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, TelegramRequest telegramRequest, TelegramSession telegramSession) {
        Class<?> paramType = parameter.getParameterType();
        BotPathVariable annotation = parameter.getParameterAnnotation(BotPathVariable.class);
        if ((telegramRequest.getTemplateVariables() == null) || (annotation == null)) {
            // nothing to extract
            return null;
        }

        String value = telegramRequest.getTemplateVariables().get(annotation.value());
        if (value == null) {
            return null;
        }

        try {
            if (String.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, value);
            } else if (Integer.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, new Integer(value));
            } else if (Long.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, new Long(value));
            } else if (Double.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, new Double(value));
            } else if (Float.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, new Float(value));
            } else if (BigInteger.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, new BigInteger(value));
            } else if (BigDecimal.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, new BigDecimal(value));
            }
        } catch (NumberFormatException ex) {

        }

        return null;
    }

    private Object validateValue(Class<?> paramType, Object value) {
        if (value != null && !paramType.isInstance(value)) {
            log.error("Current request is not of type [" + paramType.getName() + "]: " + value + "");
            return null;
        }

        return value;
    }
}
