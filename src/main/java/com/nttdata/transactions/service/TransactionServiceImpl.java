package com.nttdata.transactions.service;

import static com.nttdata.transactions.utilities.Constants.TransactionCollection.ACCOUNT;
import static com.nttdata.transactions.utilities.Constants.TransactionCollection.CREDIT;
import static com.nttdata.transactions.utilities.Constants.TransactionType.DEPOSIT;
import static com.nttdata.transactions.utilities.Constants.TransactionType.TRANSFERS;
import static com.nttdata.transactions.utilities.Constants.TransactionType.WITHDRAWALS;

import com.nttdata.transactions.dto.request.FilterRequest;
import com.nttdata.transactions.dto.response.AccountResponse;
import com.nttdata.transactions.exceptions.customs.CustomInformationException;
import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Transaction service implementation.
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
  private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);
  private static final String SUCCESS_MESSAGE = "Successful transaction";

  private final TransactionRepository transactionRepository;
  private final AccountService accountService;
  private final CreditService creditService;

  @Override
  public Flux<Transaction> listByAccountNumber(String accountNumber) {
    return accountService.findAccount(accountNumber)
        .flatMapMany(account -> transactionRepository
            .findByIdProductAndCollection(new ObjectId(account.getId()), ACCOUNT));
  }

  @Override
  public Flux<Transaction> listByCreditNumber(String creditNumber) {
    return creditService.findCredit(creditNumber)
        .flatMapMany(account -> transactionRepository
            .findByIdProductAndCollection(new ObjectId(account.getId()), CREDIT));
  }

  @Override
  public Flux<Transaction> listAccountTransactionsWithTax(String accountNumber,
                                                          FilterRequest request) {
    return accountService.findAccount(accountNumber)
        .flatMapMany(account -> transactionRepository
            .listWithTaxByIdProductAndCollection(request.getStart(), request.getEnd(),
                account.getId(), ACCOUNT));
  }

  @Override
  public Flux<Transaction> listCreditTransactionsWithTax(String creditNumber,
                                                         FilterRequest request) {
    return creditService.findCredit(creditNumber)
        .flatMapMany(account -> transactionRepository
            .listWithTaxByIdProductAndCollection(request.getStart(), request.getEnd(),
                account.getId(), CREDIT));
  }

  @Override
  public Mono<String> depositAccount(String accountNumber, BigDecimal amount) {
    return accountService.findAccount(accountNumber)
        .flatMap(account -> transactionRepository
            .countByIdProductAndCollection(account.getId(), ACCOUNT)
            .flatMap(count -> {
              BigDecimal tax = requireTax(count, account);
              Transaction transaction = new Transaction(account.getId(), ACCOUNT,
                  DEPOSIT, amount, tax);

              return create(transaction).flatMap(transact -> {
                accountService.updateAccount(transaction.getIdProduct().toString(), amount);
                if (tax != null && tax.compareTo(BigDecimal.valueOf(0)) > 0) {
                  accountService
                      .updateAccount(transaction.getIdProduct().toString(),
                          tax.multiply(BigDecimal.valueOf(-1)));
                }

                return Mono.just(SUCCESS_MESSAGE);
              });
            }));
  }

  @Override
  public Mono<String> withdrawalsAccount(String accountNumber, BigDecimal amount) {
    return accountService.findAccount(accountNumber)
        .flatMap(account -> transactionRepository
            .countByIdProductAndCollection(account.getId(), ACCOUNT)
            .flatMap(count -> {
              if (account.getBalance().compareTo(BigDecimal.valueOf(0)) == 0) {
                throw new CustomInformationException("You do not have a balance "
                    + "to carry out this transaction");
              }

              BigDecimal tax = requireTax(count, account);
              Transaction transaction = new Transaction(account.getId(), ACCOUNT,
                  WITHDRAWALS, amount, tax);

              return create(transaction).flatMap(transact -> {
                accountService.updateAccount(transaction.getIdProduct().toString(),
                    amount.multiply(BigDecimal.valueOf(-1)));
                if (tax != null && tax.compareTo(BigDecimal.valueOf(0)) > 0) {
                  accountService
                      .updateAccount(transaction.getIdProduct().toString(),
                          tax.multiply(BigDecimal.valueOf(-1)));
                }

                return Mono.just(SUCCESS_MESSAGE);
              });
            }));
  }

  @Override
  public Mono<String> transferBetweenAccounts(String exitNumber,
                                              String entryNumber,
                                              BigDecimal amount) {
    return accountService.findAccount(exitNumber)
        .flatMap(a -> transactionRepository.countByIdProductAndCollection(a.getId(), ACCOUNT)
            .flatMap(count -> accountService.findAccount(entryNumber)
                .flatMap(b -> {
                  if (a.getBalance().compareTo(BigDecimal.valueOf(0)) == 0) {
                    throw new CustomInformationException("You do not have a balance "
                        + "to carry out this transaction");
                  }

                  BigDecimal tax = requireTax(count, a);
                  Transaction exit = new Transaction(a.getId(), ACCOUNT, TRANSFERS, amount, tax);

                  return create(exit).flatMap(exitT -> {
                    Transaction entry = new Transaction();
                    entry.setIdProduct(new ObjectId(b.getId()));
                    entry.setCollection(ACCOUNT);
                    entry.setType(TRANSFERS);
                    entry.setDate(LocalDateTime.now());
                    entry.setAmount(amount);

                    return create(entry).flatMap(entryT -> {
                      accountService.updateAccount(exit.getIdProduct().toString(),
                          amount.multiply(BigDecimal.valueOf(-1)));
                      if (tax != null && tax.compareTo(BigDecimal.valueOf(0)) > 0) {
                        accountService
                            .updateAccount(a.getId(), tax.multiply(BigDecimal.valueOf(-1)));
                      }

                      accountService.updateAccount(entry.getIdProduct().toString(), amount);

                      return Mono.just(SUCCESS_MESSAGE);
                    });
                  });
                })
            ));
  }

  @Override
  public Mono<String> payCredit(String creditNumber, BigDecimal amount) {
    return creditService.findCredit(creditNumber)
        .flatMap(account -> {
          Transaction transaction = new Transaction();
          transaction.setIdProduct(new ObjectId(account.getId()));
          transaction.setCollection(CREDIT);
          transaction.setType(DEPOSIT);
          transaction.setDate(LocalDateTime.now());
          transaction.setAmount(amount);
          create(transaction);

          creditService.updateCredit(transaction.getIdProduct().toString(), amount);
          return Mono.just(SUCCESS_MESSAGE);
        });
  }

  @Override
  public Mono<String> spendCredit(String creditNumber, BigDecimal amount) {
    return creditService.findCredit(creditNumber)
        .flatMap(account -> {
          Transaction transaction = new Transaction();
          transaction.setIdProduct(new ObjectId(account.getId()));
          transaction.setCollection(CREDIT);
          transaction.setType(WITHDRAWALS);
          transaction.setDate(LocalDateTime.now());
          transaction.setAmount(amount);
          create(transaction);
          creditService.updateCredit(transaction.getIdProduct().toString(), amount);

          return Mono.just(SUCCESS_MESSAGE);
        });
  }

  private Mono<Transaction> create(Transaction transaction) {
    return transactionRepository.save(transaction)
        .map(x -> {
          logger.info("Created a new transaction with id = {}", x.getId());
          return x;
        });
  }

  private BigDecimal requireTax(Long count, AccountResponse account) {
    boolean hasTax = account.getTypeAccount().getMaxTransactions() != null
        && count >= account.getTypeAccount().getMaxTransactions();
    if (!hasTax) {
      return null;
    }

    return account.getTypeAccount().getTax() == null
        ? BigDecimal.valueOf(0) :
        account.getTypeAccount().getTax();
  }
}
