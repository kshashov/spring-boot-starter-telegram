package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.TelegramSessionResolver;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.processor.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.response.BotHandlerMethodReturnValueHandler;
import com.pengrad.telegrambot.request.BaseRequest;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

/**
 * Dispatcher which is used to finds the handler for the current telegram request and invokes it.
 */
@Slf4j
public class RequestDispatcher {
    private final HandlerMethodContainer handlerMethodContainer;
    private final TelegramSessionResolver sessionResolver;
    private final BotHandlerMethodArgumentResolver argumentResolver;
    private final BotHandlerMethodReturnValueHandler returnValueHandler;

    public RequestDispatcher(@NotNull HandlerMethodContainer handlerMethodContainer, @NotNull TelegramSessionResolver sessionResolver, @NotNull BotHandlerMethodArgumentResolver argumentResolver, @NotNull BotHandlerMethodReturnValueHandler returnValueHandler) {
        this.handlerMethodContainer = handlerMethodContainer;
        this.sessionResolver = sessionResolver;
        this.argumentResolver = argumentResolver;
        this.returnValueHandler = returnValueHandler;
    }

    /**
     * Finds the {@code HandlerMethod} request handler and invokes it.
     *
     * @param event Telegram event
     * @return invocation result
     * @throws IllegalStateException when it failed to execute the handler method correctly
     */
    public BaseRequest execute(@NotNull TelegramEvent event) throws IllegalStateException {
        TelegramSessionResolver.TelegramSessionHolder sessionHolder = null;

        try {
            // Start telegram session
            sessionHolder = sessionResolver.resolveTelegramSession(event);
            // Process telegram request by controller
            HandlerMethodContainer.HandlerLookupResult lookupResult = handlerMethodContainer.lookupHandlerMethod(event);
            if (lookupResult.getHandlerMethod() == null) {
                log.debug("Not found controller for {} (type {})", event.getText(), event.getMessageType());
                return null;
            }
            return doExecute(event, lookupResult, sessionHolder.getSession());
        } finally {
            // Clear session id from current scope
            if (sessionHolder != null) sessionHolder.releaseSessionId();
        }
    }

    private BaseRequest doExecute(@NotNull TelegramEvent event, @NotNull HandlerMethodContainer.HandlerLookupResult lookupResult, @NotNull TelegramSession session) throws IllegalStateException {
        TelegramRequest request = new TelegramRequest(
                event.getTelegramBot(),
                event.getUpdate(),
                event.getMessageType(),
                lookupResult.getBasePattern(),
                lookupResult.getTemplateVariables(),
                event.getMessage(),
                event.getText(),
                event.getChat(),
                event.getUser());

        BaseRequest result = new TelegramInvocableHandlerMethod(lookupResult.getHandlerMethod(), argumentResolver, returnValueHandler)
                .invokeAndHandle(request, session);

        log.info("{} request has been executed by '{}' handler method with {} result",
                event.getMessageType(),
                lookupResult.getHandlerMethod().toString(),
                result == null ? "null" : result.getClass().getSimpleName());

        return result;
    }


}
