package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.response.Credit;
import com.nttdata.transactions.exceptions.customs.CustomNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CreditServiceImpl implements CreditService {
    @Value("${backend.service.credit}")
    private String urlCredit;

    @Autowired
    @Qualifier("wcLoadBalanced")
    private WebClient.Builder webClient;

    @Override
    public Mono<Credit> findCredit(String number) {
        return webClient
                .build()
                .get()
                .uri(urlCredit + "/number/{number}", number)
                .retrieve()
                .onStatus(NOT_FOUND::equals, response -> Mono.error(new CustomNotFoundException("Credit " + number + " not found")))
                .bodyToMono(Credit.class);
    }

    @Override
    public void updateCredit(String id, BigDecimal amount) {
        webClient
                .build()
                .put()
                .uri(urlCredit + "/balance/{id}/amount/{amount}", id, amount)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
