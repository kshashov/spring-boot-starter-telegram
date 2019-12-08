package com.github.kshashov.telegram.handler.processor;

import lombok.Getter;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;

@Getter
public class HandlerMethod {
    private final Object bean;
    private final Class<?> beanType;
    private final Method method;
    private final Method bridgedMethod;
    private final MethodParameter[] methodParameters;

    public HandlerMethod(@NotNull Object bean, @NotNull Method method) {
        this.bean = bean;
        this.beanType = ClassUtils.getUserClass(bean);
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
        this.methodParameters = initMethodParameters();
    }

    HandlerMethod(HandlerMethod handlerMethod) {
        this.bean = handlerMethod.bean;
        this.beanType = handlerMethod.beanType;
        this.method = handlerMethod.method;
        this.bridgedMethod = handlerMethod.bridgedMethod;
        this.methodParameters = handlerMethod.methodParameters;
    }

    private MethodParameter[] initMethodParameters() {
        int count = this.bridgedMethod.getParameterCount();
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            MethodParameter parameter = new SynthesizingMethodParameter(this.bridgedMethod, i);
            GenericTypeResolver.resolveParameterType(parameter, this.beanType);
            result[i] = parameter;
        }
        return result;
    }

    public MethodParameter getReturnValue(@Nullable Object returnValue) {
        return new ReturnValueMethodParameter(returnValue);
    }

    @Override
    public String toString() {
        return bridgedMethod.getDeclaringClass().getTypeName() + '.' + bridgedMethod.getName();
    }

    /**
     * A MethodParameter for a HandlerMethod return type based on an actual return value.
     */
    private class ReturnValueMethodParameter extends MethodParameter {

        @Nullable
        private final Object returnValue;

        ReturnValueMethodParameter(@Nullable Object returnValue) {
            super(bridgedMethod, -1);
            this.returnValue = returnValue;
        }

        ReturnValueMethodParameter(ReturnValueMethodParameter original) {
            super(original);
            this.returnValue = original.returnValue;
        }

        @NonNull
        @Override
        public Class<?> getParameterType() {
            return (this.returnValue != null ? this.returnValue.getClass() : super.getParameterType());
        }

        @NonNull
        @Override
        public ReturnValueMethodParameter clone() {
            return new ReturnValueMethodParameter(this);
        }
    }
}
