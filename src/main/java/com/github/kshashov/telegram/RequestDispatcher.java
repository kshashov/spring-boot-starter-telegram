package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.TelegramScope;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;


public class RequestDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

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
     * Находит обработчик запроса пользователя и вызывает его, далее передает в телеграмм ответ обработчика
     * @param update Запрос пользователя
     * @param telegramBot какой бот принял запрос
     */
    @SuppressWarnings("unchecked")
    public void execute(Update update, TelegramBot telegramBot) {
        taskExecutor.execute(() -> {
            TelegramRequest telegramRequest = new TelegramRequest(update, telegramBot);

            HandlerMethod handlerMethod = handlerMethodContainer.lookupHandlerMethod(telegramRequest);
            if (handlerMethod == null) {
                logger.error("Not found controller for {} type {}", telegramRequest.getText(), telegramRequest.getMessageType());
                return;
            }

            TelegramScope.setIdThreadLocal(telegramRequest.chatId());
            telegramRequest.setSession(telegramSession);

            BaseRequest baseRequest;
            try {
                baseRequest = handlerAdapter.handle(telegramRequest, handlerMethod);
                if (baseRequest != null) {
                    logger.debug("Request {}", baseRequest);
                    telegramBot.execute(baseRequest, new Callback<BaseRequest, BaseResponse>() {
                        @Override
                        public void onResponse(BaseRequest request, BaseResponse response) {
                            telegramRequest.complete(response);
                        }

                        @Override
                        public void onFailure(BaseRequest request, IOException e) {
                            logger.error("Send request callback {}", telegramRequest.chatId(), e);
                            telegramRequest.error(e);
                        }
                    });
                    TelegramScope.removeId();
                } else {
                    telegramRequest.complete(null);
                    logger.debug("handlerAdapter return null");
                }
            } catch (Exception e) {
                telegramRequest.error(e);
                logger.info("Execute error handlerAdapter {}", handlerAdapter, e);
            }
        });
    }
}
