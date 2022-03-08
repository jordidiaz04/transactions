package com.nttdata.transactions.service;

import com.nttdata.transactions.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TransactionService {
    Flux<Transaction> findByIdProductAndCollection(String idProduct, int collection);
    Mono<String> depositAccount(String accountNumber, BigDecimal amount);
    Mono<String> withdrawalsAccount(String number, BigDecimal amount);
    Mono<String> transferBetweenAccounts(String exitNumber, String entryNumber, BigDecimal amount);
    Mono<String> payCredit(String number, BigDecimal amount);
    Mono<String> spendCredit(String number, BigDecimal amount);
}
