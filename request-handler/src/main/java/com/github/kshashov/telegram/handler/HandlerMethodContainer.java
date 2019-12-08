package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.handler.processor.HandlerMethod;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Map;

public interface HandlerMethodContainer {

    @NonNull
    HandlerLookupResult lookupHandlerMethod(@NotNull TelegramEvent telegramEvent);

    void registerController(@NotNull Object bean, @NotNull Method method, @NotNull RequestMappingInfo mappingInfo);

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandlerLookupResult {
        @Nullable
        private HandlerMethod handlerMethod;
        @Nullable
        private String basePattern;
        @Nullable
        private Map<String, String> templateVariables;
    }
}
