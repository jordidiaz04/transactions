package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.request.FilterRequest;
import com.nttdata.transactions.dto.request.TransactionRequest;
import com.nttdata.transactions.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Transaction service interface.
 */
public interface TransactionService {
  Flux<Transaction> listByAccountNumber(String accountNumber);

  Flux<Transaction> listByCreditNumber(String creditNumber);

  Flux<Transaction> listAccountTransactionsWithTax(String accountNumber, FilterRequest request);

  Flux<Transaction> listCreditTransactionsWithTax(String accountNumber, FilterRequest request);

  Mono<String> depositAccount(String accountNumber, TransactionRequest request);

  Mono<String> withdrawalAccount(String accountNumber, TransactionRequest request);

  Mono<String> withdrawalFromDebitCard(String debitCard, TransactionRequest request);

  Mono<String> transferBetweenAccounts(String exitNumber, String entryNumber,
                                       TransactionRequest request);

  /*Mono<String> payCredit(String creditNumber, BigDecimal amount);

  Mono<String> spendCredit(String creditNumber, BigDecimal amount);*/
}
