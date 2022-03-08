package com.nttdata.transactions.model.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class FilterRequest {
    @NotNull(message = "Field start must be required")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate start;
    @NotNull(message = "Field end must be required")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate end;
}
