package com.nttdata.transactions.service;

import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;

    @Override
    public Flux<Transaction> findByIdProductAndCollection(String idProduct, int collection) {
        return transactionRepository.findByIdProductAndCollection(new ObjectId(idProduct), collection);
    }

    @Override
    public Mono<Transaction> create(Transaction transaction) {
        return transactionRepository.save(transaction)
                .map(x -> {
                    logger.info("Created a new transaction with id = {}", x.getId());
                    return x;
                });
    }
}
