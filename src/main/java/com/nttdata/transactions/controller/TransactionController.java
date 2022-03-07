package com.nttdata.transactions.controller;

import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.service.AccountService;
import com.nttdata.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.nttdata.transactions.utilities.Constants.TransactionType.*;
import static com.nttdata.transactions.utilities.Constants.TransactionCollection.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping(value = "/get/{idProduct}/{collection}", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<Transaction> findByIdProductAndCollection(@PathVariable String idProduct,
                                                          @PathVariable int collection) {
        return transactionService.findByIdProductAndCollection(idProduct, collection);
    }

    @PostMapping("/deposit/account/{number}")
    @ResponseStatus(CREATED)
    public Mono<String> depositAccount(@PathVariable String number,
                                       BigDecimal amount) {
        return accountService.findAccount(number)
                .flatMap(account -> {
                    Transaction transaction = new Transaction();
                    transaction.setIdProduct(new ObjectId(account.getId()));
                    transaction.setCollection(ACCOUNT);
                    transaction.setType(DEPOSIT);
                    transaction.setDate(LocalDateTime.now());
                    transaction.setAmount(amount);
                    transactionService.create(transaction);

                    accountService.updateAccount(transaction.getIdProduct().toString(), amount);
                    return Mono.just("Successful transaction");
                });
    }

    @PostMapping("/withdrawals/account/{number}")
    @ResponseStatus(CREATED)
    public Mono<String> withdrawalsAccount(@PathVariable String number,
                                           BigDecimal amount) {
        BigDecimal finalAmount = amount.multiply(BigDecimal.valueOf(-1));

        return accountService.findAccount(number)
                .flatMap(account -> {
                    Transaction transaction = new Transaction();
                    transaction.setIdProduct(new ObjectId(account.getId()));
                    transaction.setCollection(ACCOUNT);
                    transaction.setType(WITHDRAWALS);
                    transaction.setDate(LocalDateTime.now());
                    transaction.setAmount(finalAmount);
                    transactionService.create(transaction);
                    accountService.updateAccount(transaction.getIdProduct().toString(), finalAmount);

                    return Mono.just("Successful transaction");
                });
    }

    @PostMapping("/transfer/account/{exitNumber}/to/{entryNumber}")
    @ResponseStatus(CREATED)
    public Mono<String> transferBetweenAccounts(@PathVariable String exitNumber,
                                                @PathVariable String entryNumber,
                                                BigDecimal amount) {
        BigDecimal exitAmount = amount.multiply(BigDecimal.valueOf(-1));

        return accountService.findAccount(exitNumber)
                .flatMap(a -> accountService.findAccount(entryNumber).flatMap(b -> {
                    Transaction exit = new Transaction();
                    exit.setIdProduct(new ObjectId(a.getId()));
                    exit.setCollection(ACCOUNT);
                    exit.setType(TRANSFERS);
                    exit.setDate(LocalDateTime.now());
                    exit.setAmount(exitAmount);

                    return transactionService.create(exit).flatMap(exitT -> {
                        Transaction entry = new Transaction();
                        entry.setIdProduct(new ObjectId(b.getId()));
                        entry.setCollection(ACCOUNT);
                        entry.setType(TRANSFERS);
                        entry.setDate(LocalDateTime.now());
                        entry.setAmount(amount);

                        return transactionService.create(entry).flatMap(entryT -> {
                            accountService.updateAccount(exit.getIdProduct().toString(), exitAmount);
                            accountService.updateAccount(entry.getIdProduct().toString(), amount);

                            return Mono.just("Successful transaction");
                        });
                    });
                }));
    }
}
