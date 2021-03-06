package com.nttdata.transactions.service;

import static com.nttdata.transactions.utilities.Constants.TransactionCollection.ACCOUNT;
import static com.nttdata.transactions.utilities.Constants.TransactionCollection.CREDIT;
import static com.nttdata.transactions.utilities.Constants.TransactionType.ENTRY;
import static com.nttdata.transactions.utilities.Constants.TransactionType.EXIT;

import com.nttdata.transactions.dto.request.FilterRequest;
import com.nttdata.transactions.dto.request.TransactionRequest;
import com.nttdata.transactions.dto.response.AccountResponse;
import com.nttdata.transactions.exceptions.customs.CustomInformationException;
import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

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
  public Flux<Transaction> listAccountTransactionsWithCommission(String accountNumber,
                                                                 FilterRequest request) {
    return accountService.findAccount(accountNumber)
        .flatMapMany(account -> transactionRepository
            .listWithTaxByIdProductAndCollection(request.getStart(), request.getEnd(),
                account.getId(), ACCOUNT));
  }

  @Override
  public Mono<String> depositAccount(String accountNumber, TransactionRequest request) {
    return accountService.findAccount(accountNumber)
        .flatMap(account -> transactionRepository
            .countByIdProductAndCollection(account.getId(), ACCOUNT)
            .flatMap(count -> {
              BigDecimal commission = getCommission(count, account);
              Transaction transaction = new Transaction(ACCOUNT, account.getId(),
                  request.getDescription(), ENTRY, request.getAmount(), commission);

              return create(transaction).flatMap(transact -> {
                updateAccountBalance(transaction, request.getAmount(), ENTRY);

                if (commission.compareTo(BigDecimal.ZERO) > 0) {
                  updateAccountBalance(transaction, commission, EXIT);
                }

                return Mono.just(SUCCESS_MESSAGE);
              });
            }));
  }

  @Override
  public Mono<String> withdrawalAccount(String accountNumber, TransactionRequest request) {
    return accountService.findAccount(accountNumber)
        .flatMap(account -> transactionRepository
            .countByIdProductAndCollection(account.getId(), ACCOUNT)
            .flatMap(count -> {
              if (account.getBalance().compareTo(request.getAmount()) < 0) {
                return Mono.error(new CustomInformationException("You do not have a balance "
                    + "to carry out this transaction"));
              }

              BigDecimal commission = getCommission(count, account);
              Transaction transaction = new Transaction(ACCOUNT, account.getId(),
                  request.getDescription(), EXIT, request.getAmount(), commission);

              return create(transaction).flatMap(transact -> {
                updateAccountBalance(transaction, request.getAmount(), EXIT);

                if (commission.compareTo(BigDecimal.ZERO) > 0) {
                  updateAccountBalance(transaction, commission, EXIT);
                }

                return Mono.just(SUCCESS_MESSAGE);
              });
            }));
  }

  @Override
  public Mono<String> withdrawalFromDebitCard(String debitCard, TransactionRequest request) {
    Flux<AccountResponse> fluxAccount = accountService.listByDebitCard(debitCard)
        .flatMap(this::setTotalTransactions);

    return fluxAccount
        .map(AccountResponse::getAvailableBalance)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .flatMap(total -> {
          if (total.compareTo(request.getAmount()) < 0) {
            return Mono.error(new CustomInformationException("You do not have "
                + "enough balance in your accounts"));
          }
          return Mono.just(total);
        })
        .flatMapMany(total -> fluxAccount)
        .sort(Comparator.comparing(AccountResponse::getPosition))
        .flatMap(account -> {
          if (request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balance = account.getAvailableBalance();
            BigDecimal commission = getCommission(account.getTotalTransactions(), account);
            BigDecimal amount;
            BigDecimal amountTransaction;

            if (balance.subtract(request.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
              request.setAmount(request.getAmount().subtract(balance));
              amount = account.getBalance();
              amountTransaction = account.getAvailableBalance();
            } else {
              amount = request.getAmount().add(commission);
              amountTransaction = request.getAmount();
              request.setAmount(BigDecimal.ZERO);
            }

            Transaction transaction = new Transaction(ACCOUNT, account.getId(),
                request.getDescription(), EXIT, amountTransaction, commission);

            return create(transaction)
                .map(transact -> {
                  updateAccountBalance(transaction, amount, EXIT);

                  return account;
                });
          } else {
            return Mono.just(account);
          }
        })
        .then(Mono.just(SUCCESS_MESSAGE));
  }

  @Override
  public Mono<String> transferBetweenAccounts(String exitNumber,
                                              String entryNumber,
                                              TransactionRequest request) {
    Mono<AccountResponse> exitAccount = accountService.findAccount(exitNumber)
        .subscribeOn(Schedulers.parallel());
    Mono<AccountResponse> entryAccount = accountService.findAccount(entryNumber)
        .subscribeOn(Schedulers.parallel());

    Mono<Tuple2<AccountResponse, AccountResponse>> zip = Mono.zip(exitAccount, entryAccount);
    return zip
        .flatMap(res -> {
          logger.info("Exit account: {}", res.getT1());
          logger.info("Entry account: {}", res.getT2());
          AccountResponse acExit = res.getT1();
          AccountResponse acEntry = res.getT2();

          return transactionRepository.countByIdProductAndCollection(acExit.getId(), ACCOUNT)
              .flatMap(count -> {
                if (acExit.getBalance().compareTo(request.getAmount()) < 0) {
                  return Mono.error(new CustomInformationException("You do not have a balance "
                      + "to carry out this transaction"));
                }

                BigDecimal commission = getCommission(count, acExit);
                Transaction exit = new Transaction(ACCOUNT, acExit.getId(),
                    request.getDescription(), EXIT, request.getAmount(), commission);
                Transaction entry = new Transaction(ACCOUNT, acEntry.getId(),
                    request.getDescription(), ENTRY, request.getAmount(), commission);

                Mono<Transaction> monoExit = create(exit)
                    .doOnNext(t -> {
                      updateAccountBalance(exit, request.getAmount(), EXIT);

                      if (commission.compareTo(BigDecimal.ZERO) > 0) {
                        updateAccountBalance(exit, commission, EXIT);
                      }
                    })
                    .subscribeOn(Schedulers.parallel());
                Mono<Transaction> monoEntry = create(entry)
                    .doOnNext(t -> updateAccountBalance(entry, request.getAmount(), ENTRY))
                    .subscribeOn(Schedulers.parallel());

                return Mono.zip(monoExit, monoEntry)
                    .flatMap(t -> Mono.just(SUCCESS_MESSAGE));
              });
        });
  }

  @Override
  public Mono<String> payCredit(String creditNumber, BigDecimal amount) {
    return creditService.findCredit(creditNumber)
        .flatMap(account -> {
          Transaction transaction = new Transaction();
          transaction.setIdProduct(new ObjectId(account.getId()));
          transaction.setCollection(CREDIT);
          transaction.setType(ENTRY);
          transaction.setDate(LocalDateTime.now());
          transaction.setAmount(amount);
          create(transaction);
          updateCreditBalance(transaction, amount, ENTRY);

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
          transaction.setType(EXIT);
          transaction.setDate(LocalDateTime.now());
          transaction.setAmount(amount);
          create(transaction);
          updateCreditBalance(transaction, amount, EXIT);

          return Mono.just(SUCCESS_MESSAGE);
        });
  }

  private BigDecimal getCommission(Long count, AccountResponse account) {
    boolean requireCommission = account.getTypeAccount().getMaxTransactions() != null
        && count >= account.getTypeAccount().getMaxTransactions();
    if (!requireCommission) {
      return BigDecimal.ZERO;
    }

    return account.getTypeAccount().getCommission() == null
        ? BigDecimal.ZERO :
        account.getTypeAccount().getCommission();
  }

  private Mono<AccountResponse> setTotalTransactions(AccountResponse account) {
    return transactionRepository
        .countByIdProductAndCollection(account.getId(), ACCOUNT)
        .flatMap(count -> {
          BigDecimal commission = account.getTypeAccount().getCommission() == null
              ? BigDecimal.ZERO :
              account.getTypeAccount().getCommission();
          BigDecimal availableBalance = count >= setInteger(account
              .getTypeAccount().getMaxTransactions())
              ? account.getBalance().subtract(commission) :
              account.getBalance();
          account.setAvailableBalance(availableBalance);
          account.setTotalTransactions(count);

          return Mono.just(account);
        });
  }

  private Mono<Transaction> create(Transaction transaction) {
    return transactionRepository.save(transaction)
        .flatMap(x -> {
          logger.info("Created a new transaction with id = {}", x.getId());
          return Mono.just(x);
        });
  }

  private void updateAccountBalance(Transaction transaction, BigDecimal amount, int type) {
    BigDecimal finalAmount = type == ENTRY ? amount : amount.multiply(BigDecimal.valueOf(-1));
    accountService
        .updateAccount(transaction.getIdProduct().toString(), finalAmount);
  }

  private void updateCreditBalance(Transaction transaction, BigDecimal amount, int type) {
    BigDecimal finalAmount = type == ENTRY ? amount : amount.multiply(BigDecimal.valueOf(-1));
    creditService
        .updateCredit(transaction.getIdProduct().toString(), finalAmount);
  }

  private Integer setInteger(Integer value) {
    return value == null ? 0 : value;
  }
}
