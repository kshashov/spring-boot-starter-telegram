package com.github.kshashov.telegram.handler.processor.response;

import com.github.kshashov.telegram.TestUtils;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class BotBaseRequestMethodProcessorTest {

    private BotBaseRequestMethodProcessor processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;

    @BeforeEach
    public void prepare() {
        this.processor = new BotBaseRequestMethodProcessor();
        this.values = Stream.of(TestUtils.findMethod(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.telegramRequest = mock(TelegramRequest.class);
    }

    @Test
    public void supportsReturnType() {
        assertFalse(processor.supportsReturnType(values[0]));
        assertFalse(processor.supportsReturnType(values[1]));
        assertFalse(processor.supportsReturnType(values[2]));

        assertTrue(processor.supportsReturnType(values[3]));
        assertTrue(processor.supportsReturnType(values[4]));
    }

    @Test
    public void handleReturnValue_IllegalValues_ReturnNull() {
        assertNull(processor.handleReturnValue("", values[0], telegramRequest));
        assertNull(processor.handleReturnValue(5, values[1], telegramRequest));
        assertNull(processor.handleReturnValue(6, values[2], telegramRequest));
        verifyNoMoreInteractions(telegramRequest);
    }

    @Test
    public void handleReturnValue_TestSendMessage() {
        BaseRequest result = processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest);

        assertNotNull(result);
        assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        assertEquals(sendMessage.getParameters().get("text"), "text");
        verifyNoMoreInteractions(telegramRequest);
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
