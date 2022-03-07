package com.nttdata.transactions.controller;

import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping(value = "/get/{idProduct}/{collection}", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<Transaction> findByIdProductAndCollection(@PathVariable String idProduct,
                                                          @PathVariable int collection) {
        return transactionService.findByIdProductAndCollection(idProduct, collection);
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
        BigDecimal finalAmount = amount.multiply(BigDecimal.valueOf(-1));
        return transactionService.withdrawalsAccount(number, finalAmount);
    }

    @PostMapping("/transfer/account/{exitNumber}/to/{entryNumber}")
    @ResponseStatus(CREATED)
    public Mono<String> transferBetweenAccounts(@PathVariable String exitNumber,
                                                @PathVariable String entryNumber,
                                                BigDecimal amount) {
        BigDecimal exitAmount = amount.multiply(BigDecimal.valueOf(-1));
        return transactionService.transferBetweenAccounts(exitNumber, exitAmount, entryNumber, amount);
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
