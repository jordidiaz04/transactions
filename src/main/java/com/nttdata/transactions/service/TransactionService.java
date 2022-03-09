package com.nttdata.transactions.service;

import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.dto.request.FilterRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TransactionService {
    Flux<Transaction> listByAccountNumber(String accountNumber);

    Flux<Transaction> listByCreditNumber(String creditNumber);

    Flux<Transaction> listAccountTransactionsWithTax(String accountNumber, FilterRequest request);

    Flux<Transaction> listCreditTransactionsWithTax(String accountNumber, FilterRequest request);

    Mono<String> depositAccount(String accountNumber, BigDecimal amount);

    Mono<String> withdrawalsAccount(String accountNumber, BigDecimal amount);

    Mono<String> transferBetweenAccounts(String exitNumber, String entryNumber, BigDecimal amount);

    Mono<String> payCredit(String creditNumber, BigDecimal amount);

    Mono<String> spendCredit(String creditNumber, BigDecimal amount);
}
