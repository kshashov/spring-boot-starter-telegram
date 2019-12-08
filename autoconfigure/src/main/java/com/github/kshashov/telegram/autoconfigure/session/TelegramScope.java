package com.github.kshashov.telegram.autoconfigure.session;

import com.github.kshashov.telegram.handler.processor.TelegramSession;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Scope store all beans in the cache by chat id (or user id)
 * <p>The bean lifetime after the last call can be redefined using the property {@code
 * TelegramConfigurationProperties.getSessionSeconds()}.</p>
 *
 * <p><strong>Note: </strong> All {@link TelegramSession} instances have this scope by
 * default.
 *
 * @see TelegramScopeException
 */
@Slf4j
public class TelegramScope implements Scope {
    public static final String SCOPE = "telegramScope";
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    private final ConfigurableListableBeanFactory beanFactory;
    private final LoadingCache<String, ConcurrentHashMap<String, Object>> conversations;

    @SuppressWarnings("unchecked")
    public TelegramScope(@NotNull ConfigurableListableBeanFactory beanFactory, long expireSeconds) {
        this.beanFactory = beanFactory;
        conversations = CacheBuilder
                .newBuilder()
                .expireAfterAccess(expireSeconds, TimeUnit.SECONDS)
                .removalListener(notification -> {
                    if (notification.wasEvicted()) {
                        log.debug("Evict session for key {}", notification.getKey());
                        Map<String, Object> userScope = (Map<String, Object>) notification.getValue();
                        if (userScope != null) {
                            userScope.values().forEach(this::removeBean);
                        }
                    }
                })
                .build(new CacheLoader<String, ConcurrentHashMap<String, Object>>() {
                    @Override
                    public ConcurrentHashMap<String, Object> load(@NonNull String key) {
                        log.debug("Create session for key = {}", key);
                        return new ConcurrentHashMap<>();
                    }
                });
    }

    static void setIdThreadLocal(Long chatId) {
        USER_THREAD_LOCAL.set(chatId);
    }

    static void removeId() {
        USER_THREAD_LOCAL.remove();
    }

    private void removeBean(Object bean) {
        beanFactory.destroyBean(bean);
    }

    @NonNull
    @Override
    public Object get(@NonNull String name, @NonNull ObjectFactory<?> objectFactory) throws TelegramScopeException {
        final String sessionId = getConversationId();
        if (sessionId == null) {
            throw new TelegramScopeException("There is no current session");
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
    public Object remove(@NonNull String name) {
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
    public void registerDestructionCallback(@NonNull String name, @NonNull Runnable callback) {
    }

    @Override
    public Object resolveContextualObject(@NonNull String key) {
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
