package com.github.kshashov.telegram.autoconfigure.api;


import com.github.kshashov.telegram.handler.config.TelegramBotGlobalProperties;

/**
 * Designed to support code-based configuration of the telegram mvc.
 */
@FunctionalInterface
public interface TelegramBotGlobalPropertiesConfiguration {

    /**
     * Add custom settings to preconfigured builder.
     *
     * @param builder is preconfigured builder
     */
    void configure(TelegramBotGlobalProperties.Builder builder);
}
