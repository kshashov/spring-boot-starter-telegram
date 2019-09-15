package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.MessageType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.*;

/**
 * Utility class to handle path templates for the current bot controller
 */
@Builder
public class RequestMappingInfo {
    @Getter
    private final PathMatcher pathMatcher = new AntPathMatcher();
    @Getter
    private final Set<String> patterns;
    @Getter
    private final Set<MessageType> messageTypes;

    private RequestMappingInfo(Set<String> patterns, Set<MessageType> messageTypes) {
        this.patterns = Collections.unmodifiableSet(Sets.newLinkedHashSet(patterns));
        this.messageTypes = ImmutableSet.copyOf(messageTypes);
    }

    public List<String> getMatchingPatterns(String requestText) {
        if (this.patterns.isEmpty()) {
            return new ArrayList<>();
        }
        if (requestText == null) {
            requestText = "";
        }
        return matchingPatterns(requestText);
    }

    private List<String> matchingPatterns(String lookupPath) {
        List<String> matches = new ArrayList<>();
        for (String pattern : this.patterns) {
            if (this.pathMatcher.match(pattern, lookupPath)) {
                matches.add(pattern);
            }
        }
        matches.sort(this.pathMatcher.getPatternComparator(lookupPath));
        return matches;
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
