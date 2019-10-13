package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.ResolversContainer;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.arguments.BotRequestMethodArgumentResolver;
import com.github.kshashov.telegram.handler.arguments.BotRequestMethodPathArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotBaseRequestMethodProcessor;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.github.kshashov.telegram.handler.response.BotResponseBodyMethodProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

import java.util.List;

@Configuration
public class MethodProcessorsConfiguration {

    @Bean
    @ConditionalOnMissingBean(ResolversContainer.class)
    public ResolversContainer resolversContainer(List<BotHandlerMethodArgumentResolver> resolvers, List<BotHandlerMethodReturnValueHandler> handlers) {
        return new ResolversContainerImpl(resolvers, handlers);
    }

    @Bean
    public BotHandlerMethodArgumentResolver botRequestMethodArgumentResolver() {
        return new BotRequestMethodArgumentResolver();
    }

    @Bean
    public BotHandlerMethodArgumentResolver botRequestMethodPathArgumentResolver() {
        return new BotRequestMethodPathArgumentResolver();
    }

    @Bean
    public BotHandlerMethodReturnValueHandler botBaseRequestMethodProcessor() {
        return new BotBaseRequestMethodProcessor();
    }

    @Bean
    public BotHandlerMethodReturnValueHandler botResponseBodyMethodProcessor(ConversionService conversionService) {
        return new BotResponseBodyMethodProcessor(conversionService);
    }

    @Bean
    public ConversionService conversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    static final class ResolversContainerImpl implements ResolversContainer {

        private final List<BotHandlerMethodArgumentResolver> argumentResolvers;
        private final List<BotHandlerMethodReturnValueHandler> returnValueHandlers;

        ResolversContainerImpl(List<BotHandlerMethodArgumentResolver> resolvers, List<BotHandlerMethodReturnValueHandler> handlers) {
            this.argumentResolvers = resolvers;
            this.returnValueHandlers = handlers;
        }

        public List<BotHandlerMethodArgumentResolver> getArgumentResolvers() {
            return argumentResolvers;
        }

        public List<BotHandlerMethodReturnValueHandler> getReturnValueHandlers() {
            return returnValueHandlers;
        }
    }
}
