package com.nttdata.transactions.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {
    private String id;
    private String number;
    private Client client;
    private TypeAccount typeAccount;
    private List<String> holders;
    private List<String> signatories;
    private BigDecimal balance = BigDecimal.valueOf(0);
    private boolean status;
}
