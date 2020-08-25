package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.handler.processor.TelegramEvent;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class DefaultRequestMappingsMatcherStrategyTest {
    private final DefaultRequestMappingsMatcherStrategy strategy = new DefaultRequestMappingsMatcherStrategy();

    @Test
    void isMatched() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "/**", 1, Sets.newHashSet(MessageType.MESSAGE));

        boolean result = strategy.isMatched(request("test", MessageType.MESSAGE), mapping);
        assertFalse(result);

        result = strategy.isMatched(request("/test", MessageType.MESSAGE), mapping);
        assertTrue(result);

        mapping = new RequestMappingInfo("token", "/*", 1, Sets.newHashSet(MessageType.MESSAGE));

        result = strategy.isMatched(request("/test/asd", MessageType.MESSAGE), mapping);
        assertFalse(result);

        result = strategy.isMatched(request("/test", MessageType.MESSAGE), mapping);
        assertTrue(result);
    }

    @Test
    void isMatched_MappingHasAnyMessageType_ReturnTrue() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "test", 1, Sets.newHashSet(MessageType.MESSAGE, MessageType.ANY));
        boolean result = strategy.isMatched(request("test", MessageType.CALLBACK_QUERY), mapping);

        assertTrue(result);
    }

    @Test
    void isMatched_WrongMessageType_ReturnFalse() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "test", 1, Sets.newHashSet(MessageType.MESSAGE));
        boolean result = strategy.isMatched(request("test", MessageType.CALLBACK_QUERY), mapping);

        assertFalse(result);
    }

    @Test
    void isMatched_WrongPattern_ReturnFalse() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "test", 1, Sets.newHashSet(MessageType.MESSAGE));
        boolean result = strategy.isMatched(request("test1", MessageType.MESSAGE), mapping);

        assertFalse(result);
    }

    @Test
    void isMatched_NullPattern_WorkAsEmptyString() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "test", 1, Sets.newHashSet(MessageType.MESSAGE));
        boolean result = strategy.isMatched(request(null, MessageType.MESSAGE), mapping);

        assertFalse(result);

        mapping = new RequestMappingInfo("token", "", 1, Sets.newHashSet(MessageType.MESSAGE));
        result = strategy.isMatched(request(null, MessageType.MESSAGE), mapping);

        assertTrue(result);
    }

    @Test
    void extractPatternVariables() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "test {var:[0-9]} {var2:[ab]}", 1, Sets.newHashSet(MessageType.MESSAGE));
        Map<String, String> result = strategy.extractPatternVariables("test 1 b", mapping);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1", result.get("var"));
        assertEquals("b", result.get("var2"));
    }

    @Test
    void extractPatternVariables_MappingIsNotMatched_ThrowIllegalStateException() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "test {var:[0-9]}", 1, Sets.newHashSet(MessageType.MESSAGE));

        assertThrows(IllegalStateException.class, () -> strategy.extractPatternVariables("ads 1", mapping));
    }

    @Test
    void extractPatternVariables_NullPattern_WorkAsEmptyString() {
        RequestMappingInfo mapping = new RequestMappingInfo("token", "", 1, Sets.newHashSet(MessageType.MESSAGE));
        Map<String, String> result = strategy.extractPatternVariables(null, mapping);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void postProcess_NullPatterns_ReplaceWithWildCard() {
        // null -> **
        RequestMappingInfo mapping1 = new RequestMappingInfo("token", "{var:[0-9]}", 1, Sets.newHashSet(MessageType.MESSAGE));
        RequestMappingInfo mapping2 = new RequestMappingInfo("token", null, 1, Sets.newHashSet(MessageType.MESSAGE));

        List<HandlerMethodContainer.RequestMapping> mappings = new ArrayList<>();
        mappings.add(new HandlerMethodContainer.RequestMapping(mapping1, null));
        mappings.add(new HandlerMethodContainer.RequestMapping(mapping2, null));

        List<HandlerMethodContainer.RequestMapping> result = strategy.postProcess(mappings);
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("{var:[0-9]}", result.get(0).getMappingInfo().getPattern());
        assertEquals("**", result.get(1).getMappingInfo().getPattern());
    }

    @Test
    void postProcess() {
        RequestMappingInfo mapping1 = new RequestMappingInfo("token", "{var:[0-9]}", 1, Sets.newHashSet(MessageType.MESSAGE));
        RequestMappingInfo mapping2 = new RequestMappingInfo("token", "2", 1, Sets.newHashSet(MessageType.MESSAGE));
        RequestMappingInfo mapping3 = new RequestMappingInfo("token", "3", 2, Sets.newHashSet(MessageType.ANY));
        RequestMappingInfo mapping4 = new RequestMappingInfo("token", "4", 2, Sets.newHashSet(MessageType.MESSAGE));
        RequestMappingInfo mapping5 = new RequestMappingInfo("token", "5", 2, Sets.newHashSet(MessageType.MESSAGE, MessageType.INLINE_QUERY));

        List<HandlerMethodContainer.RequestMapping> mappings = new ArrayList<>();
        mappings.add(new HandlerMethodContainer.RequestMapping(mapping1, null));
        mappings.add(new HandlerMethodContainer.RequestMapping(mapping2, null));
        mappings.add(new HandlerMethodContainer.RequestMapping(mapping3, null));
        mappings.add(new HandlerMethodContainer.RequestMapping(mapping4, null));
        mappings.add(new HandlerMethodContainer.RequestMapping(mapping5, null));

        List<HandlerMethodContainer.RequestMapping> result = strategy.postProcess(mappings);
        assertNotNull(result);
        assertEquals(5, result.size());

        assertEquals("2", result.get(0).getMappingInfo().getPattern());
        assertEquals("4", result.get(1).getMappingInfo().getPattern());
        assertEquals("5", result.get(2).getMappingInfo().getPattern());
        assertEquals("3", result.get(3).getMappingInfo().getPattern());
        assertEquals("{var:[0-9]}", result.get(4).getMappingInfo().getPattern());

    }

    private TelegramEvent request(String text, MessageType type) {
        TelegramEvent request = Mockito.mock(TelegramEvent.class);
        when(request.getToken()).thenReturn("token");
        when(request.getText()).thenReturn(text);
        when(request.getMessageType()).thenReturn(type);
        return request;
    }
}
