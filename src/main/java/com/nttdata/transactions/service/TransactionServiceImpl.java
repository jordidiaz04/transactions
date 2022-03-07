package com.nttdata.transactions.service;

import com.nttdata.transactions.exceptions.customs.CustomInformationException;
import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.nttdata.transactions.utilities.Constants.TransactionCollection.ACCOUNT;
import static com.nttdata.transactions.utilities.Constants.TransactionType.*;
import static com.nttdata.transactions.utilities.Constants.TransactionType.TRANSFERS;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CreditService creditService;

    @Override
    public Flux<Transaction> findByIdProductAndCollection(String idProduct, int collection) {
        return transactionRepository.findByIdProductAndCollection(new ObjectId(idProduct), collection);
    }

    @Override
    public Mono<String> depositAccount(String accountNumber, BigDecimal amount) {
        return accountService.findAccount(accountNumber)
                .flatMap(account -> transactionRepository.countByIdProductAndCollection(account.getId(), ACCOUNT)
                        .flatMap(count -> {
                            if(account.getTypeAccount().getMaxTransactions() != null && count >= account.getTypeAccount().getMaxTransactions()) {
                                throw new CustomInformationException("You reached the limit of transactions per month");
                            }

                            Transaction transaction = new Transaction();
                            transaction.setIdProduct(new ObjectId(account.getId()));
                            transaction.setCollection(ACCOUNT);
                            transaction.setType(DEPOSIT);
                            transaction.setDate(LocalDateTime.now());
                            transaction.setAmount(amount);

                            return create(transaction).flatMap(transact -> {
                                accountService.updateAccount(transaction.getIdProduct().toString(), amount);
                                return Mono.just("Successful transaction");
                            });
                        }));
    }

    @Override
    public Mono<String> withdrawalsAccount(String number, BigDecimal amount) {
        return accountService.findAccount(number)
                .flatMap(account -> {
                    Transaction transaction = new Transaction();
                    transaction.setIdProduct(new ObjectId(account.getId()));
                    transaction.setCollection(ACCOUNT);
                    transaction.setType(WITHDRAWALS);
                    transaction.setDate(LocalDateTime.now());
                    transaction.setAmount(amount);
                    return create(transaction).flatMap(transact -> {
                        accountService.updateAccount(transaction.getIdProduct().toString(), amount);
                        return Mono.just("Successful transaction");
                    });
                });
    }

    @Override
    public Mono<String> transferBetweenAccounts(String exitNumber, BigDecimal exitAmount, String entryNumber, BigDecimal entryAmount) {
        return accountService.findAccount(exitNumber)
                .flatMap(a -> accountService.findAccount(entryNumber).flatMap(b -> {
                    Transaction exit = new Transaction();
                    exit.setIdProduct(new ObjectId(a.getId()));
                    exit.setCollection(ACCOUNT);
                    exit.setType(TRANSFERS);
                    exit.setDate(LocalDateTime.now());
                    exit.setAmount(exitAmount);

                    return create(exit).flatMap(exitT -> {
                        Transaction entry = new Transaction();
                        entry.setIdProduct(new ObjectId(b.getId()));
                        entry.setCollection(ACCOUNT);
                        entry.setType(TRANSFERS);
                        entry.setDate(LocalDateTime.now());
                        entry.setAmount(entryAmount);

                        return create(entry).flatMap(entryT -> {
                            accountService.updateAccount(exit.getIdProduct().toString(), exitAmount);
                            accountService.updateAccount(entry.getIdProduct().toString(), entryAmount);

                            return Mono.just("Successful transaction");
                        });
                    });
                }));
    }

    @Override
    public Mono<String> payCredit(String number, BigDecimal amount) {
        return creditService.findCredit(number)
                .flatMap(account -> {
                    Transaction transaction = new Transaction();
                    transaction.setIdProduct(new ObjectId(account.getId()));
                    transaction.setCollection(ACCOUNT);
                    transaction.setType(DEPOSIT);
                    transaction.setDate(LocalDateTime.now());
                    transaction.setAmount(amount);
                    create(transaction);

                    creditService.updateCredit(transaction.getIdProduct().toString(), amount);
                    return Mono.just("Successful transaction");
                });
    }

    @Override
    public Mono<String> spendCredit(String number, BigDecimal amount) {
        return creditService.findCredit(number)
                .flatMap(account -> {
                    Transaction transaction = new Transaction();
                    transaction.setIdProduct(new ObjectId(account.getId()));
                    transaction.setCollection(ACCOUNT);
                    transaction.setType(WITHDRAWALS);
                    transaction.setDate(LocalDateTime.now());
                    transaction.setAmount(amount);
                    create(transaction);
                    creditService.updateCredit(transaction.getIdProduct().toString(), amount);

                    return Mono.just("Successful transaction");
                });
    }

    private Mono<Transaction> create(Transaction transaction) {
        return transactionRepository.save(transaction)
                .map(x -> {
                    logger.info("Created a new transaction with id = {}", x.getId());
                    return x;
                });
    }
}
