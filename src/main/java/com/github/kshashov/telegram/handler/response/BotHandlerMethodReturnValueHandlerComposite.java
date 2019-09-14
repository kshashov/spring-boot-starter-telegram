package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.api.TelegramRequest;
import org.springframework.core.MethodParameter;

import java.util.ArrayList;
import java.util.List;

public class BotHandlerMethodReturnValueHandlerComposite implements BotHandlerMethodReturnValueHandler {

    private final List<BotHandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return getReturnValueHandler(returnType) != null;
    }

    private BotHandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
        for (BotHandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
            if (handler.supportsReturnType(returnType)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Iterate over registered {@link BotHandlerMethodReturnValueHandler}s and invoke the one that supports it.
     *
     * @throws IllegalStateException if no suitable {@link BotHandlerMethodReturnValueHandler} is found.
     */
    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) throws Exception {
        BotHandlerMethodReturnValueHandler handler = selectHandler(returnValue, returnType);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown return value type: " + returnType.getParameterType().getName());
        }
        handler.handleReturnValue(returnValue, returnType, telegramRequest);
    }

    private BotHandlerMethodReturnValueHandler selectHandler(Object value, MethodParameter returnType) {
        for (BotHandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
            if (handler.supportsReturnType(returnType)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Add the given {@link BotHandlerMethodReturnValueHandler}s.
     * @param handlers Список ресолверов
     * @return Композитный ресолвер
     */
    public BotHandlerMethodReturnValueHandlerComposite addHandlers(List<? extends BotHandlerMethodReturnValueHandler> handlers) {
        if (handlers != null) {
            this.returnValueHandlers.addAll(handlers);
        }
        return this;
    }

}
