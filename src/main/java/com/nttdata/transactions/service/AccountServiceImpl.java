package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.response.Account;
import com.nttdata.transactions.exceptions.customs.CustomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AccountServiceImpl implements AccountService {
    @Value("${backend.service.account}")
    private String urlAccount;

    @Autowired
    @Qualifier("wcLoadBalanced")
    private WebClient.Builder webClient;

    @Override
    public Mono<Account> findAccount(String number) {
        return webClient
                .build()
                .get()
                .uri(urlAccount + "/get/number/{number}", number)
                .retrieve()
                .onStatus(NOT_FOUND::equals, response -> Mono.error(new CustomNotFoundException("Account " + number + " not found")))
                .bodyToMono(Account.class);
    }

    @Override
    public void updateAccount(String id, BigDecimal amount) {
        webClient
                .build()
                .put()
                .uri(urlAccount + "/balance/{id}/amount/{amount}", id, amount)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
