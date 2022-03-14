package com.nttdata.transactions.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class TransactionRequest {
  private String description;
  @NotNull(message = "Field amount must be required")
  @Min(value = 1, message = "The minimum amount must be 1")
  private BigDecimal amount;
}
