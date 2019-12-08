package com.github.kshashov.telegram.autoconfigure;

import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.MessageType;
import com.github.kshashov.telegram.handler.RequestMappingInfo;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class HandlerMethodRepositoryTest {
    private Method method;
    private RequestMappingInfo mappingInfo = new RequestMappingInfo(
            "",
            Sets.newHashSet("", ""),
            Sets.newHashSet(MessageType.MESSAGE, MessageType.CALLBACK_QUERY));
    private HandlerMethodContainer container = new HandlerMethodRepository();
    private String token = "token";

    @BeforeEach
    void init() {
        method = TestUtils.findMethodByTitle(this, "method");
    }

    @Test
    void lookupHandlerMethod_MappingHasEmptyPatterns_ReturnMethodWithEmptyPattern() {
        container.registerController(1, method, new RequestMappingInfo(token, Sets.newHashSet(), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test", MessageType.MESSAGE));

        Assertions.assertNotNull(result);

        Assertions.assertNotNull(result.getHandlerMethod());
        Assertions.assertEquals("", result.getBasePattern());
        Assertions.assertNotNull(result.getTemplateVariables());
        Assertions.assertTrue(result.getTemplateVariables().isEmpty());

        Assertions.assertEquals(1, result.getHandlerMethod().getBean());
        Assertions.assertEquals(method, result.getHandlerMethod().getMethod());
    }

    @Test
    void lookupHandlerMethod_MappingHasAnyMessageType_ReturnMethod() {
        container.registerController(1, method, new RequestMappingInfo(token, Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE, MessageType.ANY)));
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test", MessageType.CALLBACK_QUERY));

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getHandlerMethod());
        Assertions.assertEquals("test", result.getBasePattern());
        Assertions.assertNotNull(result.getTemplateVariables());
        Assertions.assertTrue(result.getTemplateVariables().isEmpty());

        Assertions.assertEquals(1, result.getHandlerMethod().getBean());
        Assertions.assertEquals(method, result.getHandlerMethod().getMethod());
    }

    @Test
    void lookupHandlerMethod_WrongToken_ReturnNullMethod() {
        container.registerController(1, method, new RequestMappingInfo("incorrect", Sets.newHashSet(), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test", MessageType.MESSAGE));

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getHandlerMethod());
    }

    @Test
    void lookupHandlerMethod_WrongMessageType_ReturnNullMethod() {
        container.registerController(1, method, new RequestMappingInfo(token, Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test", MessageType.CALLBACK_QUERY));

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getHandlerMethod());
    }

    @Test
    void lookupHandlerMethod_WrongPattern_ReturnNull() {
        container.registerController(1, method, new RequestMappingInfo(token, Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test1", MessageType.MESSAGE));

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getHandlerMethod());
    }

    @Test
    void lookupHandlerMethod_NullPattern_WorksAsEmptyString() {
        container.registerController(1, method, new RequestMappingInfo(token, Sets.newHashSet("test"), Sets.newHashSet(MessageType.MESSAGE)));
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request(null, MessageType.MESSAGE));

        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getHandlerMethod());

        container.registerController(1, method, new RequestMappingInfo(token, Sets.newHashSet(""), Sets.newHashSet(MessageType.MESSAGE)));
        result = container.lookupHandlerMethod(request(null, MessageType.MESSAGE));

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getHandlerMethod());
        Assertions.assertEquals("", result.getBasePattern());
        Assertions.assertNotNull(result.getTemplateVariables());
        Assertions.assertTrue(result.getTemplateVariables().isEmpty());

        Assertions.assertEquals(1, result.getHandlerMethod().getBean());
        Assertions.assertEquals(method, result.getHandlerMethod().getMethod());
    }

    @Test
    void lookupHandlerMethod() {
        // check without  registered handler
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test 1", MessageType.CALLBACK_QUERY));
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getHandlerMethod());

        // check with registered handler
        container.registerController(1, method, new RequestMappingInfo(token, Sets.newHashSet("test {var:[0-9]}"), Sets.newHashSet(MessageType.CALLBACK_QUERY)));
        TelegramEvent test = request("test 1", MessageType.CALLBACK_QUERY);
        result = container.lookupHandlerMethod(test);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getHandlerMethod());
        Assertions.assertEquals(1, result.getHandlerMethod().getBean());
        Assertions.assertEquals(method, result.getHandlerMethod().getMethod());

        // check request updates
        Assertions.assertEquals("test {var:[0-9]}", result.getBasePattern());
        Assertions.assertNotNull(result.getTemplateVariables());
        Assertions.assertEquals(1, result.getTemplateVariables().size());
        Assertions.assertEquals("1", result.getTemplateVariables().get("var"));
    }

    public void method() {
    }

    private TelegramEvent request(String text, MessageType type) {
        TelegramEvent request = Mockito.mock(TelegramEvent.class);
        Mockito.when(request.getToken()).thenReturn(token);
        Mockito.when(request.getText()).thenReturn(text);
        Mockito.when(request.getMessageType()).thenReturn(type);
        return request;
    }
}
