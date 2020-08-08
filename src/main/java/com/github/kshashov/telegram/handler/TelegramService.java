package com.github.kshashov.telegram.handler;

/**
 * Is used to listen for telegram events and process them.
 */
public interface TelegramService {

    /**
     * Subscribe on Telegram events.
     */
    void start();

    /**
     * Unsubscribe from Telegram events.
     */
    void stop();
}
