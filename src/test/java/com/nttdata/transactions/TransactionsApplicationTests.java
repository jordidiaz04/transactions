package com.nttdata.transactions;

import com.nttdata.transactions.controller.TransactionController;
import com.nttdata.transactions.dto.request.FilterRequest;
import com.nttdata.transactions.dto.request.TransactionRequest;
import com.nttdata.transactions.model.Transaction;
import com.nttdata.transactions.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.nttdata.transactions.utilities.Constants.TransactionType.ENTRY;
import static com.nttdata.transactions.utilities.Constants.TransactionType.EXIT;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@WebFluxTest(TransactionController.class)
class TransactionsApplicationTests {
  private static final String SUCCESS_MESSAGE = "Successful transaction";

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private TransactionService transactionService;

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

    var fluxTransaction = Flux.just(transaction, transaction1);
    when(transactionService.listByAccountNumber("1234567890")).thenReturn(fluxTransaction);

    var responseBody = webTestClient
        .get()
        .uri("/get/account/1234567890")
        .exchange()
        .expectStatus().isOk()
        .returnResult(Transaction.class)
        .getResponseBody();

    StepVerifier
        .create(responseBody)
        .expectSubscription()
        .expectNext(transaction)
        .expectNext(transaction1)
        .verifyComplete();
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

    var fluxTransaction = Flux.just(transaction, transaction1);
    when(transactionService.listByCreditNumber("1234567890")).thenReturn(fluxTransaction);

    var responseBody = webTestClient
        .get()
        .uri("/get/credit/1234567890")
        .exchange()
        .expectStatus().isOk()
        .returnResult(Transaction.class)
        .getResponseBody();

    StepVerifier
        .create(responseBody)
        .expectSubscription()
        .expectNext(transaction)
        .expectNext(transaction1)
        .verifyComplete();
  }

  @Test
  void testListAccountTransactionsWithCommission() {
    DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    Transaction transaction = new Transaction();
    transaction.setType(ENTRY);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatterDateTime));
    transaction.setAmount(BigDecimal.valueOf(200));

    Transaction transaction1 = new Transaction();
    transaction1.setType(EXIT);
    transaction.setDate(LocalDateTime.parse("18/03/2022 14:15:20", formatterDateTime));
    transaction1.setAmount(BigDecimal.valueOf(100));

    FilterRequest request = new FilterRequest();
    request.setStart(LocalDate.parse("01/03/2022", formatterDate));
    request.setEnd(LocalDate.parse("31/03/2022", formatterDate));

    var fluxTransaction = Flux.just(transaction, transaction1);
    when(transactionService.listAccountTransactionsWithCommission("1234567890", request)).thenReturn(fluxTransaction);

    var responseBody = webTestClient
        .get()
        .uri("/get/account/1234567890/commissions?start=01/03/2022&end=31/03/2022")
        .exchange()
        .expectStatus().isOk()
        .returnResult(Transaction.class)
        .getResponseBody();

    StepVerifier
        .create(responseBody)
        .expectSubscription()
        .expectNext(transaction)
        .expectNext(transaction1)
        .verifyComplete();
  }

  @Test
  void testDeposit() {
    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.depositAccount("1234567890", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/deposit/account/1234567890")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testDeposit2() {
    TransactionRequest request = new TransactionRequest();
    request.setDescription("Ingreso de dinero");
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.depositAccount("1234567890", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/deposit/account/1234567890")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testDepositWithoutAmount() {
    TransactionRequest request = new TransactionRequest();

    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.depositAccount("1234567890", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/deposit/account/1234567890")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void testWithdrawal() {
    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.withdrawalAccount("1234567890", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/withdrawal/account/1234567890")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testWithdrawal2() {
    TransactionRequest request = new TransactionRequest();
    request.setDescription("Retiro de cuenta");
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.withdrawalAccount("1234567890", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/withdrawal/account/1234567890")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testWithdrawalDebitCard() {
    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.withdrawalFromDebitCard("1234567890", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/withdrawal/debitCard/1234567890")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testWithdrawalDebitCard2() {
    TransactionRequest request = new TransactionRequest();
    request.setDescription("Retiro por tarjeta");
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.withdrawalFromDebitCard("1234567890", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/withdrawal/debitCard/1234567890")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testTransfer() {
    TransactionRequest request = new TransactionRequest();
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just("Successful transaction").map(x -> x);
    when(transactionService.transferBetweenAccounts("1234567890", "1234567891", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/transfer/account/1234567890/account/1234567891")
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testTransfer2() {
    TransactionRequest request = new TransactionRequest();
    request.setDescription("Transferencia a tercero");
    request.setAmount(BigDecimal.valueOf(2000));

    Mono<String> monoResult = Mono.just("Successful transaction").map(x -> x);
    when(transactionService.transferBetweenAccounts("1234567890", "1234567891", request)).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/transfer/account/1234567890/account/1234567891")
        .body(Mono.just(request), TransactionRequest.class)
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testPay() {
    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.payCredit("1234567890", BigDecimal.valueOf(200))).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/pay/credit/1234567890?amount=200")
        .exchange()
        .expectStatus().isCreated();
  }

  @Test
  void testSpend() {
    Mono<String> monoResult = Mono.just(SUCCESS_MESSAGE);
    when(transactionService.spendCredit("1234567890", BigDecimal.valueOf(200))).thenReturn(monoResult);

    webTestClient
        .post()
        .uri("/spend/credit/1234567890?amount=100")
        .exchange()
        .expectStatus().isCreated();
  }
}
