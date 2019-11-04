package com.github.kshashov.telegram.handler;


import com.github.kshashov.telegram.config.TelegramBotProperties;
import com.github.kshashov.telegram.handler.processor.RequestDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

/**
 * Service used to process the telegram events with {@link RequestDispatcher} instance
 */
@Slf4j
public class TelegramService {
    private final TelegramBot telegramBot;
    private final RequestDispatcher botRequestDispatcher;
    private final TelegramBotProperties botProperties;

    public TelegramService(@NotNull TelegramBotProperties botProperties, @NotNull RequestDispatcher requestDispatcher) {
        this.botProperties = botProperties;
        this.telegramBot = new TelegramBot.Builder(botProperties.getToken())
                .okHttpClient(botProperties.getOkHttpClient())
                .updateListenerSleep(botProperties.getListenerSleep())
                .apiUrl(botProperties.getUrl())
                .build();
        this.botRequestDispatcher = requestDispatcher;

    }

    /**
     * Subscribe on {@link TelegramBot} events instance and process them with {@link RequestDispatcher}.
     */
    public void start() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    botRequestDispatcher.execute(botProperties.getToken(), update, telegramBot);
                } catch (Exception e) {
                    log.error("{}", e.getMessage(), e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, new GetUpdates());
    }
}
