package com.nttdata.transactions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeAccount {
    private int option;
    private Integer maxTransactions;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal maintenanceFee;
    private BigDecimal tax;
    private Integer day;
}
