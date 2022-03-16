package com.nttdata.transactions.service;

import static com.nttdata.transactions.utilities.Constants.AccountType.FIXED_TERM;

import com.nttdata.transactions.dto.response.AccountResponse;
import com.nttdata.transactions.exceptions.customs.CustomInformationException;
import com.nttdata.transactions.exceptions.customs.CustomNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Account service implementation.
 */
@Service
public class AccountServiceImpl implements AccountService {
  @Value("${backend.service.account}")
  private String urlAccount;

  private static final String NOT_FOUND = " not found";

  @Autowired
  @Qualifier("wcLoadBalanced")
  private WebClient.Builder webClient;

  @Override
  public Mono<List<AccountResponse>> listByDebitCard(String debitCard) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/get/debitCard/{debitCard}", debitCard)
        .retrieve()
        .onStatus(NOT_FOUND::equals, response -> Mono
            .error(new CustomNotFoundException("Debit card " + debitCard + NOT_FOUND)))
        .bodyToFlux(AccountResponse.class)
        .collectList();
  }

  @Override
  public Mono<AccountResponse> findAccount(String number) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/get/number/{number}", number)
        .retrieve()
        .onStatus(NOT_FOUND::equals, response -> Mono
            .error(new CustomNotFoundException("Account " + number + NOT_FOUND)))
        .bodyToMono(AccountResponse.class)
        .map(account -> {
          if (account.getTypeAccount().getOption() == FIXED_TERM) {
            int currentDay = LocalDate.now().getDayOfMonth();
            if (currentDay != account.getTypeAccount().getDay()) {
              throw new CustomInformationException("Only the day "
                  + account.getTypeAccount().getDay()
                  + " of each month you can make a transaction for your account");
            }
          }

          return account;
        });
  }

  @Override
  public Mono<BigDecimal> getTotalBalanceByDebitCard(String debitCard) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/get/totalBalance/{debitCard}", debitCard)
        .retrieve()
        .onStatus(NOT_FOUND::equals, response -> Mono
            .error(new CustomNotFoundException("Debit card " + debitCard + NOT_FOUND)))
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
