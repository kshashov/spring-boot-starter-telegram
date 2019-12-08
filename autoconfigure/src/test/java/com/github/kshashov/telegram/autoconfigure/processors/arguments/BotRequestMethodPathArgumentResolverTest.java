package com.github.kshashov.telegram.autoconfigure.processors.arguments;

import com.github.kshashov.telegram.autoconfigure.TestUtils;
import com.github.kshashov.telegram.autoconfigure.annotation.BotPathVariable;
import com.github.kshashov.telegram.handler.TelegramRequest;
import com.github.kshashov.telegram.handler.processor.TelegramSession;
import com.pengrad.telegrambot.request.BaseRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.stream.Stream;

public class BotRequestMethodPathArgumentResolverTest {

    private BotRequestMethodPathArgumentResolver processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;
    private HashMap<String, String> variables = new HashMap<>();
    private TelegramSession telegramSession;

    @BeforeEach
    void prepare() {
        this.processor = new BotRequestMethodPathArgumentResolver();
        this.values = Stream.of(TestUtils.findMethodByTitle(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.telegramSession = Mockito.mock(TelegramSession.class);
        this.telegramRequest = Mockito.mock(TelegramRequest.class);
        this.variables = new HashMap<>();
        variables.put("", "empty");
        variables.put("text", "text");
        variables.put("int", "12");
        variables.put("double", "12.12");
        Mockito.when(telegramRequest.getTemplateVariables()).thenReturn(variables);
    }

    @Test
    void supportsParameter() {
        Assertions.assertFalse(processor.supportsParameter(values[0]));
        Assertions.assertFalse(processor.supportsParameter(values[1]));
        Assertions.assertFalse(processor.supportsParameter(values[3]));

        Assertions.assertTrue(processor.supportsParameter(values[2]));
        Assertions.assertTrue(processor.supportsParameter(values[4]));
        Assertions.assertTrue(processor.supportsParameter(values[5]));
        Assertions.assertTrue(processor.supportsParameter(values[6]));
        Assertions.assertTrue(processor.supportsParameter(values[7]));
        Assertions.assertTrue(processor.supportsParameter(values[8]));
        Assertions.assertTrue(processor.supportsParameter(values[9]));
        Assertions.assertTrue(processor.supportsParameter(values[10]));
        Assertions.assertTrue(processor.supportsParameter(values[11]));
        Assertions.assertTrue(processor.supportsParameter(values[12]));
    }

    @Test
    void resolveArgument_Strings_ReturnAsIs() {
        Assertions.assertEquals("text", processor.resolveArgument(values[5], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_Numbers_ReturnConverted() {
        Assertions.assertEquals(12, processor.resolveArgument(values[7], telegramRequest, telegramSession));
        Assertions.assertEquals(12L, processor.resolveArgument(values[8], telegramRequest, telegramSession));
        Assertions.assertEquals(new Double(12.12), processor.resolveArgument(values[9], telegramRequest, telegramSession));
        Assertions.assertEquals(new Float(12.12), processor.resolveArgument(values[10], telegramRequest, telegramSession));
        Assertions.assertEquals(BigInteger.valueOf(12L), processor.resolveArgument(values[11], telegramRequest, telegramSession));
        Assertions.assertEquals(new BigDecimal(12), processor.resolveArgument(values[12], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_UnsupportedTypes_ReturnNull() {
        Assertions.assertNull(processor.resolveArgument(values[0], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_InvalidNumbers_ReturnNull() {
        Assertions.assertNull(processor.resolveArgument(values[6], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_DefaultName_ReturnForEmptyString() {
        Assertions.assertEquals("empty", processor.resolveArgument(values[4], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_MissedAnnotation_ReturnNull() {
        Assertions.assertNull(processor.resolveArgument(values[1], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_MissedName_ReturnNull() {
        Assertions.assertNull(processor.resolveArgument(values[3], telegramRequest, telegramSession));
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
