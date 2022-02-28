package com.nttdata.transactions.service;

import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{
    private final TransactionRepository transactionRepository;

    @Override
    public Flux<Transaction> findByIdProductAndCollection(String idProduct, int collection) {
        return transactionRepository.findByIdProductAndCollection(new ObjectId(idProduct), collection);
    }

    @Override
    public void create(Transaction transaction) {
        transactionRepository.save(transaction).subscribe();
    }
}
