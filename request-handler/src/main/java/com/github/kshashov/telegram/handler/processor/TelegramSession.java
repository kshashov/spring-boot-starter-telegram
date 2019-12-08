package com.github.kshashov.telegram.handler.processor;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TelegramSession {
    private final ConcurrentHashMap<String, Object> items = new ConcurrentHashMap<>();
}
