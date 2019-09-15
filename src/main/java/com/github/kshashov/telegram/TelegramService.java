package com.github.kshashov.telegram;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service which is used to start {@link TelegramBot} and subscribe the {@link RequestDispatcher} to its updates
 */
public class TelegramService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
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
                    logger.error("{}", e.getMessage(), e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, new GetUpdates());
    }

}
