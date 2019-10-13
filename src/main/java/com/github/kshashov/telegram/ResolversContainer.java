package com.github.kshashov.telegram;

import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;

import java.util.Collections;
import java.util.List;

/**
 * Stores arguments resolvers and return value handlers
 *
 * @see BotHandlerMethodArgumentResolver
 * @see BotHandlerMethodReturnValueHandler
 */
public interface ResolversContainer {

    default List<BotHandlerMethodArgumentResolver> getArgumentResolvers() {
        return Collections.emptyList();
    }

    default List<BotHandlerMethodReturnValueHandler> getReturnValueHandlers() {
        return Collections.emptyList();
    }
}
