package com.github.kshashov.telegram.autoconfigure.processors.arguments;

import com.github.kshashov.telegram.autoconfigure.TestUtils;
import com.github.kshashov.telegram.handler.TelegramRequest;
import com.github.kshashov.telegram.handler.processor.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.TelegramSession;
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

public class BotHandlerMethodArgumentResolverCompositeTest {

    private MethodParameter[] values;
    private List<BotHandlerMethodArgumentResolver> resolvers = new ArrayList<>();
    private TelegramRequest telegramRequest;
    private TelegramSession telegramSession;

    @BeforeEach
    void prepare() {
        this.values = Stream.of(TestUtils.findMethodByTitle(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        for (int i = 0; i < 10; i++) {
            BotHandlerMethodArgumentResolver handler = Mockito.mock(BotHandlerMethodArgumentResolver.class);
            Mockito.when(handler.supportsParameter(ArgumentMatchers.any())).thenReturn(false);
            resolvers.add(handler);
        }

        this.telegramSession = Mockito.mock(TelegramSession.class);
        this.telegramRequest = Mockito.mock(TelegramRequest.class);
    }

    @Test
    void supportsParameter_WithoutResolvers_ReturnFalse() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(new ArrayList<>());

        Assertions.assertFalse(processor.supportsParameter(values[0]));
        Assertions.assertFalse(processor.supportsParameter(values[1]));
        Assertions.assertFalse(processor.supportsParameter(values[2]));
        Assertions.assertFalse(processor.supportsParameter(values[3]));
        Assertions.assertFalse(processor.supportsParameter(values[4]));
    }

    @Test
    void supportsParameter_UnsupportedArguments_ReturnFalse() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        Assertions.assertFalse(processor.supportsParameter(values[0]));
        Assertions.assertFalse(processor.supportsParameter(values[1]));
        Assertions.assertFalse(processor.supportsParameter(values[2]));
        Assertions.assertFalse(processor.supportsParameter(values[3]));
        Assertions.assertFalse(processor.supportsParameter(values[4]));

        resolvers.forEach(h -> {
            Mockito.verify(h, Mockito.times(5)).supportsParameter(ArgumentMatchers.any());
            Mockito.verifyNoMoreInteractions(h);
        });
    }

    @Test
    void supportsParameter() {
        BotHandlerMethodArgumentResolver handler = Mockito.mock(BotHandlerMethodArgumentResolver.class);
        Mockito.when(handler.supportsParameter(ArgumentMatchers.any(MethodParameter.class))).thenAnswer(any ->
                SendMessage.class.isAssignableFrom(any.<MethodParameter>getArgument(0).getParameterType()));
        resolvers.add(4, handler);

        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        Assertions.assertTrue(processor.supportsParameter(values[4]));

        resolvers.stream()
                .limit(5)
                .forEach(h -> {
                    Mockito.verify(h, Mockito.times(1)).supportsParameter(ArgumentMatchers.any());
                    Mockito.verifyNoMoreInteractions(h);
                });

        resolvers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    @Test
    void resolveArgument_WithoutResolvers_ReturnNull() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(new ArrayList<>());

        Assertions.assertNull(processor.resolveArgument(values[4], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_UnsupportedArgument_ReturnNull() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        Assertions.assertNull(processor.resolveArgument(values[4], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument() {
        BotHandlerMethodArgumentResolver handler = Mockito.mock(BotHandlerMethodArgumentResolver.class);
        Mockito.when(handler.supportsParameter(ArgumentMatchers.any(MethodParameter.class))).thenReturn(true);
        Mockito.when(handler.resolveArgument(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new SendMessage(12L, "text"));

        resolvers.add(4, handler);

        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        Object result = processor.resolveArgument(values[4], telegramRequest, telegramSession);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        Assertions.assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        Assertions.assertEquals(sendMessage.getParameters().get("text"), "text");

        Mockito.verify(handler).supportsParameter(ArgumentMatchers.any());
        Mockito.verify(handler).resolveArgument(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(handler);

        resolvers.stream()
                .limit(4)
                .forEach(h -> {
                    Mockito.verify(h).supportsParameter(ArgumentMatchers.any());
                    Mockito.verifyNoMoreInteractions(h);
                });

        resolvers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
