package com.github.kshashov.telegram.config;

/**
 * Unchecked exception thrown when an attempt is made to get a bean from a nonexistent session.
 *
 * @see TelegramScope
 */
public class TelegramScopeException extends RuntimeException {
    public TelegramScopeException(String message) {
        super(message);
    }

    public TelegramScopeException(Throwable e) {
        super(e);
    }
}
