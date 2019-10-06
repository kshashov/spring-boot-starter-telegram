package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolverComposite;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandlerComposite;
import com.pengrad.telegrambot.request.BaseRequest;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

/**
 * Invokes a request handler, prepares method parameters for execution, processes the result object
 *
 * @see BotHandlerMethodArgumentResolver
 * @see BotHandlerMethodReturnValueHandler
 */
public class HandlerAdapter {

    private final BotHandlerMethodArgumentResolverComposite argumentResolvers;
    private final BotHandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public HandlerAdapter(List<BotHandlerMethodArgumentResolver> resolvers, List<BotHandlerMethodReturnValueHandler> handlers) {
        this.argumentResolvers = new BotHandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        this.returnValueHandlers = new BotHandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
    }

    /**
     * Invokes the method
     *
     * @param handlerMethod  method to invoke
     * @param telegramRequest the current telegram request
     * @param telegramSession the current session   
     * @return result of the method invocation
     * @throws Exception in case of errors with the method invocation
     */
    public BaseRequest handle(HandlerMethod handlerMethod, TelegramRequest telegramRequest, TelegramSession telegramSession) throws Exception {
        TelegramInvocableHandlerMethod invocableMethod = new TelegramInvocableHandlerMethod(handlerMethod, this.argumentResolvers, this.returnValueHandlers);
        return invocableMethod.invokeAndHandle(telegramRequest, telegramSession);
    }
}
