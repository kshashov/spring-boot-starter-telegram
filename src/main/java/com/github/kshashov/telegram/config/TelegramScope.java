package com.github.kshashov.telegram.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class TelegramScope implements Scope {
    private static final Logger logger = LoggerFactory.getLogger(TelegramScope.class);
    public static final String SCOPE = "telegramScope";
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    private final ConfigurableListableBeanFactory beanFactory;
    private final LoadingCache<String, ConcurrentHashMap<String, Object>> conversations;

    @SuppressWarnings("unchecked")
    public TelegramScope(ConfigurableListableBeanFactory beanFactory, long expireMiileSeconds) {
        this.beanFactory = beanFactory;
        // По истечению 1 часа пользовательские бины удаляются
        conversations = CacheBuilder
                .newBuilder()
                .expireAfterAccess(expireMiileSeconds, TimeUnit.MILLISECONDS)
                .removalListener(notification -> {
                    if (notification.wasEvicted()) {
                        logger.debug("Evict session for key {}", notification.getKey());
                        Map<String, Object> userScope = (Map<String, Object>) notification.getValue();
                        if (userScope != null) {
                            userScope.values().forEach(this::removeBean);
                        }
                    }
                })
                .build(new CacheLoader<String, ConcurrentHashMap<String, Object>>() {
                    @Override
                    public ConcurrentHashMap<String, Object> load(String key) throws Exception {
                        logger.debug("Create session for key = {}", key);
                        return new ConcurrentHashMap<>();
                    }
                });
    }

    private void removeBean(Object bean) {
        beanFactory.destroyBean(bean);
    }

    public static void setIdThreadLocal(Long chatId) {
        USER_THREAD_LOCAL.set(chatId);
    }

    public static void removeId() {
        USER_THREAD_LOCAL.remove();
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        final String sessionId = getConversationId();
        if (sessionId == null) {
            throw new TelegramScopeException("There is no current user");
        }
        ConcurrentHashMap<String, Object> beans;
        try {
            beans = conversations.get(sessionId);
        } catch (ExecutionException e) {
            throw new TelegramScopeException(e);
        }
        return beans.computeIfAbsent(name, (k) -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {
        final String sessionId = getConversationId();
        if (sessionId != null) {
            final Map<String, Object> userBeans = conversations.getIfPresent(sessionId);
            if (userBeans != null) {
                return userBeans.remove(name);
            }
        }
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        Long id = USER_THREAD_LOCAL.get();
        if (id == null) {
            return null;
        }
        return Long.toString(id);
    }

}
