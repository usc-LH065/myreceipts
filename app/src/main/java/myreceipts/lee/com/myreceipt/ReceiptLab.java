package myreceipts.lee.com.myreceipt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import myreceipts.lee.com.myreceipt.database.ReceiptBaseHelper;
import myreceipts.lee.com.myreceipt.database.ReceiptCursorWrapper;
import myreceipts.lee.com.myreceipt.database.ReceiptDBSchema.ReceiptTable;

public class ReceiptLab {
    private static ReceiptLab sReceiptLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    /**
     * @param context of the application state
     * @return existing or new ReceiptLab (If one doesn't exist)
     */
    public static ReceiptLab get(Context context) {
        if (sReceiptLab == null) {
            sReceiptLab = new ReceiptLab(context);
        }

        return sReceiptLab;
    }

    // Adds a Receipt to the list of Receipts
    public void addReceipt(Receipt c) {
        ContentValues values = getContentValues(c);

        mDatabase.insert(ReceiptTable.NAME, null, values);
    }

    public void updateReceipt(Receipt Receipt) {
        String uuidString = Receipt.getId().toString();
        ContentValues values = getContentValues(Receipt);

        mDatabase.update(ReceiptTable.NAME, values,
                ReceiptTable.Cols.UUID + "=?",
                new String[]{uuidString});
    }

    public void deleteReceipt(Receipt Receipt) {
        mDatabase.delete(
                ReceiptTable.NAME,
                ReceiptTable.Cols.UUID + "=?",
                new String[] {Receipt.getId().toString()}
        );
    }

    public List<Receipt> getReceipts() {
        List<Receipt> Receipts = new ArrayList<>();

        ReceiptCursorWrapper cursor = queryReceipts(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Receipts.add(cursor.getReceipt());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return Receipts;
    }

    public Receipt getReceipt(UUID id) {
        // otherwise no Receipt by that id
        ReceiptCursorWrapper cursor = queryReceipts(
                ReceiptTable.Cols.UUID + "=?",
                new String[]{id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getReceipt();
        } finally {
            cursor.close();
        }
    }

    public File getPhotoFile(Receipt Receipt) {
        File filesDir = mContext.getFilesDir();

        return new File(filesDir, Receipt.getPhotoFilename());
    }

    private static ContentValues getContentValues(Receipt Receipt) {
        ContentValues values = new ContentValues();
        values.put(ReceiptTable.Cols.UUID, Receipt.getId().toString());
        values.put(ReceiptTable.Cols.TITLE, Receipt.getTitle());
        values.put(ReceiptTable.Cols.SHOPNAME, Receipt.getShopName());
        values.put(ReceiptTable.Cols.DATE, Receipt.getDate().getTime());
        values.put(ReceiptTable.Cols.COMMENT, Receipt.getComment());
        values.put(ReceiptTable.Cols.LAT, Receipt.getLat());
        values.put(ReceiptTable.Cols.LON, Receipt.getLon());



        return values;
    }

    private ReceiptCursorWrapper queryReceipts(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ReceiptTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null // orderBy
        );

        return new ReceiptCursorWrapper(cursor);
    }

    /**
     * Private constructor prevents more than one instance
     * and can only be created from this class
     */
    private ReceiptLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ReceiptBaseHelper(mContext)
                .getWritableDatabase();
    }
}
