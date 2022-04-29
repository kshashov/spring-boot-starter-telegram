package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.api.MessageType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

/**
 * Stores request mappings for the current bot handler.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RequestMappingInfo {
    private final String token;
    private final String pattern;
    private final int patternsCount;
    private final Set<MessageType> messageTypes;
}
