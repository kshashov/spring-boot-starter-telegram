package com.github.kshashov.telegram.api;

import java.util.concurrent.ConcurrentHashMap;

public class TelegramSession {
    private ConcurrentHashMap<String, Object> attribute;

    public ConcurrentHashMap<String, Object> getAttribute() {
        return attribute;
    }
}
