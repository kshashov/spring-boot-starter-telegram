package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.handler.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Extension of {@link HandlerMethod} that invokes the underlying method with argument values resolved from the current
 * telegram request through a list of {@link BotHandlerMethodArgumentResolver} and then resolves the return value with a
 * list of {@link BotHandlerMethodReturnValueHandler}.
 */
@Slf4j
public class TelegramInvocableHandlerMethod extends HandlerMethod {

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final BotHandlerMethodArgumentResolver argumentResolver;
    private final BotHandlerMethodReturnValueHandler returnValueHandler;

    /**
     * Create an instance from a bean instance and a method.
     *
     * @param handlerMethod       method to invoke
     * @param argumentResolver   resolvers list to resolve arguments
     * @param returnValueHandler handlers list to handle return value
     */
    public TelegramInvocableHandlerMethod(@NotNull HandlerMethod handlerMethod, @NotNull BotHandlerMethodArgumentResolver argumentResolver, @NotNull BotHandlerMethodReturnValueHandler returnValueHandler) {
        super(handlerMethod);
        this.argumentResolver = argumentResolver;
        this.returnValueHandler = returnValueHandler;
    }

    /**
     * Invoke {@code TelegramInvocableHandlerMethod} with given arguments and return result.
     *
     * @param telegramRequest request
     * @param telegramSession current session
     * @return result of invocation
     * @throws IllegalStateException when it failed to execute the handler method correctly
     */
    public BaseRequest invokeAndHandle(@NotNull TelegramRequest telegramRequest, @NotNull TelegramSession telegramSession) throws IllegalStateException {
        Object[] args = getMethodArgumentValues(telegramRequest, telegramSession);
        if (log.isTraceEnabled()) {
            log.trace("Invoking '" + ClassUtils.getQualifiedMethodName(getMethod(), getBeanType()) + "' with arguments " + Arrays.toString(args));
        }
        Object returnValue = doSafeInvoke(args);
        if (log.isTraceEnabled()) {
            log.trace("Method [" + ClassUtils.getQualifiedMethodName(getMethod(), getBeanType()) + "] returned [" + returnValue + "]");
        }

        MethodParameter returnValueParameter = getReturnValue(returnValue);
        if (returnValueHandler.supportsReturnType(returnValueParameter)) {
            return returnValueHandler.handleReturnValue(returnValue, returnValueParameter, telegramRequest);
        }

        return null;
    }

    private Object doSafeInvoke(Object[] args) throws IllegalStateException {
        ReflectionUtils.makeAccessible(getBridgedMethod());
        try {
            return getBridgedMethod().invoke(getBean(), args);
        } catch (InvocationTargetException ex) {
            // Unwrap for HandlerExceptionResolvers ...
            Throwable targetException = ex.getTargetException();
            String text = getInvocationErrorMessage("Failed to invoke handler method", args);
            throw new IllegalStateException(text, targetException);
        } catch (Exception ex) {
            String text = ex.getMessage() != null ? ex.getMessage() : "";
            throw new IllegalStateException(getInvocationErrorMessage(text, args), ex);
        }
    }

    private Object[] getMethodArgumentValues(@NotNull TelegramRequest telegramRequest, @NotNull TelegramSession telegramSession) {
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            if (argumentResolver.supportsParameter(parameter)) {
                args[i] = this.argumentResolver.resolveArgument(parameter, telegramRequest, telegramSession);
            }
        }
        return args;
    }

    /**
     * Generate detailed information about method invocation.
     *
     * @param text         initial exception message
     * @param resolvedArgs input arguments for method invocation
     * @return detailed information about method invocation
     */
    private String getInvocationErrorMessage(String text, Object[] resolvedArgs) {
        StringBuilder sb = new StringBuilder(text).append("\n");
        sb.append("HandlerMethod details: \n");
        sb.append("Controller [").append(getBeanType().getName()).append("]\n");
        sb.append("Method [").append(getBridgedMethod().toGenericString()).append("]\n");
        sb.append("Resolved arguments: \n");
        for (int i = 0; i < resolvedArgs.length; i++) {
            sb.append("[").append(i).append("] ");
            if (resolvedArgs[i] == null) {
                sb.append("[null] \n");
            } else {
                sb.append("[type=").append(resolvedArgs[i].getClass().getName()).append("] ");
                sb.append("[value=").append(resolvedArgs[i]).append("]\n");
            }
        }
        return sb.toString();
    }
}
