package com.nttdata.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Transaction application, enable eureka client.
 */
@EnableEurekaClient
@SpringBootApplication
public class TransactionsApplication {
  public static void main(String[] args) {
    SpringApplication.run(TransactionsApplication.class, args);
  }
}
