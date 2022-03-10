package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.response.AccountResponse;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

/**
 * Account service interface.
 */
public interface AccountService {
  Mono<AccountResponse> findAccount(String number);

  void updateAccount(String id, BigDecimal amount);
}
