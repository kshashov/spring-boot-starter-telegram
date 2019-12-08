package com.github.kshashov.telegram.autoconfigure.processors.response;

import com.github.kshashov.telegram.autoconfigure.TestUtils;
import com.github.kshashov.telegram.handler.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;

import java.util.stream.Stream;

public class BotBaseRequestMethodProcessorTest {

    private BotBaseRequestMethodProcessor processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;

    @BeforeEach
    void prepare() {
        this.processor = new BotBaseRequestMethodProcessor();
        this.values = Stream.of(TestUtils.findMethodByTitle(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.telegramRequest = Mockito.mock(TelegramRequest.class);
    }

    @Test
    void supportsReturnType() {
        Assertions.assertFalse(processor.supportsReturnType(values[0]));
        Assertions.assertFalse(processor.supportsReturnType(values[1]));
        Assertions.assertFalse(processor.supportsReturnType(values[2]));

        Assertions.assertTrue(processor.supportsReturnType(values[3]));
        Assertions.assertTrue(processor.supportsReturnType(values[4]));
    }

    @Test
    void handleReturnValue_IllegalValues_ReturnNull() {
        Assertions.assertNull(processor.handleReturnValue("", values[0], telegramRequest));
        Assertions.assertNull(processor.handleReturnValue(5, values[1], telegramRequest));
        Assertions.assertNull(processor.handleReturnValue(6, values[2], telegramRequest));
        Mockito.verifyNoMoreInteractions(telegramRequest);
    }

    @Test
    void handleReturnValue_TestSendMessage() {
        BaseRequest result = processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        Assertions.assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        Assertions.assertEquals(sendMessage.getParameters().get("text"), "text");
        Mockito.verifyNoMoreInteractions(telegramRequest);
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
