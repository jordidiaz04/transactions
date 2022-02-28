package com.nttdata.transactions.service;

import com.nttdata.transactions.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Flux<Transaction> findByIdProductAndCollection(String idProduct, int collection);
    Mono<Transaction> create(Transaction transaction);
}
