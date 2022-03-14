package com.nttdata.transactions.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.nttdata.transactions.dto.request.FilterRequest;
import com.nttdata.transactions.dto.request.TransactionRequest;
import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.service.TransactionService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * RestController for transaction service.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
  private final TransactionService transactionService;

  @GetMapping(value = "/get/account/{number}", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listByAccountNumber(@PathVariable String number) {
    return transactionService.listByAccountNumber(number);
  }

  @GetMapping(value = "/get/credit/{number}", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listByCreditNumber(@PathVariable String number) {
    return transactionService.listByCreditNumber(number);
  }

  @PostMapping("/deposit/account/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> depositAccount(@PathVariable String number,
                                     @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Ingreso en efectivo");
    }
    return transactionService.depositAccount(number, request);
  }

  @PostMapping("/withdrawal/account/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> withdrawalAccount(@PathVariable String number,
                                        @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Retiro de efectivo");
    }
    return transactionService.withdrawalAccount(number, request);
  }





  @GetMapping(value = "/get/account/{number}/tax", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listAccountTransactionsWithTax(@PathVariable String number,
                                                          @Valid FilterRequest request) {
    return transactionService.listAccountTransactionsWithTax(number, request);
  }


  @GetMapping(value = "/get/credit/{number}/tax", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listCreditTransactionsWithTax(@PathVariable String number,
                                                         @Valid FilterRequest request) {
    return transactionService.listCreditTransactionsWithTax(number, request);
  }
/*
  @PostMapping("/transfer/{exitNumber}/to/{entryNumber}")
  @ResponseStatus(CREATED)
  public Mono<String> transferBetweenAccounts(@PathVariable String exitNumber,
                                              @PathVariable String entryNumber,
                                              @Valid @RequestBody TransactionRequest request) {
    return transactionService.transferBetweenAccounts(exitNumber, entryNumber, request);
  }

  @PostMapping("/pay/credit/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> payCredit(@PathVariable String number,
                                BigDecimal amount) {
    return transactionService.payCredit(number, amount);
  }

  @PostMapping("/spend/credit/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> spendCredit(@PathVariable String number,
                                  BigDecimal amount) {
    BigDecimal finalAmount = amount.multiply(BigDecimal.valueOf(-1));
    return transactionService.spendCredit(number, finalAmount);
  }
 */
}
