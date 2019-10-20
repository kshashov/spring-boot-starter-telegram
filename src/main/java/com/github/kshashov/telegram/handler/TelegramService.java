package com.github.kshashov.telegram.handler;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import lombok.extern.slf4j.Slf4j;

/**
 * Service used to process the telegram events with {@link RequestDispatcher} instance
 */
@Slf4j
public class TelegramService {
    private final TelegramBot telegramBot;
    private final RequestDispatcher botRequestDispatcher;

    public TelegramService(TelegramBot telegramBot, RequestDispatcher botRequestDispatcher) {
        this.telegramBot = telegramBot;
        this.botRequestDispatcher = botRequestDispatcher;
    }

    /**
     * Subscribe on {@link TelegramBot} events instance and process them with {@link RequestDispatcher}.
     */
    public void start() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    botRequestDispatcher.execute(update, telegramBot);
                } catch (Exception e) {
                    log.error("{}", e.getMessage(), e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, new GetUpdates());
    }

}
