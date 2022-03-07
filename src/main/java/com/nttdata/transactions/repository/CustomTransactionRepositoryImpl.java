package com.nttdata.transactions.repository;

import com.nttdata.transactions.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RequiredArgsConstructor
public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Long> countByIdProductAndCollection(String idProduct, Integer collection) {
        Query query = new Query(where("idProduct").is(new ObjectId(idProduct))
                .and("collection").is(collection));
        return mongoTemplate.count(query, Transaction.class);
    }
}
