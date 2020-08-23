package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.TestUtils;
import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.handler.processor.HandlerMethod;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class HandlerMethodContainerTest {
    private Method method;
    private HandlerMethodContainer container = new HandlerMethodContainer();
    private String token = "token";
    private RequestMappingsMatcherStrategy matcherStrategy;

    @BeforeEach
    void init() {
        matcherStrategy = Mockito.mock(RequestMappingsMatcherStrategy.class);
        when(matcherStrategy.postProcess(any())).thenAnswer((Answer) invocation -> invocation.getArguments()[0]);
        method = TestUtils.findMethodByTitle(this, "method");
    }

    @Test
    void registerController_PatternsIsEmpty_ReturnNull() {
        HandlerMethod result = container.registerController(1, method, new ArrayList<>());

        assertNull(result);
    }

    @Test
    void registerController() {
        RequestMappingInfo mapping = new RequestMappingInfo(token, "/", 1, Sets.newHashSet(MessageType.MESSAGE));
        HandlerMethod result = container.registerController(1, method, Lists.newArrayList(mapping));

        assertNotNull(result);
        assertEquals(result.getMethod(), method);
    }

    @Test
    void lookupHandlerMethod_MatcherStrategyIsMissing_ThrowIllegalStateException() {
        RequestMappingInfo mapping = new RequestMappingInfo(token, "/", 1, Sets.newHashSet(MessageType.MESSAGE));
        HandlerMethod result = container.registerController(1, method, Lists.newArrayList(mapping));

        assertNotNull(result);

        assertThrows(IllegalStateException.class, () -> {
            container.lookupHandlerMethod(request("test", MessageType.MESSAGE));
        });
    }

    @Test
    void lookupHandlerMethod_WrongToken_ReturnNullMethod() {
        container.setMatcherStrategy(matcherStrategy);

        RequestMappingInfo mapping = new RequestMappingInfo("incorrect", "/", 1, Sets.newHashSet(MessageType.MESSAGE));
        container.registerController(1, method, Lists.newArrayList(mapping));

        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test", MessageType.MESSAGE));

        assertNotNull(result);
        assertNull(result.getHandlerMethod());
    }

    @Test
    void lookupHandlerMethod() {
        container.setMatcherStrategy(matcherStrategy);

        // check without registered handler
        HandlerMethodContainer.HandlerLookupResult result = container.lookupHandlerMethod(request("test 1", MessageType.CALLBACK_QUERY));
        assertNotNull(result);
        assertNull(result.getHandlerMethod());

        // check with registered handler
        when(matcherStrategy.extractPatternVariables(any(), any())).thenReturn(Maps.newHashMap("var", "1"));
        when(matcherStrategy.isMatched(any(), any())).thenReturn(false);
        RequestMappingInfo mapping = new RequestMappingInfo(token, "test {var:[0-9]}", 1, Sets.newHashSet(MessageType.CALLBACK_QUERY));
        container.registerController(1, method, Lists.newArrayList(mapping));
        result = container.lookupHandlerMethod(request("test 1", MessageType.CALLBACK_QUERY));

        assertNotNull(result);
        assertNull(result.getHandlerMethod());

        when(matcherStrategy.isMatched(any(), any())).thenReturn(true);
        container.registerController(1, method, Lists.newArrayList(mapping));
        result = container.lookupHandlerMethod(request("test 1", MessageType.CALLBACK_QUERY));

        assertNotNull(result);
        assertNotNull(result.getHandlerMethod());
        assertEquals(1, result.getHandlerMethod().getBean());
        assertEquals(method, result.getHandlerMethod().getMethod());

        // check request updates
        assertEquals("test {var:[0-9]}", result.getBasePattern());
        assertNotNull(result.getTemplateVariables());
        assertEquals(1, result.getTemplateVariables().size());
        assertEquals("1", result.getTemplateVariables().get("var"));
    }

    public void method() {
    }

    private TelegramEvent request(String text, MessageType type) {
        TelegramEvent request = Mockito.mock(TelegramEvent.class);
        when(request.getToken()).thenReturn(token);
        when(request.getText()).thenReturn(text);
        when(request.getMessageType()).thenReturn(type);
        return request;
    }
}
