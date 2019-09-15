package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.TelegramRequestResult;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

/**
 * Add support for {@link BaseRequest} return type
 */
@Component
public class BotBaseRequestMethodProcessor implements BotHandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return BaseRequest.class.isAssignableFrom(paramType);
    }

    @Override
    public TelegramRequestResult handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) throws Exception {
        Class<?> paramType = returnType.getParameterType();
        TelegramRequestResult result = new TelegramRequestResult();
        if (BaseRequest.class.isAssignableFrom(paramType)) {
            if (!paramType.isInstance(returnValue)) {
                throw new IllegalStateException(
                        "Current request is not of type [" + paramType.getName() + "]: " + telegramRequest);
            }
            result.setBaseRequest((BaseRequest) returnValue);
        }

        return result;
    }
}