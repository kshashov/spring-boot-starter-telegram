package com.github.kshashov.telegram.config;


import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class TelegramBotProperties implements Iterable<TelegramBotProperty> {
    private Set<TelegramBotProperty> telegramBotProperties;

    public TelegramBotProperties() {
        this.telegramBotProperties = new TreeSet<>((o1, o2) -> o1.getToken().compareToIgnoreCase(o2.getToken()));
    }

    public void addTelegramProperty(TelegramBotProperty telegramBotProperty) {
        telegramBotProperties.add(telegramBotProperty);
    }

    @Override
    public Iterator<TelegramBotProperty> iterator() {
        return telegramBotProperties.iterator();
    }

    public void addAll(TelegramBotProperties telegramBotProperties) {
        this.telegramBotProperties.addAll(telegramBotProperties.telegramBotProperties);
    }
}
