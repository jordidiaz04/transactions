package com.nttdata.transactions.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Credit {
    private String id;
    private Client client;
    private String number;
    private boolean active;
    private int type;
    private BigDecimal credit_total;
    private BigDecimal credit_balance;
}
