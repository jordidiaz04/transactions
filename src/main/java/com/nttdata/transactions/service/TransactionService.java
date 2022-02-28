package com.nttdata.transactions.service;

import com.nttdata.transactions.model.Transaction;
import reactor.core.publisher.Flux;

public interface TransactionService {
    Flux<Transaction> findByIdProductAndCollection(String idProduct, int collection);
    void create(Transaction transaction);
}
