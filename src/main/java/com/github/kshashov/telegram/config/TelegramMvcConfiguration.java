package com.github.kshashov.telegram.config;

/**
 * Интерфейс конфигурации бота
 */
public interface TelegramMvcConfiguration {
    void configuration(TelegramBotBuilder telegramBotBuilder);
}
