package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.TelegramScope;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;

/**
 * Dispatcher which is used to finds the handler for the current telegram request and invokes it
 */
@Slf4j
public class RequestDispatcher {
    private final HandlerMethodContainer handlerMethodContainer;
    private final HandlerAdapter handlerAdapter;
    private final TaskExecutor taskExecutor;

    @Autowired
    private TelegramSession telegramSession;

    public RequestDispatcher(HandlerMethodContainer handlerMethodContainer,
                             HandlerAdapter handlerAdapter,
                             TaskExecutor taskExecutor) {
        this.handlerMethodContainer = handlerMethodContainer;
        this.handlerAdapter = handlerAdapter;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Finds the {@code HandlerMethod} request handler and invokes it, then sends the response to the telegram   
     * @param update User request      
     * @param telegramBot which bot accepted the request
     */
    @SuppressWarnings("unchecked")
    public void execute(Update update, TelegramBot telegramBot) {
        taskExecutor.execute(() -> {
            TelegramRequest telegramRequest = new TelegramRequest(update, telegramBot);

            HandlerMethod handlerMethod = handlerMethodContainer.lookupHandlerMethod(telegramRequest);
            if (handlerMethod == null) {
                log.debug("Not found controller for {} type {}", telegramRequest.getText(), telegramRequest.getMessageType());
                return;
            }

            TelegramScope.setIdThreadLocal(getSessionIdForRequest(telegramRequest));

            BaseRequest baseRequest;
            TelegramRequestResult requestResult = new TelegramRequestResult();
            try {
                baseRequest = handlerAdapter.handle(handlerMethod, telegramRequest, telegramSession);
                requestResult.setBaseRequest(baseRequest);
                if (baseRequest != null) {
                    log.debug("Request {}", baseRequest);
                    telegramBot.execute(baseRequest, new Callback<BaseRequest, BaseResponse>() {
                        @Override
                        public void onResponse(BaseRequest request, BaseResponse response) {
                            requestResult.setBaseResponse(response);
                        }

                        @Override
                        public void onFailure(BaseRequest request, IOException e) {
                            log.error("Send request callback {}", telegramRequest.getChat().id(), e);
                            requestResult.setError(e);
                        }
                    });
                    TelegramScope.removeId();
                } else {
                    log.debug("handlerAdapter return null");
                }
            } catch (Exception e) {
                log.info("Execute error handlerAdapter {}", handlerAdapter, e);
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
