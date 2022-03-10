package com.nttdata.transactions.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration.
 */
@Configuration
public class WebClientConfig {
  @Bean("wcLoadBalanced")
  @LoadBalanced
  public WebClient.Builder webClient() {
    return WebClient.builder();
  }
}
