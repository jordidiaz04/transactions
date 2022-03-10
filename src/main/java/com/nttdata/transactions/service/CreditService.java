package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.response.CreditResponse;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

/**
 * Credit service interface.
 */
public interface CreditService {
  Mono<CreditResponse> findCredit(String number);

  void updateCredit(String id, BigDecimal amount);
}
