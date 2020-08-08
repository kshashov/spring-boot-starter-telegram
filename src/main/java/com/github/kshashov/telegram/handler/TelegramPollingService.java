package com.github.kshashov.telegram.handler;


import com.github.kshashov.telegram.config.TelegramBotProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.GetUpdates;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

/**
 * Service used to listen for telegram events via polling and process them with {@link TelegramUpdatesHandler} instance.
 */
@Slf4j
public class TelegramPollingService implements TelegramService {
    private final TelegramBot telegramBot;
    private final TelegramBotProperties botProperties;
    private final TelegramUpdatesHandler updatesHandler;

    public TelegramPollingService(@NotNull TelegramBotProperties botProperties, @NotNull TelegramUpdatesHandler updatesHandler) {
        this.botProperties = botProperties;
        this.updatesHandler = updatesHandler;
        this.telegramBot = botProperties.getBotBuilder().build();
    }

    /**
     * Subscribe on {@link TelegramBot} events and process them with {@link TelegramUpdatesHandler}.
     */
    @Override
    public void start() {
        // Make sure that webhook is disabled
        telegramBot.execute(new DeleteWebhook());

        telegramBot.setUpdatesListener(updates -> {
            updatesHandler.processUpdates(botProperties.getToken(), telegramBot, updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, new GetUpdates());
    }

    /**
     * Unsubscribe from {@link TelegramBot} events.
     */
    @Override
    public void stop() {
        telegramBot.removeGetUpdatesListener();
    }
}
