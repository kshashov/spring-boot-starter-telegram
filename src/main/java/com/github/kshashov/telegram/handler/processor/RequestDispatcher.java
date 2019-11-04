package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.TelegramSessionResolver;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * Dispatcher which is used to finds the handler for the current telegram request and invokes it
 */
@Slf4j
@Getter
public class RequestDispatcher {
    private final HandlerMethodContainer handlerMethodContainer;
    private final TelegramBotGlobalProperties botGlobalProperties;
    private final TelegramSessionResolver sessionResolver;

    public RequestDispatcher(@NotNull HandlerMethodContainer handlerMethodContainer, @NotNull TelegramBotGlobalProperties botGlobalProperties, @NotNull TelegramSessionResolver sessionResolver) {
        this.handlerMethodContainer = handlerMethodContainer;
        this.botGlobalProperties = botGlobalProperties;
        this.sessionResolver = sessionResolver;
    }

    /**
     * Finds the {@code HandlerMethod} request handler and invokes it, then sends the response to the telegram   
     *
     * @param token       Telegram bot token
     * @param update      User request      
     * @param telegramBot which bot accepted the request
     */
    public void execute(String token, @NotNull Update update, @NotNull TelegramBot telegramBot) {
        botGlobalProperties.getTaskExecutor().execute(() -> {
            TelegramSessionResolver.TelegramSessionHolder sessionHolder = null;

            try {
                // Process initial telegram request by controller
                TelegramEvent event = new TelegramEvent(token, update, telegramBot);
                sessionHolder = sessionResolver.resolveTelegramSession(event);
                BaseRequest baseRequest = doExecute(event, sessionHolder.getSession());

                if (baseRequest != null) {
                    // Execute telegram request from controller response
                    log.debug("Controller returned Telegram request {}", baseRequest);
                    postExecute(baseRequest, telegramBot);
                }
            } catch (Exception e) {
                log.error("Execution error", e);
            } finally {
                // Clear session id from current scope
                if (sessionHolder != null) sessionHolder.releaseSessionId();
            }
        });
    }

    private BaseRequest doExecute(@NotNull TelegramEvent event, @NotNull TelegramSession session) throws Exception {
        HandlerMethodContainer.HandlerLookupResult lookupResult = handlerMethodContainer.lookupHandlerMethod(event);
        if (!lookupResult.hasResolvedMethod()) {
            log.warn("Not found controller for {} (type {})", event.getText(), event.getMessageType());
            return null;
        }

        TelegramRequest telegramRequest = new TelegramRequest(
                event.getTelegramBot(), event.getUpdate(), event.getMessageType(), lookupResult.getBasePattern(), lookupResult.getTemplateVariables(), event.getMessage(), event.getText(), event.getChat(), event.getUser());

        return new TelegramInvocableHandlerMethod(lookupResult.getHandlerMethod(), botGlobalProperties.getArgumentResolvers(), botGlobalProperties.getReturnValueHandlers())
                .invokeAndHandle(telegramRequest, session);
    }

    @SuppressWarnings("unchecked")
    private void postExecute(@NotNull BaseRequest baseRequest, @NotNull TelegramBot telegramBot) {
        telegramBot.execute(baseRequest, new Callback<BaseRequest, BaseResponse>() {
            @Override
            public void onResponse(BaseRequest request, BaseResponse response) {
                botGlobalProperties.getResponseCallback().onResponse(request, response);
                log.debug("Request was successfully executed");
            }

            @Override
            public void onFailure(BaseRequest request, IOException e) {
                botGlobalProperties.getResponseCallback().onFailure(request, e);
                log.error("Request was failed", e);
            }
        });
    }
}
