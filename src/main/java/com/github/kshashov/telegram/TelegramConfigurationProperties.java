package com.github.kshashov.telegram;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramConfigurationProperties {

    /**
     * Core pool size for default pool executor.
     */
    private int corePoolSize = 15;

    /**
     * Max pool size for default pool executor.
     */
    private int maxPoolSize = 50;

    /**
     * Cache expiration time for the all beans inside {@link TelegramScope}.
     */
    private int sessionSeconds = 3600;

    /**
     * Timeout between requests to Telegrams API if long polling is enabled.
     */
    private long updateListenerSleep = 300L;

    /**
     * HTTP port that will be used to start embedded web server if webhooks is enabled.
     */
    private int serverPort = 8443;
}

