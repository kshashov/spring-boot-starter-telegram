package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.handler.processor.RequestDispatcher;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import com.github.kshashov.telegram.metrics.MetricsService;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * Helper service that processes {@link List} of {@link Update} updates with {@link RequestDispatcher}.
 */
@Slf4j
@Component
public class TelegramUpdatesHandler {
    private final RequestDispatcher botRequestDispatcher;
    private final TelegramBotGlobalProperties globalProperties;
    private final MetricsService metricsService;

    @Autowired
    public TelegramUpdatesHandler(@NotNull RequestDispatcher botRequestDispatcher, @NotNull TelegramBotGlobalProperties globalProperties, @NotNull MetricsService metricsService) {
        this.botRequestDispatcher = botRequestDispatcher;
        this.globalProperties = globalProperties;
        this.metricsService = metricsService;
    }

    /**
     * Processes updates with {@link RequestDispatcher}. Sends the processing result to the Telegram.
     *
     * @param token   token
     * @param bot     bot
     * @param updates telegram updates
     */
    public void processUpdates(@NotNull String token, @NotNull TelegramBot bot, @NotNull List<Update> updates) {
        metricsService.onUpdatesReceived(updates.size());
        try {
            for (Update update : updates) {
                globalProperties.getTaskExecutor().execute(() -> {
                    try {
                        TelegramEvent event = new TelegramEvent(token, update, bot);

                        BaseRequest executionResult = botRequestDispatcher.execute(event);
                        if (executionResult != null) {
                            // Execute telegram request from controller response
                            log.debug("Controller returned Telegram request {}", executionResult);
                            postExecute(executionResult, bot);
                        }
                    } catch (IllegalStateException e) {
                        metricsService.onUpdateError();
                        log.error("Execution error", e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("An unhandled exception occurred while processing the Telegram request", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void postExecute(@NotNull BaseRequest baseRequest, @NotNull TelegramBot telegramBot) {
        telegramBot.execute(baseRequest, new Callback<BaseRequest, BaseResponse>() {
            @Override
            public void onResponse(BaseRequest request, BaseResponse response) {
                globalProperties.getResponseCallback().onResponse(request, response);
                log.debug("{} request was successfully executed", baseRequest);
            }

            @Override
            public void onFailure(BaseRequest request, IOException e) {
                globalProperties.getResponseCallback().onFailure(request, e);
                metricsService.onUpdateError();
                log.error(baseRequest + " request was failed", e);
            }
        });
    }
}
