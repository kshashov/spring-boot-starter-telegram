package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.TelegramScope;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;

import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * Dispatcher which is used to finds the handler for the current telegram request and invokes it
 */
@Slf4j
public class RequestDispatcher {
    private final HandlerMethodContainer handlerMethodContainer;
    private final TelegramBotGlobalProperties botGlobalProperties;

    @Autowired
    private ApplicationContext context;

    public RequestDispatcher(@NotNull HandlerMethodContainer handlerMethodContainer, @NotNull TelegramBotGlobalProperties botGlobalProperties) {
        this.handlerMethodContainer = handlerMethodContainer;
        this.botGlobalProperties = botGlobalProperties;
    }

    /**
     * Finds the {@code HandlerMethod} request handler and invokes it, then sends the response to the telegram   
     *
     * @param update      User request      
     * @param telegramBot which bot accepted the request
     */
    public void execute(@NotNull Update update, @NotNull TelegramBot telegramBot) {
        botGlobalProperties.getTaskExecutor().execute(() -> {
            TelegramRequest telegramRequest = new TelegramRequest(update, telegramBot);

            HandlerMethod handlerMethod = handlerMethodContainer.lookupHandlerMethod(telegramRequest);
            if (handlerMethod == null) {
                log.warn("Not found controller for {} type {}", telegramRequest.getText(), telegramRequest.getMessageType());
                return;
            }

            TelegramScope.setIdThreadLocal(getSessionIdForRequest(telegramRequest));

            try {
                BaseRequest baseRequest = new TelegramInvocableHandlerMethod(handlerMethod, botGlobalProperties.getArgumentResolvers(), botGlobalProperties.getReturnValueHandlers())
                        .invokeAndHandle(telegramRequest, context.getBean(TelegramSession.class));
                if (baseRequest != null) {
                    log.debug("Request {}", baseRequest);
                    postExecute(baseRequest, telegramBot);
                    TelegramScope.removeId();
                } else {
                    log.warn("handlerAdapter return null");
                }
            } catch (Exception e) {
                log.error("Execute error", e);
            }
        });
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


    private Long getSessionIdForRequest(TelegramRequest telegramRequest) {
        if (telegramRequest.getChat() != null) {
            return telegramRequest.getChat().id();
        } else if (telegramRequest.getUser() != null) {
            return Long.valueOf(telegramRequest.getUser().id());
        } else {
            // We are sure that update object could not be null
            return Long.valueOf(telegramRequest.getUpdate().updateId());
        }
    }
}
