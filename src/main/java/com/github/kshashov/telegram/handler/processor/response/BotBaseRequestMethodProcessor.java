package com.github.kshashov.telegram.handler.processor.response;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;

/**
 * Add support for {@link BaseRequest} return type.
 */
@Slf4j
public class BotBaseRequestMethodProcessor implements BotHandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return BaseRequest.class.isAssignableFrom(paramType);
    }

    @Override
    public BaseRequest handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) {
        Class<?> paramType = returnType.getParameterType();
        if (BaseRequest.class.isAssignableFrom(paramType)) {
            if (paramType.isInstance(returnValue)) {
                return (BaseRequest) returnValue;
            } else {
                log.error("Current request is not of type [" + paramType.getName() + "]: " + telegramRequest);
            }
        }

        return null;
    }
}
