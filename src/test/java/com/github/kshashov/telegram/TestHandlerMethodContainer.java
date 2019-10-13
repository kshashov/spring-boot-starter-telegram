package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestHandlerMethodContainer {
    private Method method;
    private RequestMappingInfo mappingInfo = new RequestMappingInfo(
            Sets.newHashSet("", ""),
            Sets.newHashSet(MessageType.MESSAGE, MessageType.CALLBACK_QUERY));
    private HandlerMethodContainer container = new HandlerMethodContainer();

    @BeforeEach
    public void init() throws NoSuchMethodException {
        method = getClass().getMethod("method");
    }

    @Test
    public void lookupHandlerMethod_MappingHasEmptyPatterns_ReturnMethod() {
        container.registerController(1, method, new RequestMappingInfo(Sets.newHashSet(), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethod result = container.lookupHandlerMethod(request("test", MessageType.MESSAGE));

        assertNotNull(result);
        assertEquals(1, result.getBean());
        assertEquals(method, result.getMethod());
    }

    @Test
    public void lookupHandlerMethod_MappingHasAnyMessageType_ReturnMethod() {
        container.registerController(1, method, new RequestMappingInfo(Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE, MessageType.ANY)));
        HandlerMethod result = container.lookupHandlerMethod(request("test", MessageType.CALLBACK_QUERY));

        assertNotNull(result);
        assertEquals(1, result.getBean());
        assertEquals(method, result.getMethod());
    }

    @Test
    public void lookupHandlerMethod_WrongMessageType_ReturnNull() {
        container.registerController(1, method, new RequestMappingInfo(Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethod result = container.lookupHandlerMethod(request("test", MessageType.CALLBACK_QUERY));

        assertNull(result);
    }

    @Test
    public void lookupHandlerMethod_WrongPattern_ReturnNull() {
        container.registerController(1, method, new RequestMappingInfo(Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethod result = container.lookupHandlerMethod(request("test1", MessageType.MESSAGE));

        assertNull(result);
    }

    @Test
    public void lookupHandlerMethod_NullPattern_WorksAsEmptyString() {
        container.registerController(1, method, new RequestMappingInfo(Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethod result = container.lookupHandlerMethod(request(null, MessageType.MESSAGE));

        assertNull(result);

        container.registerController(1, method, new RequestMappingInfo(Sets.newHashSet(""), Sets.newHashSet(MessageType.MESSAGE)));
        result = container.lookupHandlerMethod(request(null, MessageType.MESSAGE));

        assertNotNull(result);
        assertEquals(1, result.getBean());
        assertEquals(method, result.getMethod());
    }

    @Test
    public void lookupHandlerMethod() {
        // check without  registered handler
        HandlerMethod result = container.lookupHandlerMethod(request("test 1", MessageType.CALLBACK_QUERY));
        assertNull(result);

        // check with registered handler
        container.registerController(1, method, new RequestMappingInfo(Sets.newHashSet("test {var:[0-9]}"), Sets.newHashSet(MessageType.CALLBACK_QUERY)));
        TelegramRequest test = request("test 1", MessageType.CALLBACK_QUERY);
        result = container.lookupHandlerMethod(test);

        assertNotNull(result);
        assertEquals(1, result.getBean());
        assertEquals(method, result.getMethod());

        // check request updates
        ArgumentCaptor<Map> variables = ArgumentCaptor.forClass(Map.class);
        verify(test).setBasePattern("test {var:[0-9]}");
        verify(test).setTemplateVariables(variables.capture());
        assertEquals("1", variables.getValue().get("var"));
    }

    public void method() {
    }

    private TelegramRequest request(String text, MessageType type) {
        TelegramRequest request = Mockito.mock(TelegramRequest.class);
        when(request.getText()).thenReturn(text);
        when(request.getMessageType()).thenReturn(type);
        return request;
    }
}
