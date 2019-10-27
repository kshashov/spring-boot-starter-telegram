package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.TestUtils;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.pengrad.telegrambot.request.BaseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBotRequestMethodPathArgumentResolver {

    private BotRequestMethodPathArgumentResolver processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;
    private HashMap<String, String> variables = new HashMap<>();
    private TelegramSession telegramSession;

    @BeforeEach
    public void prepare() {
        this.processor = new BotRequestMethodPathArgumentResolver();
        this.values = Stream.of(TestUtils.findMethod(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.telegramSession = mock(TelegramSession.class);
        this.telegramRequest = mock(TelegramRequest.class);
        this.variables = new HashMap<>();
        variables.put("", "empty");
        variables.put("text", "text");
        variables.put("int", "12");
        variables.put("double", "12.12");
        when(telegramRequest.getTemplateVariables()).thenReturn(variables);
    }

    @Test
    public void supportsParameter() {
        assertFalse(processor.supportsParameter(values[0]));
        assertFalse(processor.supportsParameter(values[1]));
        assertFalse(processor.supportsParameter(values[3]));

        assertTrue(processor.supportsParameter(values[2]));
        assertTrue(processor.supportsParameter(values[4]));
        assertTrue(processor.supportsParameter(values[5]));
        assertTrue(processor.supportsParameter(values[6]));
        assertTrue(processor.supportsParameter(values[7]));
        assertTrue(processor.supportsParameter(values[8]));
        assertTrue(processor.supportsParameter(values[9]));
        assertTrue(processor.supportsParameter(values[10]));
        assertTrue(processor.supportsParameter(values[11]));
        assertTrue(processor.supportsParameter(values[12]));
    }

    @Test
    public void resolveArgument_Strings_ReturnAsIs() {
        assertEquals("text", processor.resolveArgument(values[5], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_Numbers_ReturnConverted() {
        assertEquals(12, processor.resolveArgument(values[7], telegramRequest, telegramSession));
        assertEquals(12L, processor.resolveArgument(values[8], telegramRequest, telegramSession));
        assertEquals(new Double(12.12), processor.resolveArgument(values[9], telegramRequest, telegramSession));
        assertEquals(new Float(12.12), processor.resolveArgument(values[10], telegramRequest, telegramSession));
        assertEquals(BigInteger.valueOf(12L), processor.resolveArgument(values[11], telegramRequest, telegramSession));
        assertEquals(new BigDecimal(12), processor.resolveArgument(values[12], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_UnsupportedTypes_ReturnNull() {
        assertNull(processor.resolveArgument(values[0], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_InvalidNumbers_ReturnNull() {
        assertNull(processor.resolveArgument(values[6], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_DefaultName_ReturnForEmptyString() {
        assertEquals("empty", processor.resolveArgument(values[4], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_MissedAnnotation_ReturnNull() {
        assertNull(processor.resolveArgument(values[1], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_MissedName_ReturnNull() {
        assertNull(processor.resolveArgument(values[3], telegramRequest, telegramSession));
    }

    public void method(
            BaseRequest unsupported,
            String str,
            @BotPathVariable("missed") String missed,
            @BotPathVariable("int") int primitive,

            @BotPathVariable() String empty,
            @BotPathVariable("text") String text,
            @BotPathVariable("text") Integer incorrectInt,
            @BotPathVariable("int") Integer integer,
            @BotPathVariable("int") Long longNum,

            @BotPathVariable("double") Double doubleNum,
            @BotPathVariable("double") Float floatNum,
            @BotPathVariable("int") BigInteger bigInt,
            @BotPathVariable("int") BigDecimal bigDecimal) {
    }
}
