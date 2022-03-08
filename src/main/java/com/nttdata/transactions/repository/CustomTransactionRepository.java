package com.nttdata.transactions.repository;

import com.nttdata.transactions.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CustomTransactionRepository {
    Mono<Long> countByIdProductAndCollection(String idProduct, Integer collection);
    Flux<Transaction> listWithTaxByIdProductAndCollection(LocalDate start, LocalDate end, String idProduct, int collection);
}
