package com.nttdata.transactions.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * Transaction object.
 */
@Document("transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId id;
  private int collection;
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId idProduct;
  private String description;
  private int type;
  @JsonFormat(pattern = "dd/MM/yyyy")
  private LocalDateTime date;
  private int month;
  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal amount;
  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal fee;

  /**
   * Return transaction.
   *
   * @param collection Account or credit collection - use TransactionCollection constant
   * @param idProduct  Id of account or credit
   * @param description Description of the transaction
   * @param type       Type of transaction - use TransactionType constant
   * @param amount     Transaction amount
   * @param fee        Transaction fee
   */
  public Transaction(int collection, String idProduct,
                     String description, int type,
                     BigDecimal amount, BigDecimal fee) {
    this.collection = collection;
    this.idProduct = new ObjectId(idProduct);
    this.description = description;
    this.type = type;
    this.date = LocalDateTime.now();
    this.month = LocalDate.now().getMonthValue();
    this.amount = amount;
    this.fee = fee;
  }
}
