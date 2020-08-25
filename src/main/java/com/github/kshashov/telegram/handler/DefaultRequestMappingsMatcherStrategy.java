package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Default implementation of {@link RequestMappingsMatcherStrategy} that uses {@link AntPathMatcher}.
 * Uses the following criteries for route priorities: pattern complexity, patterns count, types count.
 */
public class DefaultRequestMappingsMatcherStrategy implements RequestMappingsMatcherStrategy, Comparator<RequestMappingInfo> {
    private final PathMatcher pathMatcher;

    public DefaultRequestMappingsMatcherStrategy() {
        this(new AntPathMatcher());
    }

    public DefaultRequestMappingsMatcherStrategy(@NotNull PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    public boolean isMatched(@NotNull TelegramEvent telegramEvent, @NotNull RequestMappingInfo mappingInfo) {
        // Check message type
        if (!mappingInfo.getMessageTypes().contains(telegramEvent.getMessageType()) && !mappingInfo.getMessageTypes().contains(MessageType.ANY)) {
            return false;
        }

        String text = telegramEvent.getText();
        if (text == null) {
            text = "";
        }

        return pathMatcher.match(mappingInfo.getPattern(), text);
    }

    @Override
    @NotNull
    public List<HandlerMethodContainer.RequestMapping> postProcess(@NotNull List<HandlerMethodContainer.RequestMapping> mappings) {
        List<HandlerMethodContainer.RequestMapping> result = new ArrayList<>(mappings);
        // Replace null pattern with '**'
        result.forEach(mapping -> {
            if (mapping.getMappingInfo().getPattern() == null) {
                RequestMappingInfo info = mapping.getMappingInfo();
                mapping.setMappingInfo(new RequestMappingInfo(info.getToken(), "**", info.getPatternsCount(), info.getMessageTypes()));
            }
        });
        result.sort(((o1, o2) -> compare(o1.getMappingInfo(), o2.getMappingInfo())));
        return result;
    }

    @Override
    @NotNull
    public Map<String, String> extractPatternVariables(@NotNull String text, @NotNull RequestMappingInfo mappingInfo) {
        if (text == null) text = "";
        return pathMatcher.extractUriTemplateVariables(mappingInfo.getPattern(), text);
    }

    @Override
    public int compare(@NotNull RequestMappingInfo o1, @NotNull RequestMappingInfo o2) {
        int compared = pathMatcher.getPatternComparator("").compare(o1.getPattern(), o2.getPattern());
        if (compared != 0) {
            return compared;
        } else if (o1.getPatternsCount() != o2.getPatternsCount()) {
            return Integer.compare(o1.getPatternsCount(), o2.getPatternsCount());
        }

        // Check types
        Set<MessageType> t1 = o1.getMessageTypes();
        Set<MessageType> t2 = o2.getMessageTypes();
        if (t1.contains(MessageType.ANY) && t2.contains(MessageType.ANY)) {
            return 0;
        } else if (t1.contains(MessageType.ANY)) {
            return 1;
        } else if (t2.contains(MessageType.ANY)) {
            return -1;
        } else if (t1.size() != t2.size()) {
            return Integer.compare(t1.size(), t2.size());
        }

        return 0;
    }
}
