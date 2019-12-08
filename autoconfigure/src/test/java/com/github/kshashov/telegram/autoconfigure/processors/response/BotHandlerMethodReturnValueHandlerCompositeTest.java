package com.github.kshashov.telegram.autoconfigure.processors.response;

import com.github.kshashov.telegram.autoconfigure.TestUtils;
import com.github.kshashov.telegram.handler.TelegramRequest;
import com.github.kshashov.telegram.handler.processor.BotHandlerMethodReturnValueHandler;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BotHandlerMethodReturnValueHandlerCompositeTest {

    private MethodParameter[] values;
    private List<BotHandlerMethodReturnValueHandler> handlers = new ArrayList<>();
    private TelegramRequest telegramRequest;

    @BeforeEach
    void prepare() {
        this.values = Stream.of(TestUtils.findMethodByTitle(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        for (int i = 0; i < 10; i++) {
            BotHandlerMethodReturnValueHandler handler = Mockito.mock(BotHandlerMethodReturnValueHandler.class);
            Mockito.when(handler.supportsReturnType(ArgumentMatchers.any())).thenReturn(false);
            handlers.add(handler);
        }

        this.telegramRequest = Mockito.mock(TelegramRequest.class);
    }

    @Test
    void supportsReturnType_WithoutResolvers_ReturnFalse() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(new ArrayList<>());

        Assertions.assertFalse(processor.supportsReturnType(values[0]));
        Assertions.assertFalse(processor.supportsReturnType(values[1]));
        Assertions.assertFalse(processor.supportsReturnType(values[2]));
        Assertions.assertFalse(processor.supportsReturnType(values[3]));
        Assertions.assertFalse(processor.supportsReturnType(values[4]));
    }

    @Test
    void supportsReturnType_UnsupportedTypes_ReturnFalse() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        Assertions.assertFalse(processor.supportsReturnType(values[0]));
        Assertions.assertFalse(processor.supportsReturnType(values[1]));
        Assertions.assertFalse(processor.supportsReturnType(values[2]));
        Assertions.assertFalse(processor.supportsReturnType(values[3]));
        Assertions.assertFalse(processor.supportsReturnType(values[4]));

        handlers.forEach(h -> {
            Mockito.verify(h, Mockito.times(5)).supportsReturnType(ArgumentMatchers.any());
            Mockito.verifyNoMoreInteractions(h);
        });
    }

    @Test
    void supportsReturnType() {
        BotHandlerMethodReturnValueHandler handler = Mockito.mock(BotHandlerMethodReturnValueHandler.class);
        Mockito.when(handler.supportsReturnType(ArgumentMatchers.any(MethodParameter.class))).thenAnswer(any ->
                BaseRequest.class.isAssignableFrom(any.<MethodParameter>getArgument(0).getParameterType()));
        handlers.add(4, handler);

        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        Assertions.assertTrue(processor.supportsReturnType(values[3]));
        Assertions.assertTrue(processor.supportsReturnType(values[4]));

        handlers.stream()
                .limit(5)
                .forEach(h -> {
                    Mockito.verify(h, Mockito.times(2)).supportsReturnType(ArgumentMatchers.any());
                    Mockito.verifyNoMoreInteractions(h);
                });

        handlers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    @Test
    void handleReturnValue_WithoutResolvers_ReturnNull() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(new ArrayList<>());
        Assertions.assertNull(processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest));
    }

    @Test
    void handleReturnValue_UnsupportedTypes_ReturnNull() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        Assertions.assertNull(processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest));
    }

    @Test
    void testHandleReturnValue() {
        BotHandlerMethodReturnValueHandler handler = Mockito.mock(BotHandlerMethodReturnValueHandler.class);
        Mockito.when(handler.supportsReturnType(ArgumentMatchers.any(MethodParameter.class))).thenReturn(true);
        Mockito.when(handler.handleReturnValue(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new SendMessage(12L, "text"));

        handlers.add(4, handler);
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        BaseRequest result = processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        Assertions.assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        Assertions.assertEquals(sendMessage.getParameters().get("text"), "text");

        Mockito.verify(handler).supportsReturnType(ArgumentMatchers.any());
        Mockito.verify(handler).handleReturnValue(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(handler);

        handlers.stream()
                .limit(4)
                .forEach(h -> {
                    Mockito.verify(h).supportsReturnType(ArgumentMatchers.any());
                    Mockito.verifyNoMoreInteractions(h);
                });

        handlers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
