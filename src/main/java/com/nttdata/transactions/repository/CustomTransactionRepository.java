package com.nttdata.transactions.repository;

import com.nttdata.transactions.model.Transaction;
import java.time.LocalDate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom transaction repository.
 */
public interface CustomTransactionRepository {
  Mono<Long> countByIdProductAndCollection(String idProduct, Integer collection);

  Flux<Transaction> listWithTaxByIdProductAndCollection(LocalDate start, LocalDate end,
                                                        String idProduct, int collection);
}
