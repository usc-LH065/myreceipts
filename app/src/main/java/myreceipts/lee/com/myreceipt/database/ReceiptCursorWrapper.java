package myreceipts.lee.com.myreceipt.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.sql.Date;
import java.util.UUID;

import myreceipts.lee.com.myreceipt.Receipt;
import myreceipts.lee.com.myreceipt.database.ReceiptDBSchema.ReceiptTable;

public class ReceiptCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ReceiptCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Receipt getReceipt(){

        String uuidString = getString(getColumnIndex(ReceiptTable.Cols.UUID));
        String title = getString(getColumnIndex(ReceiptTable.Cols.TITLE));
        String comment = getString(getColumnIndex(ReceiptTable.Cols.COMMENT));
        String shopname = getString(getColumnIndex(ReceiptTable.Cols.SHOPNAME));
        long date = getLong(getColumnIndex(ReceiptTable.Cols.DATE));
        double lat = getDouble(getColumnIndex(ReceiptTable.Cols.LAT));
        double lon = getDouble(getColumnIndex(ReceiptTable.Cols.LON));

        Receipt receipt = new Receipt();
        receipt.setId(UUID.fromString(uuidString));
        receipt.setTitle(title);
        receipt.setComment(comment);
        receipt.setShopName(shopname);
        receipt.setDate(new Date(date));
        receipt.setLat(lat);
        receipt.setLon(lon);

        return receipt;
    }
}
