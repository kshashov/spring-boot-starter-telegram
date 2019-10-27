package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolverComposite;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandlerComposite;
import com.pengrad.telegrambot.request.BaseRequest;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Extension of {@link HandlerMethod} that invokes the underlying method with argument values resolved from the current
 * telegram request through a list of {@link BotHandlerMethodArgumentResolver} and then resolves the return value with a
 * list of {@link BotHandlerMethodReturnValueHandler}.
 */
public class TelegramInvocableHandlerMethod extends HandlerMethod {

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final BotHandlerMethodArgumentResolverComposite argumentResolver;
    private final BotHandlerMethodReturnValueHandlerComposite returnValueHandler;

    /**
     * Create an instance from a bean instance and a method
     *
     * @param handlerMethod method to invoke
     * @param argumentResolvers resolvers list to resolve arguments
     * @param returnValueHandlers handlers list to handle return value
     */
    public TelegramInvocableHandlerMethod(@NotNull HandlerMethod handlerMethod, @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers, @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers) {
        super(handlerMethod);
        this.argumentResolver = new BotHandlerMethodArgumentResolverComposite(argumentResolvers);
        this.returnValueHandler = new BotHandlerMethodReturnValueHandlerComposite(returnValueHandlers);
    }

    public BaseRequest invokeAndHandle(TelegramRequest telegramRequest, TelegramSession telegramSession) throws Exception {
        Object[] args = getMethodArgumentValues(telegramRequest, telegramSession);
        if (logger.isTraceEnabled()) {
            logger.trace("Invoking '" + ClassUtils.getQualifiedMethodName(getMethod(), getBeanType()) + "' with arguments " + Arrays.toString(args));
        }
        Object returnValue = doInvoke(args);
        if (logger.isTraceEnabled()) {
            logger.trace("Method [" + ClassUtils.getQualifiedMethodName(getMethod(), getBeanType()) + "] returned [" + returnValue + "]");
        }
        return returnValueHandler.handleReturnValue(returnValue, getReturnValueType(returnValue), telegramRequest);
    }

    private Object doInvoke(Object[] args) throws Exception {
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

    private Object[] getMethodArgumentValues(TelegramRequest telegramRequest, TelegramSession telegramSession) {
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args[i] = this.argumentResolver.resolveArgument(parameter, telegramRequest, telegramSession);
        }
        return args;
    }

    /**
     * Generate detailed information about method invocation
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
