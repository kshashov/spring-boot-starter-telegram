package com.github.kshashov.telegram.handler.processor.response;

import com.github.kshashov.telegram.TestUtils;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BotResponseBodyMethodProcessorTest {

    private BotResponseBodyMethodProcessor processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;
    private Chat chat = new Chat();
    private ConversionService conversionService;

    @BeforeEach
    void prepare() {
        this.conversionService = mock(ConversionService.class);
        this.processor = new BotResponseBodyMethodProcessor(conversionService);
        this.values = Stream.of(TestUtils.findMethodByTitle(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.chat = mock(Chat.class);
        when(chat.id()).thenReturn(12L);
        this.telegramRequest = mock(TelegramRequest.class);
        when(telegramRequest.getChat()).thenReturn(chat);
    }

    @Test
    void supportsReturnType() {
        assertTrue(processor.supportsReturnType(values[0]));
        assertTrue(processor.supportsReturnType(values[1]));
        assertTrue(processor.supportsReturnType(values[2]));
        assertTrue(processor.supportsReturnType(values[3]));
        assertTrue(processor.supportsReturnType(values[4]));
    }

    @Test
    void handleReturnValue_UnsupportedTypes_ReturnNull() {
        assertNull(processor.handleReturnValue(5, values[1], telegramRequest));
        assertNull(processor.handleReturnValue(6, values[2], telegramRequest));
        assertNull(processor.handleReturnValue(new SendMessage(12L, "text"), values[3], telegramRequest));
        verifyNoMoreInteractions(telegramRequest);
    }

    @Test
    void handleReturnValue_WithoutChat_ReturnNull() {
        when(telegramRequest.getChat()).thenReturn(null);
        BaseRequest result = processor.handleReturnValue("text", values[0], telegramRequest);

        assertNull(result);
    }

    @Test
    void handleReturnValue_TestString() {
        BaseRequest result = processor.handleReturnValue("text", values[0], telegramRequest);

        assertNotNull(result);
        assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        assertEquals(sendMessage.getParameters().get("text"), "text");
    }

    @Test
    void handleReturnValue_CanConvertToString_ReturnConverted() {
        when(conversionService.canConvert(eq(SendMessage.class), eq(String.class))).thenReturn(true);
        when(conversionService.convert(any(SendMessage.class), eq(String.class))).thenReturn("converted");

        BaseRequest result = processor.handleReturnValue(new SendMessage(1L, ""), values[4], telegramRequest);
        assertNotNull(result);
        assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        assertEquals(sendMessage.getParameters().get("text"), "converted");

        verify(conversionService).canConvert(any(), eq(String.class));
        verify(conversionService).convert(any(), eq(String.class));
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
