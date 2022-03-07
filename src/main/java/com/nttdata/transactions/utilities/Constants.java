package com.nttdata.transactions.utilities;

public class Constants {
    public static final class TransactionType
    {
        public static int DEPOSIT = 1;
        public static int WITHDRAWALS = 2;
        public static int TRANSFERS = 3;
    }

    public static final class TransactionCollection
    {
        public static int ACCOUNT = 1;
        public static int CREDIT = 2;
    }

    public static final class AccountType
    {
        public static int SAVING = 1;
        public static int CHECKING = 2;
        public static int FIXED_TERM = 3;
    }
}
