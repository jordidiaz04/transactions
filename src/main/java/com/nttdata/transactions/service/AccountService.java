package com.nttdata.transactions.service;

import com.nttdata.transactions.dto.response.Account;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService {
    Mono<Account> findAccount(String number);
    void updateAccount(String id, BigDecimal amount);
}
