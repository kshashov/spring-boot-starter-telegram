package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import org.springframework.core.MethodParameter;

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
    boolean supportsParameter(MethodParameter methodParameter);

    /**
     * Resolve the current method parameter Resolves a method parameter into an argument value from a given request.
     *
     * @param parameter       the method parameter to resolve. This parameter must have previously been passed to {@link
     *                        #supportsParameter} which must have returned {@code true}.  
     * @param telegramRequest the current telegram request
     * @param telegramSession the current session   
     * @return the resolved argument value, or {@code null} if not resolvable
     * @throws Exception in case of errors with the preparation of argument values
     */
    Object resolveArgument(MethodParameter parameter, TelegramRequest telegramRequest, TelegramSession telegramSession) throws Exception;
}
