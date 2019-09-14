package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.api.TelegramRequest;
import org.springframework.core.MethodParameter;

/**
 * Имплементация интерфейса умеет преобразовывать возвращаемый параметр, заполняет в {@link TelegramRequest} параметр baseRequest
 */
public interface BotHandlerMethodReturnValueHandler {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) throws Exception;
}
