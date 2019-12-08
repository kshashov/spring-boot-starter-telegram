package com.github.kshashov.telegram.autoconfigure;

import com.github.kshashov.telegram.autoconfigure.session.TelegramScope;
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
    private int maxPoolSize = 200;

    /**
     * Cache expiration time for the all beans inside {@link TelegramScope}.
     */
    private int sessionSeconds = 3600;
}

