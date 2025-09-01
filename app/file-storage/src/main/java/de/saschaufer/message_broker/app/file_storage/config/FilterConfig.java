package de.saschaufer.message_broker.app.file_storage.config;

import de.saschaufer.message_broker.app.file_storage.api.filter.MDCFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
public class FilterConfig {

    @Bean
    @Order(Integer.MIN_VALUE)
    public FilterRegistrationBean<MDCFilter> registration() {
        return new FilterRegistrationBean<>(new MDCFilter());
    }
}
