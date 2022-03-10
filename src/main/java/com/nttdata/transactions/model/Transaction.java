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
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId idProduct;
  private int collection;
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
   * @param idProduct  Id of account or credit
   * @param collection Account or credit collection - use TransactionCollection constan
   * @param type       Type of transaction - use TransactionType constant
   * @param amount     Transaction amount
   * @param fee        Transaction fee
   */
  public Transaction(String idProduct, int collection, int type,
                     BigDecimal amount, BigDecimal fee) {
    this.idProduct = new ObjectId(idProduct);
    this.collection = collection;
    this.type = type;
    this.date = LocalDateTime.now();
    this.month = LocalDate.now().getMonthValue();
    this.amount = amount;
    this.fee = fee;
  }
}
