package com.nttdata.transactions.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.nttdata.transactions.model.Transaction;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom transaction repository implementation.
 */
@RequiredArgsConstructor
public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {
  private final ReactiveMongoTemplate mongoTemplate;

  @Override
  public Mono<Long> countByIdProductAndCollection(String idProduct, Integer collection) {
    Query query = new Query(where("idProduct").is(new ObjectId(idProduct))
        .and("collection").is(collection)
        .and("month").is(LocalDate.now().getMonthValue()));
    return mongoTemplate.count(query, Transaction.class);
  }

  @Override
  public Flux<Transaction> listWithTaxByIdProductAndCollection(LocalDate start, LocalDate end,
                                                               String idProduct, int collection) {
    Query query = new Query(where("idProduct").is(new ObjectId(idProduct))
        .and("collection").is(collection)
        .and("date").gte(start).lt(end.plusDays(1)));
    return mongoTemplate.find(query, Transaction.class);
  }
}
