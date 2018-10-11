package myreceipts.lee.com.myreceipt.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import myreceipts.lee.com.myreceipt.database.ReceiptDBSchema.ReceiptTable;

public class ReceiptBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "receiptBase.db";

    public ReceiptBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ReceiptTable.NAME + "(" +
                " _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReceiptTable.Cols.UUID + " INTEGER, " +
                ReceiptTable.Cols.TITLE + " TEXT, " +
                ReceiptTable.Cols.DATE + " INTEGER, " +
                ReceiptTable.Cols.SHOPNAME + " TEXT," +
                ReceiptTable.Cols.LAT + " DOUBLE," +
                ReceiptTable.Cols.LON + " DOUBLE," +
                ReceiptTable.Cols.COMMENT + " TEXT" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
