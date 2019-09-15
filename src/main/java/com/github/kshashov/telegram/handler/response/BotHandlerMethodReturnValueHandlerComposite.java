package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.TelegramRequestResult;
import com.github.kshashov.telegram.api.TelegramRequest;
import org.springframework.core.MethodParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves method parameters by delegating to a list of registered {@link BotHandlerMethodReturnValueHandler}
 * handlers.
 */
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
    public TelegramRequestResult handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) throws Exception {
        BotHandlerMethodReturnValueHandler handler = selectHandler(returnValue, returnType);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown return value type: " + returnType.getParameterType().getName());
        }
        return handler.handleReturnValue(returnValue, returnType, telegramRequest);
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
     * @param handlers handlers to add
     * @return this object
     */
    public BotHandlerMethodReturnValueHandlerComposite addHandlers(List<? extends BotHandlerMethodReturnValueHandler> handlers) {
        if (handlers != null) {
            this.returnValueHandlers.addAll(handlers);
        }
        return this;
    }

}
