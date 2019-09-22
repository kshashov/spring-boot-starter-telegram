package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves method parameters by delegating to a list of registered {@link BotHandlerMethodArgumentResolver} resolvers.
 */
public class BotHandlerMethodArgumentResolverComposite implements BotHandlerMethodArgumentResolver {

    private static final Logger logger = LoggerFactory.getLogger(BotHandlerMethodArgumentResolverComposite.class);
    private final List<BotHandlerMethodArgumentResolver> argumentResolvers = new LinkedList<>();

    private final Map<MethodParameter, BotHandlerMethodArgumentResolver> argumentResolverCache =
            new ConcurrentHashMap<>(256);

    /**
     * Add the given {@link BotHandlerMethodArgumentResolver}s.
     * @param resolvers to add.
     * @return current object instance
     */
    public BotHandlerMethodArgumentResolverComposite addResolvers(List<? extends BotHandlerMethodArgumentResolver> resolvers) {
        if (resolvers != null) {
            this.argumentResolvers.addAll(resolvers);
        }
        return this;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (getArgumentResolver(parameter) != null);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, TelegramRequest telegramRequest, TelegramSession telegramSession) throws Exception {
        BotHandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
        if (resolver == null) {
            throw new IllegalArgumentException("Unknown parameter type [" + parameter.getParameterType().getName() + "]");
        }
        return resolver.resolveArgument(parameter, telegramRequest, telegramSession);
    }

    /**
     * Find a registered {@link BotHandlerMethodArgumentResolver} that supports the given method parameter.
     * @param parameter for which you need to find the argument resolver
     */
    private BotHandlerMethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
        BotHandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
        if (result == null) {
            for (BotHandlerMethodArgumentResolver methodArgumentResolver : this.argumentResolvers) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Testing if argument resolver [" + methodArgumentResolver + "] supports [" +
                            parameter.getGenericParameterType() + "]");
                }
                if (methodArgumentResolver.supportsParameter(parameter)) {
                    result = methodArgumentResolver;
                    this.argumentResolverCache.put(parameter, result);
                    break;
                }
            }
        }
        return result;
    }
}
