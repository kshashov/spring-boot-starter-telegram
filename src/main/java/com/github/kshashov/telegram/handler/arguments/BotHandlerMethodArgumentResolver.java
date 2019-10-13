package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import org.springframework.core.MethodParameter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * The implementation of the interface should be able to handle the arguments of the method that processes the telegram
 * request.
 */
public interface BotHandlerMethodArgumentResolver {

    /**
     * Whether the given {@linkplain MethodParameter method parameter} is
     * supported by this resolver.
     * @param methodParameter the method parameter to check
     * @return {@code true} if this resolver supports the supplied parameter;
     * {@code false} otherwise
     */
    boolean supportsParameter(@NotNull MethodParameter methodParameter);

    /**
     * Resolve the current method parameter Resolves a method parameter into an argument value from a given request.
     *
     * @param parameter       the method parameter to resolve. This parameter must have previously been passed to {@link
     *                        #supportsParameter} which must have returned {@code true}.  
     * @param telegramRequest the current telegram request
     * @param telegramSession the current session   
     * @return the resolved argument value, or {@code null} if not resolvable
     */
    @Nullable
    Object resolveArgument(@NotNull MethodParameter parameter, @NotNull TelegramRequest telegramRequest, @NotNull TelegramSession telegramSession);
}
