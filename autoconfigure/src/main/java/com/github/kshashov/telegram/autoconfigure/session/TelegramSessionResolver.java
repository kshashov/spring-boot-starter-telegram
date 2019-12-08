package com.github.kshashov.telegram.autoconfigure.session;

import com.github.kshashov.telegram.handler.processor.SessionResolver;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import com.github.kshashov.telegram.handler.processor.TelegramSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;

public class TelegramSessionResolver implements SessionResolver {
    private ApplicationContext context;

    public TelegramSessionResolver(ApplicationContext context) {
        this.context = context;
    }

    public SessionHolder resolveTelegramSession(@NotNull TelegramEvent telegramEvent) {
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
    public static class TelegramSessionHolder implements SessionHolder {
        private final @NotNull TelegramSession session;

        public void releaseSession() {
            TelegramScope.removeId();
        }
    }
}
