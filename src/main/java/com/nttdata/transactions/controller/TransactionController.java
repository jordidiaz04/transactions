package com.nttdata.transactions.controller;

import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final WebClient webClient;

    @GetMapping(value = "/get/{idProduct}/{collection}", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<Transaction> findByIdProductAndCollection(@PathVariable String idProduct,
                                                          @PathVariable int collection) {
        return transactionService.findByIdProductAndCollection(idProduct, collection);
    }

    @PostMapping("/create/{idProduct}/{collection}")
    @ResponseStatus(CREATED)
    public Mono<Transaction> create(@PathVariable String idProduct,
                                    @PathVariable int collection,
                                    BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setIdProduct(new ObjectId(idProduct));
        transaction.setCollection(collection);
        transaction.setType(amount.compareTo(BigDecimal.valueOf(0)) > 0 ? 1 : 2);
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(amount);
        Mono<Transaction> response = transactionService.create(transaction);

        String path = collection == 1 ? "http://localhost:8081" : "http://localhost:8099/credits";
        String url = path + "/balance/{id}/amount/{amount}";
        webClient.put()
                .uri(url, idProduct, amount)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();

        return response;
    }
}
