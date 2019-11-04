package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.api.MessageType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

/**
 * Stores request mappings for the current bot controller
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class RequestMappingInfo {
    private final String token;
    private final Set<String> patterns;
    private final Set<MessageType> messageTypes;
}
