package com.github.kshashov.telegram.api;

import com.github.kshashov.telegram.TelegramScope;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A Session instances are supposed to be created inside {@link TelegramScope}.
 *
 * @see TelegramScope
 */
@Getter
public class TelegramSession {
    private final ConcurrentHashMap<String, Object> items = new ConcurrentHashMap<>();
}
