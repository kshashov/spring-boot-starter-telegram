package com.github.kshashov.telegram.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Timer;
import com.github.kshashov.telegram.handler.processor.HandlerMethod;

import static java.lang.String.format;

/**
 * Manages application metrics using jmx.
 *
 * @since 0.22
 */
public class MetricsService {
    public static final String UPDATES_RECEIVED = "updates";
    public static final String UPDATE_ERRORS = "processing.errors";
    public static final String NO_HANDLERS_ERRORS = "no.handlers.errors";
    public static final String HANDLER_ERRORS = "handler.%s.errors";
    public static final String HANDLER_SUCCESSES = "handler.%s.successes";
    public static final String HANDLER_EXECUTION_TIME = "handler.%s.execution.time";
    private final MetricRegistry metricRegistry;

    public MetricsService(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        metricRegistry.register(UPDATES_RECEIVED, new Meter());
        metricRegistry.register(UPDATE_ERRORS, new Meter());
        metricRegistry.register(NO_HANDLERS_ERRORS, new Meter());
    }

    /**
     * Stores updates count into {@link #UPDATES_RECEIVED} metric.
     *
     * @param messages updates count
     */
    public void onUpdatesReceived(int messages) {
        metricRegistry.getMeters().get(UPDATES_RECEIVED).mark(messages);
    }

    /**
     * Updates {@link #NO_HANDLERS_ERRORS} metric.
     */
    public void onNoHandlersFound() {
        metricRegistry.getMeters().get(NO_HANDLERS_ERRORS).mark();
    }

    /**
     * Updates {@link #UPDATE_ERRORS} metric.
     */
    public void onUpdateError() {
        metricRegistry.getMeters().get(UPDATE_ERRORS).mark();
    }

    /**
     * Creates handler related metrics.
     *
     * @param method handler method
     */
    public void registerHandlerMethod(HandlerMethod method) {
        metricRegistry.register(format(HANDLER_ERRORS, getMethodName(method)), new Meter());
        metricRegistry.register(format(HANDLER_EXECUTION_TIME, getMethodName(method)), new Timer(new SlidingWindowReservoir(64)));
        metricRegistry.register(format(HANDLER_SUCCESSES, getMethodName(method)), new Meter());
    }

    /**
     * Started times associated with method.
     *
     * @param method handler method
     * @return timer context that should be passed to {@link #onUpdateSuccess} when updated is processed
     */
    public Timer.Context onMethodHandlerStarted(HandlerMethod method) {
        return metricRegistry.getTimers().get(format(HANDLER_EXECUTION_TIME, getMethodName(method))).time();
    }

    /**
     * Updates {@link #HANDLER_ERRORS} metric.
     *
     * @param method handler method
     */
    public void onUpdateError(HandlerMethod method) {
        metricRegistry.getMeters().get(format(HANDLER_ERRORS, getMethodName(method))).mark();
    }

    /**
     * Updates {@link #HANDLER_SUCCESSES} and {@link #HANDLER_EXECUTION_TIME} metric.
     *
     * @param method       handler method
     * @param timerContext context created by {@link #onMethodHandlerStarted}
     */
    public void onUpdateSuccess(HandlerMethod method, Timer.Context timerContext) {
        metricRegistry.getMeters().get(format(HANDLER_SUCCESSES, getMethodName(method))).mark();
        timerContext.close();
    }

    /**
     * Returns user-friendly method name.
     *
     * @param method handler method
     * @return user-friendly method name
     */
    private String getMethodName(HandlerMethod method) {
        return method.getBeanType().getName() + "." + method.getBridgedMethod().getName();
    }
}
