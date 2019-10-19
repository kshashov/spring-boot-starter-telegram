package com.github.kshashov.telegram.handler;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import lombok.extern.slf4j.Slf4j;

/**
 * Service which is used to start {@link TelegramBot} and subscribe the {@link RequestDispatcher} to its updates
 */
@Slf4j
public class TelegramService {
    private final TelegramBot telegramBot;
    private final RequestDispatcher botRequestDispatcher;

    public TelegramService(TelegramBot telegramBot, RequestDispatcher botRequestDispatcher) {
        this.telegramBot = telegramBot;
        this.botRequestDispatcher = botRequestDispatcher;
    }

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
