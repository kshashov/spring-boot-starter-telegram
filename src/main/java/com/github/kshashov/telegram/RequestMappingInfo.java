package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.MessageType;
import lombok.Data;

import java.util.Set;

/**
 * Stores request mappings for the current bot controller
 */
@Data
public final class RequestMappingInfo {
    private final Set<String> patterns;
    private final Set<MessageType> messageTypes;
}
