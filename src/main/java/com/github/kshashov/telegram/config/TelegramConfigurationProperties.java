package com.github.kshashov.telegram.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramConfigurationProperties {

    /**
     * Core pool size for default pool executor
     */
    @Min(1)
    private int corePoolSize = 15;

    /**
     * Max pool size for default pool executor
     */
    @Min(1)
    private int maxPoolSize = 200;

    /**
     * Cache expiration time for the all beans inside {@link TelegramScope}
     */
    @Min(1)
    private int sessionSeconds = 3600;
}

