package com.nttdata.transactions.repository;

import com.nttdata.transactions.model.Transaction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Transaction repository.
 */
@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, ObjectId>,
    CustomTransactionRepository {
  Flux<Transaction> findByIdProductAndCollection(ObjectId idProduct, int collection);
}
