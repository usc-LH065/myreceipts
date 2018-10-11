package myreceipts.lee.com.myreceipt.database;

public class ReceiptDBSchema {
    public static final class ReceiptTable{
        public static final String NAME = "receipts";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SHOPNAME = "shopName";
            public static final String LAT = "lat";
            public static final String LON = "lon";
            public static final String COMMENT = "comment";
        }
    }
}
