package com.github.kshashov.telegram.config;


/**
 * Designed to support code-based configuration of the telegram mvc.
 */
public interface TelegramBotGlobalPropertiesConfiguration {

    /**
     * Add custom settings to preconfigured builder
     *
     * @param builder is preconfigured builder
     */
    void configure(TelegramBotGlobalProperties.Builder builder);
}
