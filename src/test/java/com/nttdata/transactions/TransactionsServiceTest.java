package com.nttdata.transactions;

import com.nttdata.transactions.dto.request.FilterRequest;
import com.nttdata.transactions.dto.request.TransactionRequest;
import com.nttdata.transactions.dto.response.AccountResponse;
import com.nttdata.transactions.dto.response.CreditResponse;
import com.nttdata.transactions.dto.response.TypeAccountResponse;
import com.nttdata.transactions.exceptions.customs.CustomInformationException;
import com.nttdata.transactions.exceptions.customs.CustomNotFoundException;
import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.repository.TransactionRepository;
import com.nttdata.transactions.service.AccountService;
import com.nttdata.transactions.service.CreditService;
import com.nttdata.transactions.service.TransactionServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static com.nttdata.transactions.utilities.Constants.AccountType.SAVING;
import static com.nttdata.transactions.utilities.Constants.TransactionCollection.ACCOUNT;
import static com.nttdata.transactions.utilities.Constants.TransactionCollection.CREDIT;
import static com.nttdata.transactions.utilities.Constants.TransactionType.ENTRY;
import static com.nttdata.transactions.utilities.Constants.TransactionType.EXIT;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TransactionsServiceTest {
  private static final String SUCCESS_MESSAGE = "Successful transaction";

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private TransactionServiceImpl transactionService;

  @Mock
  private AccountService accountService;

  @Mock
  private CreditService creditService;

  @Test
  void testListByAccountNumber() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    Transaction transaction = new Transaction();
    transaction.setType(ENTRY);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction.setAmount(BigDecimal.valueOf(200));

    Transaction transaction1 = new Transaction();
    transaction1.setType(EXIT);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction1.setAmount(BigDecimal.valueOf(100));

    ObjectId id = new ObjectId();
    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");

    var fluxTransaction = Flux.just(transaction, transaction1);
    var monoAccount = Mono.just(account);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.findByIdProductAndCollection(id, ACCOUNT)).thenReturn(fluxTransaction);

    var list = transactionService.listByAccountNumber("1234567890");
    StepVerifier
        .create(list)
        .expectSubscription()
        .expectNext(transaction)
        .expectNext(transaction1)
        .verifyComplete();
  }

  @Test
  void testListByAccountNumberNotFoundAccount() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    Transaction transaction = new Transaction();
    transaction.setType(ENTRY);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction.setAmount(BigDecimal.valueOf(200));

    Transaction transaction1 = new Transaction();
    transaction1.setType(EXIT);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction1.setAmount(BigDecimal.valueOf(100));

    ObjectId id = new ObjectId();
    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");

    when(accountService.findAccount("1234567890")).thenReturn(Mono.error(new CustomNotFoundException("Account 1234567890 not found")));

    var list = transactionService.listByAccountNumber("1234567890");
    StepVerifier
        .create(list)
        .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundException &&
            throwable.getMessage().equals("Account 1234567890 not found"))
        .verify();
  }

  @Test
  void testListByCreditNumber() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    Transaction transaction = new Transaction();
    transaction.setType(ENTRY);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction.setAmount(BigDecimal.valueOf(200));

    Transaction transaction1 = new Transaction();
    transaction1.setType(EXIT);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction1.setAmount(BigDecimal.valueOf(100));

    ObjectId id = new ObjectId();
    CreditResponse credit = new CreditResponse();
    credit.setId(id.toString());
    credit.setNumber("1234567890");

    var fluxTransaction = Flux.just(transaction, transaction1);
    var monoCredit = Mono.just(credit);
    when(creditService.findCredit("1234567890")).thenReturn(monoCredit);
    when(transactionRepository.findByIdProductAndCollection(id, CREDIT)).thenReturn(fluxTransaction);

    var list = transactionService.listByCreditNumber("1234567890");
    StepVerifier
        .create(list)
        .expectSubscription()
        .expectNext(transaction)
        .expectNext(transaction1)
        .verifyComplete();
  }

  @Test
  void testListAccountTransactionsWithCommission() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    Transaction transaction = new Transaction();
    transaction.setType(ENTRY);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction.setAmount(BigDecimal.valueOf(200));

    Transaction transaction1 = new Transaction();
    transaction1.setType(EXIT);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatter));
    transaction1.setAmount(BigDecimal.valueOf(100));

    ObjectId id = new ObjectId();
    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");

    FilterRequest request = new FilterRequest();
    request.setStart(LocalDate.parse("01/03/2022", formatterDate));
    request.setEnd(LocalDate.parse("31/03/2022", formatterDate));

    var fluxTransaction = Flux.just(transaction, transaction1);
    var monoAccount = Mono.just(account);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.listWithTaxByIdProductAndCollection(request.getStart(), request.getEnd(), id.toString(), ACCOUNT)).thenReturn(fluxTransaction);

    var list = transactionService.listAccountTransactionsWithCommission("1234567890", request);
    StepVerifier
        .create(list)
        .expectSubscription()
        .expectNext(transaction)
        .expectNext(transaction1)
        .verifyComplete();
  }

  @Test
  void testDeposit() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.valueOf(3), null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.ZERO);

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(ENTRY);
    transaction.setAmount(BigDecimal.valueOf(2000));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(0L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));

    var resAccount = transactionService.depositAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testDepositWithoutMaxTransaction() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, null, null, null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.ZERO);

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(ENTRY);
    transaction.setAmount(BigDecimal.valueOf(2000));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(0L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));

    var resAccount = transactionService.depositAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testDepositWithCommission() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.valueOf(2), null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.ZERO);

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(ENTRY);
    transaction.setAmount(BigDecimal.valueOf(2000));
    transaction.setCommission(BigDecimal.valueOf(3));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(20L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));

    var resAccount = transactionService.depositAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testDepositWithCommission0() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.ZERO, null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.ZERO);

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(ENTRY);
    transaction.setAmount(BigDecimal.valueOf(2000));
    transaction.setCommission(BigDecimal.valueOf(3));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(20L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));

    var resAccount = transactionService.depositAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testWithdrawal() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.ZERO, null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.valueOf(4000));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(EXIT);
    transaction.setAmount(BigDecimal.valueOf(2000));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(0L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));

    var resAccount = transactionService.withdrawalAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testWithdrawalWithCommission() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.valueOf(1.25), null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.valueOf(4000));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(EXIT);
    transaction.setAmount(BigDecimal.valueOf(2000));
    transaction.setCommission(BigDecimal.valueOf(3));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(10L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));

    var resAccount = transactionService.withdrawalAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testWithdrawalWithCommissionNull() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, null, null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.valueOf(4000));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(EXIT);
    transaction.setAmount(BigDecimal.valueOf(2000));
    transaction.setCommission(BigDecimal.valueOf(3));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(10L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));

    var resAccount = transactionService.withdrawalAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testWithdrawalNotEnoughBalance() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.valueOf(3), null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.valueOf(1000));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(EXIT);
    transaction.setAmount(BigDecimal.valueOf(2000));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccount = Mono.just(account);
    var monoCount = Mono.just(0L);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccount);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);

    var resAccount = transactionService.withdrawalAccount("1234567890", request);
    StepVerifier
        .create(resAccount)
        .expectErrorMatches(throwable -> throwable instanceof CustomInformationException &&
            throwable.getMessage().equals("You do not have a balance to carry out this transaction"))
        .verify();
  }

  @Test
  void testWithdrawalWithDebitCard() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.ZERO, null);
    TypeAccountResponse typeAccount1 = new TypeAccountResponse(SAVING, null, 5, null, null);
    TypeAccountResponse typeAccount2 = new TypeAccountResponse(SAVING, null, 1, BigDecimal.valueOf(2), null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.valueOf(100));

    AccountResponse account1 = new AccountResponse();
    account1.setId(id.toString());
    account1.setPosition(1);
    account1.setNumber("1234567891");
    account1.setDebitCard("4420652012504888");
    account1.setTypeAccount(typeAccount1);
    account1.setBalance(BigDecimal.valueOf(200));

    AccountResponse account2 = new AccountResponse();
    account2.setId(id.toString());
    account2.setPosition(1);
    account2.setNumber("1234567892");
    account2.setDebitCard("4420652012504888");
    account2.setTypeAccount(typeAccount2);
    account2.setBalance(BigDecimal.valueOf(100));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(EXIT);
    transaction.setAmount(BigDecimal.valueOf(200));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(200));

    var fluxAccounts = Flux.just(account, account1, account2);
    var monoCount = Mono.just(5L);
    var monoTransaction = Mono.just(transaction);
    when(accountService.listByDebitCard("4420652012504888")).thenReturn(fluxAccounts);
    when(transactionRepository.countByIdProductAndCollection(account.getId(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(200));

    var result = transactionService.withdrawalFromDebitCard("4420652012504888", request);
    StepVerifier
        .create(result)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testWithdrawalWithDebitCardNotEnoughBalance() {
    ObjectId id = new ObjectId();

    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.ZERO, null);

    AccountResponse account = new AccountResponse();
    account.setId(id.toString());
    account.setPosition(1);
    account.setNumber("1234567890");
    account.setDebitCard("4420652012504888");
    account.setTypeAccount(typeAccount);
    account.setBalance(BigDecimal.valueOf(100));

    AccountResponse account1 = new AccountResponse();
    account1.setId(id.toString());
    account1.setPosition(1);
    account1.setNumber("1234567891");
    account1.setDebitCard("4420652012504888");
    account1.setTypeAccount(typeAccount);
    account1.setBalance(BigDecimal.valueOf(200));

    AccountResponse account2 = new AccountResponse();
    account2.setId(id.toString());
    account2.setPosition(1);
    account2.setNumber("1234567892");
    account2.setDebitCard("4420652012504888");
    account2.setTypeAccount(typeAccount);
    account2.setBalance(BigDecimal.valueOf(100));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(EXIT);
    transaction.setAmount(BigDecimal.valueOf(500));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(500));

    var fluxAccounts = Flux.just(account, account1, account2);
    var monoCount = Mono.just(0L);
    when(accountService.listByDebitCard("4420652012504888")).thenReturn(fluxAccounts);
    when(transactionRepository.countByIdProductAndCollection(account.getId(), ACCOUNT)).thenReturn(monoCount);

    var result = transactionService.withdrawalFromDebitCard("4420652012504888", request);
    StepVerifier
        .create(result)
        .expectErrorMatches(throwable -> throwable instanceof CustomInformationException &&
            throwable.getMessage().equals("You do not have enough balance in your accounts"))
        .verify();
  }

  @Test
  void testTransferBetweenAccounts() {
    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.valueOf(3), null);

    ObjectId id = new ObjectId();
    AccountResponse accountExit = new AccountResponse();
    accountExit.setId(id.toString());
    accountExit.setNumber("1234567890");
    accountExit.setTypeAccount(typeAccount);
    accountExit.setBalance(BigDecimal.valueOf(4000));

    AccountResponse accountEntry = new AccountResponse();
    accountEntry.setId(id.toString());
    accountEntry.setNumber("1234567891");
    accountEntry.setTypeAccount(typeAccount);
    accountEntry.setBalance(BigDecimal.valueOf(1000));

    Transaction exit = new Transaction();
    exit.setIdProduct(id);
    exit.setCollection(ACCOUNT);
    exit.setType(EXIT);
    exit.setAmount(BigDecimal.valueOf(2000));

    Transaction entry = new Transaction();
    entry.setIdProduct(id);
    entry.setCollection(ACCOUNT);
    entry.setType(ENTRY);
    entry.setAmount(BigDecimal.valueOf(2000));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccountExit = Mono.just(accountExit);
    var monoAccountEntry = Mono.just(accountEntry);
    var monoCount = Mono.just(0L);
    var monoTransactionExit = Mono.just(exit);
    var monoTransactionEntry = Mono.just(entry);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccountExit);
    when(accountService.findAccount("1234567891")).thenReturn(monoAccountEntry);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransactionExit);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));
    when(transactionRepository.save(any())).thenReturn(monoTransactionEntry);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));


    var resAccount = transactionService.transferBetweenAccounts("1234567890", "1234567891", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testTransferBetweenAccountsWithCommission() {
    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.valueOf(3), null);

    ObjectId id = new ObjectId();
    AccountResponse accountExit = new AccountResponse();
    accountExit.setId(id.toString());
    accountExit.setNumber("1234567890");
    accountExit.setTypeAccount(typeAccount);
    accountExit.setBalance(BigDecimal.valueOf(4000));

    AccountResponse accountEntry = new AccountResponse();
    accountEntry.setId(id.toString());
    accountEntry.setNumber("1234567891");
    accountEntry.setTypeAccount(typeAccount);
    accountEntry.setBalance(BigDecimal.valueOf(1000));

    Transaction exit = new Transaction();
    exit.setIdProduct(id);
    exit.setCollection(ACCOUNT);
    exit.setType(EXIT);
    exit.setAmount(BigDecimal.valueOf(2000));
    exit.setCommission(BigDecimal.valueOf(3));

    Transaction entry = new Transaction();
    entry.setIdProduct(id);
    entry.setCollection(ACCOUNT);
    entry.setType(ENTRY);
    entry.setAmount(BigDecimal.valueOf(2000));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccountExit = Mono.just(accountExit);
    var monoAccountEntry = Mono.just(accountEntry);
    var monoCount = Mono.just(10L);
    var monoTransactionExit = Mono.just(exit);
    var monoTransactionEntry = Mono.just(entry);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccountExit);
    when(accountService.findAccount("1234567891")).thenReturn(monoAccountEntry);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);
    when(transactionRepository.save(any())).thenReturn(monoTransactionExit);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));
    when(transactionRepository.save(any())).thenReturn(monoTransactionEntry);
    doNothing().when(accountService).updateAccount(id.toString(), BigDecimal.valueOf(2000));


    var resAccount = transactionService.transferBetweenAccounts("1234567890", "1234567891", request);
    StepVerifier
        .create(resAccount)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testTransferBetweenAccountsNotEnoughBalance() {
    TypeAccountResponse typeAccount = new TypeAccountResponse(SAVING, null, 5, BigDecimal.valueOf(3), null);

    ObjectId id = new ObjectId();
    AccountResponse accountExit = new AccountResponse();
    accountExit.setId(id.toString());
    accountExit.setNumber("1234567890");
    accountExit.setTypeAccount(typeAccount);
    accountExit.setBalance(BigDecimal.valueOf(500));

    AccountResponse accountEntry = new AccountResponse();
    accountEntry.setId(id.toString());
    accountEntry.setNumber("1234567891");
    accountEntry.setTypeAccount(typeAccount);
    accountEntry.setBalance(BigDecimal.valueOf(1000));

    Transaction exit = new Transaction();
    exit.setIdProduct(id);
    exit.setCollection(ACCOUNT);
    exit.setType(EXIT);
    exit.setAmount(BigDecimal.valueOf(2000));

    Transaction entry = new Transaction();
    entry.setIdProduct(id);
    entry.setCollection(ACCOUNT);
    entry.setType(ENTRY);
    entry.setAmount(BigDecimal.valueOf(2000));

    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    var monoAccountExit = Mono.just(accountExit);
    var monoAccountEntry = Mono.just(accountEntry);
    var monoCount = Mono.just(0L);
    when(accountService.findAccount("1234567890")).thenReturn(monoAccountExit);
    when(accountService.findAccount("1234567891")).thenReturn(monoAccountEntry);
    when(transactionRepository.countByIdProductAndCollection(id.toString(), ACCOUNT)).thenReturn(monoCount);


    var resAccount = transactionService.transferBetweenAccounts("1234567890", "1234567891", request);
    StepVerifier
        .create(resAccount)
        .expectErrorMatches(throwable -> throwable instanceof CustomInformationException &&
            throwable.getMessage().equals("You do not have a balance to carry out this transaction"))
        .verify();
  }

  @Test
  void testPayCredit() {
    ObjectId id = new ObjectId();

    CreditResponse credit = new CreditResponse();
    credit.setId(id.toString());
    credit.setNumber("1234567890");
    credit.setCreditBalance(BigDecimal.valueOf(8000));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(ENTRY);
    transaction.setAmount(BigDecimal.valueOf(2000));

    var monoCredit = Mono.just(credit);
    var monoTransaction = Mono.just(transaction);
    when(creditService.findCredit("1234567890")).thenReturn(monoCredit);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(creditService).updateCredit(id.toString(), BigDecimal.valueOf(2000));

    var result = transactionService.payCredit("1234567890", BigDecimal.valueOf(2000));
    StepVerifier
        .create(result)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }

  @Test
  void testSpendCredit() {
    ObjectId id = new ObjectId();

    CreditResponse credit = new CreditResponse();
    credit.setId(id.toString());
    credit.setNumber("1234567890");
    credit.setCreditTotal(BigDecimal.valueOf(500));
    credit.setCreditBalance(BigDecimal.valueOf(7500));

    Transaction transaction = new Transaction();
    transaction.setIdProduct(id);
    transaction.setCollection(ACCOUNT);
    transaction.setType(EXIT);
    transaction.setAmount(BigDecimal.valueOf(500));

    var monoCredit = Mono.just(credit);
    var monoTransaction = Mono.just(transaction);
    when(creditService.findCredit("1234567890")).thenReturn(monoCredit);
    when(transactionRepository.save(any())).thenReturn(monoTransaction);
    doNothing().when(creditService).updateCredit(id.toString(), BigDecimal.valueOf(2000));

    var result = transactionService.spendCredit("1234567890", BigDecimal.valueOf(2000));
    StepVerifier
        .create(result)
        .expectSubscription()
        .consumeNextWith(x -> {
          Assertions.assertNotNull(x);
          Assertions.assertEquals(SUCCESS_MESSAGE, x);
        })
        .verifyComplete();
  }
}
