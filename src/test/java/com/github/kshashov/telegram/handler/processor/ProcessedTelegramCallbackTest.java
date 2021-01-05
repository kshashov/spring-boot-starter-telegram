package com.github.kshashov.telegram.handler.processor;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.BaseRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProcessedTelegramCallbackTest {

    @Test
    void get() {
        Callback nestedCallback = mock(Callback.class);
        BaseRequest request = mock(BaseRequest.class);

        final TelegramCallback callback = new TelegramCallback(null, null);

        assertNull(callback.getRequest());
        assertDoesNotThrow(() -> callback.onFailure(null, null));
        assertDoesNotThrow(() -> callback.onResponse(null, null));

        final TelegramCallback callback2 = new TelegramCallback(request, nestedCallback);
        assertEquals(request, callback2.getRequest());
        assertDoesNotThrow(() -> callback2.onFailure(null, null));
        verify(nestedCallback).onFailure(any(), any());
        assertDoesNotThrow(() -> callback2.onResponse(null, null));
        verify(nestedCallback).onFailure(any(), any());
    }
}
