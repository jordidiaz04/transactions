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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

/**
 * RestController for transaction service.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
  private static final Logger logger = LogManager.getLogger(TransactionController.class);
  private final TransactionService transactionService;

  @GetMapping("/test")
  public Mono<String> test() {
    return Mono
        .just("abc")
        .flatMap(id -> Flux.range(1, 10)
            .flatMap(nr -> Mono.just(nr).doOnNext(a -> logger.info("Number: {}", a)))
            .then(Mono.just(id)))
        .map(id -> {
          logger.info("ID: {}", id);
          return id;
        });
  }

  @GetMapping(value = "/get/account/{number}", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listByAccountNumber(@PathVariable String number) {
    return transactionService.listByAccountNumber(number);
  }

  @GetMapping(value = "/get/credit/{number}", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listByCreditNumber(@PathVariable String number) {
    return transactionService.listByCreditNumber(number);
  }

  @GetMapping(value = "/get/account/{number}/commissions", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listAccountTransactionsWithTax(@PathVariable String number,
                                                          @Valid FilterRequest request) {
    return transactionService.listAccountTransactionsWithTax(number, request);
  }

  /**
   * Deposit account.
   */
  @PostMapping("/deposit/account/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> depositAccount(@PathVariable String number,
                                     @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Ingreso en efectivo");
    }
    return transactionService.depositAccount(number, request);
  }

  /**
   * Withdraw from account.
   */
  @PostMapping("/withdrawal/account/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> withdrawalAccount(@PathVariable String number,
                                        @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Retiro de efectivo");
    }
    return transactionService.withdrawalAccount(number, request);
  }

  /**
   * Withdraw from debit card.
   */
  @PostMapping("/withdrawal/debitCard/{debitCard}")
  @ResponseStatus(CREATED)
  public Mono<String> withdrawalFromDebitCard(@PathVariable String debitCard,
                                              @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Retiro de efectivo");
    }
    return transactionService.withdrawalFromDebitCard(debitCard, request);
  }

  /**
   * Transfer between two accounts.
   */
  @PostMapping("/transfer/account/{exitNumber}/account/{entryNumber}")
  @ResponseStatus(CREATED)
  public Mono<String> transferBetweenAccounts(@PathVariable String exitNumber,
                                              @PathVariable String entryNumber,
                                              @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Transferencia entre cuentas");
    }
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

  /*
  @GetMapping(value = "/get/credit/{number}/tax", produces = TEXT_EVENT_STREAM_VALUE)
  public Flux<Transaction> listCreditTransactionsWithTax(@PathVariable String number,
                                                         @Valid FilterRequest request) {
    return transactionService.listCreditTransactionsWithTax(number, request);
  }
 */
}
