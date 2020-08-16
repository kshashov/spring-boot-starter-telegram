package com.github.kshashov.telegram.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

    @Bean
    public MetricsService metricsService(MetricRegistry metricRegistry) {
        return new MetricsService(metricRegistry);
    }

    @Bean
    public MetricRegistry getMetricRegistry() {
        MetricRegistry registry = new MetricRegistry();
        JmxReporter
                .forRegistry(registry)
                .inDomain("bot.metrics")
                .build()
                .start();

        return registry;
    }
}
