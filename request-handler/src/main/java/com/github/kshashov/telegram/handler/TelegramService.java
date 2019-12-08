package com.github.kshashov.telegram.handler;


import com.github.kshashov.telegram.handler.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.handler.config.TelegramBotProperties;
import com.github.kshashov.telegram.handler.processor.RequestDispatcher;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * Service used to process the telegram events with {@link RequestDispatcher} instance.
 */
@Slf4j
public class TelegramService {
    private final TelegramBot telegramBot;
    private final RequestDispatcher botRequestDispatcher;
    private final TelegramBotGlobalProperties globalProperties;
    private final TelegramBotProperties botProperties;

    public TelegramService(@NonNull TelegramBotGlobalProperties globalProperties, @NotNull TelegramBotProperties botProperties, @NotNull RequestDispatcher requestDispatcher) {
        this.globalProperties = globalProperties;
        this.botProperties = botProperties;
        this.telegramBot = new TelegramBot.Builder(botProperties.getToken())
                .okHttpClient(botProperties.getOkHttpClient())
                .updateListenerSleep(botProperties.getListenerSleep())
                .apiUrl(botProperties.getUrl())
                .build();
        this.botRequestDispatcher = requestDispatcher;
    }

    /**
     * Subscribe on {@link TelegramBot} events instance and process them with {@link RequestDispatcher}. Sends the processing result to the Telegram.
     */
    public void start() {
        telegramBot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    globalProperties.getTaskExecutor().execute(() -> {
                        try {
                            TelegramEvent event = new TelegramEvent(botProperties.getToken(), update, telegramBot);
                            BaseRequest executionResult = botRequestDispatcher.execute(event);
                            if (executionResult != null) {
                                // Execute telegram request from controller response
                                log.debug("Controller returned Telegram request {}", executionResult);
                                postExecute(executionResult, telegramBot);
                            }
                        } catch (IllegalStateException e) {
                            log.error("Execution error", e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("An unhandled exception occurred while processing the Telegram request", e);
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, new GetUpdates());
    }

    @SuppressWarnings("unchecked")
    private void postExecute(@NotNull BaseRequest baseRequest, @NotNull TelegramBot telegramBot) {
        telegramBot.execute(baseRequest, new Callback<BaseRequest, BaseResponse>() {
            @Override
            public void onResponse(BaseRequest request, BaseResponse response) {
                globalProperties.getResponseCallback().onResponse(request, response);
                log.debug("{} request was successfully executed", baseRequest);
            }

            @Override
            public void onFailure(BaseRequest request, IOException e) {
                globalProperties.getResponseCallback().onFailure(request, e);
                log.error(baseRequest + " request was failed", e);
            }
        });
    }
}
