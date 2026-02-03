package com.eatclub.deals.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // used by DealService to call the external restaurant API
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}