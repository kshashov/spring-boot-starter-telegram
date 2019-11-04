package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;

public class TelegramSessionResolver {
    private ApplicationContext context;

    public TelegramSessionResolver(ApplicationContext context) {
        this.context = context;
    }

    @NotNull
    public TelegramSessionHolder resolveTelegramSession(@NotNull TelegramEvent telegramEvent) {
        TelegramScope.setIdThreadLocal(getSessionIdForRequest(telegramEvent));
        return new TelegramSessionHolder(context.getBean(TelegramSession.class));
    }

    private Long getSessionIdForRequest(@NotNull TelegramEvent telegramEvent) {
        if (telegramEvent.getChat() != null) {
            return telegramEvent.getChat().id();
        } else if (telegramEvent.getUser() != null) {
            return Long.valueOf(telegramEvent.getUser().id());
        }

        // We are sure that update object could not be null
        return Long.valueOf(telegramEvent.getUpdate().updateId());
    }

    @Getter
    @AllArgsConstructor
    public static class TelegramSessionHolder {
        private final @NotNull TelegramSession session;

        public void releaseSessionId() {
            TelegramScope.removeId();
        }
    }
}
