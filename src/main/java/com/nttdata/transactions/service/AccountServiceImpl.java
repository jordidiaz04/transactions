package com.nttdata.transactions.service;

import static com.nttdata.transactions.utilities.Constants.AccountType.FIXED_TERM;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.nttdata.transactions.dto.response.AccountResponse;
import com.nttdata.transactions.exceptions.customs.CustomInformationException;
import com.nttdata.transactions.exceptions.customs.CustomNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Account service implementation.
 */
@Service
public class AccountServiceImpl implements AccountService {
  @Value("${backend.service.account}")
  private String urlAccount;

  private static final String NOT_FOUND_MESSAGE = " not found";

  @Autowired
  @Qualifier("wcLoadBalanced")
  private WebClient.Builder webClient;

  @Override
  public Flux<AccountResponse> listByDebitCard(String debitCard) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/get/debitCard/{debitCard}", debitCard)
        .retrieve()
        .onStatus(status -> status == NOT_FOUND, response -> Mono
            .error(new CustomNotFoundException("Debit card " + debitCard + NOT_FOUND_MESSAGE)))
        .bodyToFlux(AccountResponse.class);
  }

  @Override
  public Mono<AccountResponse> findAccount(String number) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/get/number/{number}", number)
        .retrieve()
        .onStatus(status -> status == NOT_FOUND, response -> Mono
            .error(new CustomNotFoundException("Account " + number + NOT_FOUND_MESSAGE)))
        .bodyToMono(AccountResponse.class)
        .flatMap(account -> {
          if (account.getTypeAccount().getOption() == FIXED_TERM) {
            int currentDay = LocalDate.now().getDayOfMonth();
            if (currentDay != account.getTypeAccount().getDay()) {
              return Mono.error(new CustomInformationException("Only the day "
                  + account.getTypeAccount().getDay()
                  + " of each month you can make a transaction for your account"));
            }
          }

          return Mono.just(account);
        });
  }

  @Override
  public Mono<BigDecimal> getTotalBalanceByDebitCard(String debitCard) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/get/totalBalance/{debitCard}", debitCard)
        .retrieve()
        .onStatus(status -> status == NOT_FOUND, response -> Mono
            .error(new CustomNotFoundException("Debit card " + debitCard + NOT_FOUND_MESSAGE)))
        .bodyToMono(BigDecimal.class);
  }

  @Override
  public void updateAccount(String id, BigDecimal amount) {
    webClient
        .build()
        .put()
        .uri(urlAccount + "/balance/{id}/amount/{amount}", id, amount)
        .retrieve()
        .bodyToMono(Void.class)
        .subscribe();
  }
}
