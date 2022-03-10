package com.nttdata.transactions.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Credit object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditResponse {
  private String id;
  private ClientResponse client;
  private String number;
  private boolean active;
  private int type;
  private BigDecimal creditTotal;
  private BigDecimal creditBalance;
}
