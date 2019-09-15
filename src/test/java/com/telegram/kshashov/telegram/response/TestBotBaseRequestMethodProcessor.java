package com.telegram.kshashov.telegram.response;

import com.github.kshashov.telegram.TelegramRequestResult;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.handler.response.BotBaseRequestMethodProcessor;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TestBotBaseRequestMethodProcessor {

    private BotBaseRequestMethodProcessor processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;

    @BeforeEach
    public void prepare() throws NoSuchMethodException {
        this.processor = new BotBaseRequestMethodProcessor();
        this.values = Stream.of(this.getClass().getMethod("method", String.class, int.class, Integer.class, BaseRequest.class, SendMessage.class).getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.telegramRequest = mock(TelegramRequest.class);
    }

    @Test
    public void testSupportsReturnType() {
        assertFalse(processor.supportsReturnType(values[0]));
        assertFalse(processor.supportsReturnType(values[1]));
        assertFalse(processor.supportsReturnType(values[2]));

        assertTrue(processor.supportsReturnType(values[3]));
        assertTrue(processor.supportsReturnType(values[4]));
    }

    @Test
    public void testIllegalHandleReturnValue() {
        assertThrows(IllegalStateException.class, () -> processor.handleReturnValue(5, values[4], telegramRequest));
        assertThrows(IllegalStateException.class, () -> processor.handleReturnValue("", values[4], telegramRequest));
        assertThrows(IllegalStateException.class, () -> processor.handleReturnValue("", values[4], telegramRequest));
        verifyNoMoreInteractions(telegramRequest);
    }

    @Test
    public void testUnsupportedHandleReturnValue() throws Exception {
        assertNull(processor.handleReturnValue(5, values[0], telegramRequest).getBaseRequest());
        assertNull(processor.handleReturnValue("", values[1], telegramRequest).getBaseRequest());
        assertNull(processor.handleReturnValue("", values[2], telegramRequest).getBaseRequest());
        verifyNoMoreInteractions(telegramRequest);
    }

    @Test
    public void testHandleReturnValue() throws Exception {
        assertDoesNotThrow(() -> processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest));
        TelegramRequestResult result = processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest);

        assertNotNull(result);
        assertNotNull(result.getBaseRequest());
        assertTrue(result.getBaseRequest() instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result.getBaseRequest();
        assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        assertEquals(sendMessage.getParameters().get("text"), "text");
        verifyNoMoreInteractions(telegramRequest);
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
