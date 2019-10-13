package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import org.springframework.core.MethodParameter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Strategy interface for resolving method parameters into argument values in the context of a given telegram request.
 */
public interface BotHandlerMethodReturnValueHandler {
    /**
     * Whether the given {@linkplain MethodParameter method return type} is supported by this handler.
     *
     * @param returnType the method return type to check
     * @return {@code true} if this handler supports the supplied return type; {@code false} otherwise
     */
    boolean supportsReturnType(@NotNull MethodParameter returnType);

    /**
     * Resolves a method parameter into an argument value from a given request.
     *
     * @param returnValue     method result to handle
     * @param returnType      the method parameter to resolve. This parameter must have previously been passed to {@link
     *                        #supportsReturnType} which must have returned {@code true}.
     * @param telegramRequest the current telegram request
     * @return the resolved argument value, or {@code null} if not resolvable
     */
    @Nullable
    BaseRequest handleReturnValue(@Nullable Object returnValue, @NotNull MethodParameter returnType, @NotNull TelegramRequest telegramRequest);
}
