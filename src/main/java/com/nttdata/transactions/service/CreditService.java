package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.response.Credit;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CreditService {
    Mono<Credit> findCredit(String number);
    void updateCredit(String id, BigDecimal amount);
}
