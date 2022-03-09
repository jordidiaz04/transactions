package com.nttdata.transactions.controller;

import com.nttdata.transactions.dto.request.FilterRequest;
import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

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

    @PostMapping("/deposit/account/{number}")
    @ResponseStatus(CREATED)
    public Mono<String> depositAccount(@PathVariable String number,
                                       BigDecimal amount) {
        return transactionService.depositAccount(number, amount);
    }

    @PostMapping("/withdrawals/account/{number}")
    @ResponseStatus(CREATED)
    public Mono<String> withdrawalsAccount(@PathVariable String number,
                                           BigDecimal amount) {
        return transactionService.withdrawalsAccount(number, amount);
    }

    @PostMapping("/transfer/{exitNumber}/to/{entryNumber}")
    @ResponseStatus(CREATED)
    public Mono<String> transferBetweenAccounts(@PathVariable String exitNumber,
                                                @PathVariable String entryNumber,
                                                BigDecimal amount) {
        return transactionService.transferBetweenAccounts(exitNumber, entryNumber, amount);
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
}
