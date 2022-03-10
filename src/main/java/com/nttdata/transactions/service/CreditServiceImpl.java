package com.nttdata.transactions.service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.nttdata.transactions.dto.response.CreditResponse;
import com.nttdata.transactions.exceptions.customs.CustomNotFoundException;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Credit service implementation.
 */
@Service
public class CreditServiceImpl implements CreditService {
  @Value("${backend.service.credit}")
  private String urlCredit;

  @Autowired
  @Qualifier("wcLoadBalanced")
  private WebClient.Builder webClient;

  @Override
  public Mono<CreditResponse> findCredit(String number) {
    return webClient
        .build()
        .get()
        .uri(urlCredit + "/number/{number}", number)
        .retrieve()
        .onStatus(NOT_FOUND::equals, response -> Mono
            .error(new CustomNotFoundException("Credit " + number + " not found")))
        .bodyToMono(CreditResponse.class);
  }

  @Override
  public void updateCredit(String id, BigDecimal amount) {
    webClient
        .build()
        .put()
        .uri(urlCredit + "/balance/{id}/amount/{amount}", id, amount)
        .retrieve()
        .bodyToMono(Void.class)
        .subscribe();
  }
}
