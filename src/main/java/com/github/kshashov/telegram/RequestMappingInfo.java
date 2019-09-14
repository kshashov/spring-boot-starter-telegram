package com.github.kshashov.telegram;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.github.kshashov.telegram.api.MessageType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.*;

/**
 * Описание метода обработки
 */
class RequestMappingInfo {

    private final Set<String> patterns;
    private final PathMatcher pathMatcher;
    private final Set<MessageType> messageTypes;

    private RequestMappingInfo(Collection<String> patterns, Collection<MessageType> messageTypes) {
        this.patterns = Collections.unmodifiableSet(Sets.newLinkedHashSet(patterns));
        this.pathMatcher = new AntPathMatcher();
        this.messageTypes = ImmutableSet.copyOf(messageTypes);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String[] path;
        private MessageType[] messageTypes;

        private Builder() {
        }

        public Builder path(String... val) {
            path = val;
            return this;
        }

        public Builder messageType(MessageType... val) {
            messageTypes = val;
            return this;
        }

        public RequestMappingInfo build() {
            return new RequestMappingInfo(asList(path), asList(messageTypes));
        }
    }

    private static <T> List<T> asList(T... patterns) {
        return (patterns != null ? Arrays.asList(patterns) : Collections.emptyList());
    }

    public RequestMappingInfo getMatchingCondition(String requestText) {
        if (this.patterns.isEmpty()) {
            return this;
        }
        if (requestText == null) {
            requestText = "";
        }
        List<String> matches = getMatchingPatterns(requestText);

        return matches.isEmpty() ? null : this;
    }

    public List<String> getMatchingPatterns(String lookupPath) {
        List<String> matches = new ArrayList<>();
        for (String pattern : this.patterns) {
            String match = getMatchingPattern(pattern, lookupPath);
            if (match != null) {
                matches.add(match);
            }
        }
        matches.sort(this.pathMatcher.getPatternComparator(lookupPath));
        return matches;
    }

    private String getMatchingPattern(String pattern, String lookupPath) {
        if (this.pathMatcher.match(pattern, lookupPath)) {
            return pattern;
        }
        return null;
    }

    public Set<String> getPatterns() {
        return patterns;
    }

    public PathMatcher getPathMatcher() {
        return pathMatcher;
    }

    public Set<MessageType> getMessageTypes() {
        return messageTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestMappingInfo)) return false;
        RequestMappingInfo that = (RequestMappingInfo) o;
        return Objects.equals(patterns, that.patterns) &&
                Objects.equals(messageTypes, that.messageTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patterns, messageTypes);
    }
}
