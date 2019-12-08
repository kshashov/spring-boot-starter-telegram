package com.github.kshashov.telegram.handler;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class TestUtils {

    public static Method findMethodByTitle(Object bean, String methodName) throws NoSuchElementException {
        return Stream.of(bean.getClass().getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst().get();
    }
}
