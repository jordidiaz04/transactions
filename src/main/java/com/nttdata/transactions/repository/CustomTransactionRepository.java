package com.nttdata.transactions.repository;

import reactor.core.publisher.Mono;

public interface CustomTransactionRepository {
    Mono<Long> countByIdProductAndCollection(String idProduct, Integer collection);
}
