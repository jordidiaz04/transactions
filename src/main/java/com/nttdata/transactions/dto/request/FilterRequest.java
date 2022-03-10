package com.nttdata.transactions.dto.request;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Filter request only start and end date.
 */
@Data
public class FilterRequest {
  @NotNull(message = "Field start must be required")
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private LocalDate start;
  @NotNull(message = "Field end must be required")
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private LocalDate end;
}
