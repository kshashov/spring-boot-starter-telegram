package com.github.kshashov.telegram.autoconfigure.processors.response;

import com.github.kshashov.telegram.autoconfigure.TestUtils;
import com.github.kshashov.telegram.handler.TelegramRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;

import java.util.stream.Stream;

public class BotResponseBodyMethodProcessorTest {

    private BotResponseBodyMethodProcessor processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;
    private Chat chat = new Chat();
    private ConversionService conversionService;

    @BeforeEach
    void prepare() {
        this.conversionService = Mockito.mock(ConversionService.class);
        this.processor = new BotResponseBodyMethodProcessor(conversionService);
        this.values = Stream.of(TestUtils.findMethodByTitle(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.chat = Mockito.mock(Chat.class);
        Mockito.when(chat.id()).thenReturn(12L);
        this.telegramRequest = Mockito.mock(TelegramRequest.class);
        Mockito.when(telegramRequest.getChat()).thenReturn(chat);
    }

    @Test
    void supportsReturnType() {
        Assertions.assertTrue(processor.supportsReturnType(values[0]));
        Assertions.assertTrue(processor.supportsReturnType(values[1]));
        Assertions.assertTrue(processor.supportsReturnType(values[2]));
        Assertions.assertTrue(processor.supportsReturnType(values[3]));
        Assertions.assertTrue(processor.supportsReturnType(values[4]));
    }

    @Test
    void handleReturnValue_UnsupportedTypes_ReturnNull() {
        Assertions.assertNull(processor.handleReturnValue(5, values[1], telegramRequest));
        Assertions.assertNull(processor.handleReturnValue(6, values[2], telegramRequest));
        Assertions.assertNull(processor.handleReturnValue(new SendMessage(12L, "text"), values[3], telegramRequest));
        Mockito.verifyNoMoreInteractions(telegramRequest);
    }

    @Test
    void handleReturnValue_WithoutChat_ReturnNull() {
        Mockito.when(telegramRequest.getChat()).thenReturn(null);
        BaseRequest result = processor.handleReturnValue("text", values[0], telegramRequest);

        Assertions.assertNull(result);
    }

    @Test
    void handleReturnValue_TestString() {
        BaseRequest result = processor.handleReturnValue("text", values[0], telegramRequest);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        Assertions.assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        Assertions.assertEquals(sendMessage.getParameters().get("text"), "text");
    }

    @Test
    void handleReturnValue_CanConvertToString_ReturnConverted() {
        Mockito.when(conversionService.canConvert(ArgumentMatchers.eq(SendMessage.class), ArgumentMatchers.eq(String.class))).thenReturn(true);
        Mockito.when(conversionService.convert(ArgumentMatchers.any(SendMessage.class), ArgumentMatchers.eq(String.class))).thenReturn("converted");

        BaseRequest result = processor.handleReturnValue(new SendMessage(1L, ""), values[4], telegramRequest);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        Assertions.assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        Assertions.assertEquals(sendMessage.getParameters().get("text"), "converted");

        Mockito.verify(conversionService).canConvert(ArgumentMatchers.any(), ArgumentMatchers.eq(String.class));
        Mockito.verify(conversionService).convert(ArgumentMatchers.any(), ArgumentMatchers.eq(String.class));
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
