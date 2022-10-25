package com.github.kshashov.telegram.handler;


import com.github.kshashov.telegram.config.TelegramBotProperties;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Service used to listen for telegram events via local http server and process them with {@link TelegramUpdatesHandler} instance.
 */
@Slf4j
public class TelegramWebhookService implements TelegramService {
    private final TelegramBot telegramBot;
    private final TelegramBotProperties botProperties;
    private final TelegramUpdatesHandler updatesHandler;
    private final Javalin server;

    public TelegramWebhookService(@NotNull TelegramBotProperties botProperties, TelegramBot bot, @NotNull TelegramUpdatesHandler updatesHandler, @NotNull Javalin server) {
        this.botProperties = botProperties;
        this.updatesHandler = updatesHandler;
        this.server = server;
        this.telegramBot = bot;
    }

    /**
     * Subscribe on {@link TelegramBot} events and process them with {@link TelegramUpdatesHandler}.
     */
    @Override
    public void start() {
        String endpoint = getEndpoint(botProperties.getWebhook());
        String url = (String) botProperties.getWebhook().getParameters().get("url");

        try {
            BaseResponse response = telegramBot.execute(botProperties.getWebhook());
            if (!response.isOk()) throw new IllegalStateException();
            log.info("Webhook '{}' has been enabled", url);
        } catch (Exception ex) {
            log.error("Webhook '{}' couldn't be enabled", url);
            throw ex;
        }

        try {
            registerEndpoint(endpoint);
            log.info("Endpoint '{}' has been created", endpoint);
        } catch (Exception ex) {
            log.error("An unexpected error occured while adding webhook endpoint", ex);
            throw ex;
        }
    }

    private void registerEndpoint(String endpoint) {
        server.post(endpoint, context -> {
            log.info("endpoint triggered:");
            log.info(context.body());
            List<Update> updates = null;
            try {
                Update update = BotUtils.parseUpdate(context.body());
                updates = Collections.singletonList(update);
            } catch (Exception ex) {
                log.error("Telegram updates can't be parsed for '{}' webhook", endpoint);
            }
            if (updates != null) {
                updatesHandler.processUpdates(botProperties.getToken(), telegramBot, updates);
            }
        });
    }

    /**
     * Add /uuid path to webhook url
     *
     * @param setWebhook webhook configuration
     * @return endpoint url
     */
    private String getEndpoint(SetWebhook setWebhook) {
        String url = (String) setWebhook.getParameters().get("url");
        String uuid = UUID.randomUUID().toString();
        try {
            String path = new URL(new URL(url), uuid).toString();
            setWebhook.url(path);
        } catch (Exception ex) {
            log.error("Webhook url '{}' can't be parsed", url);
        }

        return "/" + uuid;
    }

    /**
     * Unsubscribe from {@link TelegramBot} events.
     */
    @Override
    public void stop() {
        if (!botProperties.isKeepWebhookRegistration()) {
            log.info("Webhook has been deleted");
            telegramBot.execute(new DeleteWebhook());
        }
    }
}
