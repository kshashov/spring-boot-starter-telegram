package com.github.kshashov.telegram.handler.processor;

import javax.validation.constraints.NotNull;

public interface SessionResolver {
    SessionHolder resolveTelegramSession(@NotNull TelegramEvent telegramEvent);

    interface SessionHolder {
        TelegramSession getSession();

        void releaseSession();
    }
}
