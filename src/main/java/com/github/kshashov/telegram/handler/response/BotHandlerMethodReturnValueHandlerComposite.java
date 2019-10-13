package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves method parameters by delegating to a list of registered {@link BotHandlerMethodReturnValueHandler}
 * handlers.
 */
@Slf4j
public class BotHandlerMethodReturnValueHandlerComposite implements BotHandlerMethodReturnValueHandler {

    private final List<BotHandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();

    /**
     * Create with the given {@link BotHandlerMethodReturnValueHandler}s.
     *
     * @param handlers handlers to add
     */
    public BotHandlerMethodReturnValueHandlerComposite(@NotNull List<BotHandlerMethodReturnValueHandler> handlers) {
        returnValueHandlers.addAll(handlers);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return getReturnValueHandler(returnType) != null;
    }

    private BotHandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
        for (BotHandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
            if (log.isTraceEnabled()) {
                log.trace("Testing if response resolver [" + handler + "] supports [" + returnType.getGenericParameterType() + "]");
            }
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
    public BaseRequest handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) {
        BotHandlerMethodReturnValueHandler handler = getReturnValueHandler(returnType);
        if (handler == null) {
            log.error("Unknown return value type: " + returnType.getParameterType().getName());
            return null;
        }
        return handler.handleReturnValue(returnValue, returnType, telegramRequest);
    }
}
