package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.response.AccountResponse;
import java.math.BigDecimal;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Account service interface.
 */
public interface AccountService {
  Mono<List<AccountResponse>> listByDebitCard(String debitCard);

  Mono<AccountResponse> findAccount(String number);

  Mono<BigDecimal> getTotalBalanceByDebitCard(String debitCard);

  void updateAccount(String id, BigDecimal amount);
}
