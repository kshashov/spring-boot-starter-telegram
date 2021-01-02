package com.github.kshashov.telegram.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Helper service that processes {@link List} of {@link Update} updates.
 */
public interface TelegramUpdatesHandler {

    /**
     * Processes updates and sends the processing result to the Telegram.
     *
     * @param token   token
     * @param bot     bot
     * @param updates telegram updates
     */
    void processUpdates(@NotNull String token, @NotNull TelegramBot bot, @NotNull List<Update> updates);
}
