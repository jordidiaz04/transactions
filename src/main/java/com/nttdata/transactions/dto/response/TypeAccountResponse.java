package com.nttdata.transactions.dto.response;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TypeAccount object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypeAccountResponse {
  private int option;
  private BigDecimal maintenance;
  private Integer maxTransactions;
  private BigDecimal commission;
  private Integer day;
}
