package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.handler.processor.TelegramEvent;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Strategy that uses to work with discovered handler methods metainfo.
 */
public interface RequestMappingsMatcherStrategy {
    /**
     * Override mappings of bot handlers.
     * Will be invoked after all mappings are discovered.
     *
     * @param mappings all discovered mappings
     * @return processed discovered mappings
     */
    @NotNull
    List<HandlerMethodContainer.RequestMapping> postProcess(@NotNull List<HandlerMethodContainer.RequestMapping> mappings);

    /**
     * Check if mapping info is matched with Telegram event.
     *
     * @param telegramEvent event received from Telegram API
     * @param mappingInfo   mapping info
     * @return true if mapping is matched with passed telegramEvent
     */
    boolean isMatched(@NotNull TelegramEvent telegramEvent, @NotNull RequestMappingInfo mappingInfo);

    /**
     * Extracts variables from event text according to the matched pattern. Returns empty collection if no variable was found.
     *
     * @param text        Telegram event text
     * @param mappingInfo matched handler method metainfo
     * @return extracted variables
     */
    @NotNull
    Map<String, String> extractPatternVariables(@NotNull String text, @NotNull RequestMappingInfo mappingInfo);
}
