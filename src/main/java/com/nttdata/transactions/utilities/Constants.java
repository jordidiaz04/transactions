package com.nttdata.transactions.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
  /**
   * Transaction types.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class TransactionType {
    public static final int ENTRY = 1;
    public static final int EXIT = 2;
  }

  /**
   * Transaction collections.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class TransactionCollection {
    public static final int ACCOUNT = 1;
    public static final int CREDIT = 2;
  }

  /**
   * Account types.
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class AccountType {
    public static final int SAVING = 1;
    public static final int CHECKING = 2;
    public static final int FIXED_TERM = 3;
  }
}
