package com.bigsoftware.jh_simple_pos.data;

/**
 * Created by shanesepac on 4/15/19.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * This class is used to store selected items into the local SQLite database.
 * StoreConfig data should be obtained from system preferences (no database required)
 */


public class PurchaseDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "PurchaseDBHelper";
    private SQLiteDatabase db;
    private SQLiteDatabase rdb;

    @Override
    public synchronized void close() {
        db.close();
        rdb.close();
        super.close();
    }

    public PurchaseDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
        rdb = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //TODO: Create database upgrade code
        //sqLiteDatabase.execSQL(DELETE_TABLE);
        //sqLiteDatabase.execSQL(CREATE_TABLE_NEW);
    }

    public static final class PurchaseDBEntry {

        public static final String TABLE_NAME = "Purchases";
        public static final String COLUMN_NAME_ID = "ID";
        public static final String COLUMN_NAME_ITEM_NAME = "Item_Name";
        public static final String COLUMN_NAME_DATETIME = "Datetime";
        public static final String COLUMN_NAME_STORE_ID = "Store_ID";

        private String itemName;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public long getDateTime() {
            return dateTime;
        }

        public void setDateTime(long dateTime) {
            this.dateTime = dateTime;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }

        private long dateTime;
        private String storeName;

    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "purchasedata.db";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + PurchaseDBEntry.TABLE_NAME + " (" +
                    PurchaseDBEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
                    PurchaseDBEntry.COLUMN_NAME_ITEM_NAME + " TEXT, " +
                    PurchaseDBEntry.COLUMN_NAME_DATETIME + " TEXT, " +
                    PurchaseDBEntry.COLUMN_NAME_STORE_ID + " TEXT)";

    private static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + PurchaseDBEntry.TABLE_NAME;


    public void insertData(String itemName, String storeName, Runnable success, Runnable failure){

        ContentValues values = new ContentValues();
        values.put(PurchaseDBEntry.COLUMN_NAME_ITEM_NAME, itemName);
        values.put(PurchaseDBEntry.COLUMN_NAME_DATETIME, Utils.getDateTime());
        values.put(PurchaseDBEntry.COLUMN_NAME_STORE_ID, storeName);

        long val = db.insert(PurchaseDBEntry.TABLE_NAME, null, values);

        if(val!=-1){
            success.run();
        }else{
            failure.run();
        }

    }

    public interface ICompletion{
        void success(ArrayList<PurchaseDBEntry> entries);
        void failure();
    }

    public void obtainStoreData(String storeName, ICompletion comp){

        ArrayList<PurchaseDBEntry> entryList = new ArrayList<>();
        String selection = PurchaseDBEntry.COLUMN_NAME_STORE_ID + " = ?";
        String[] selectionArgs = { storeName };

        Cursor cursor = rdb.query(
                PurchaseDBEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null            // The sort order
        );

        try {
            while(cursor.moveToNext()) {
                PurchaseDBEntry entry = new PurchaseDBEntry();
                entry.setStoreName(cursor.getString(cursor.getColumnIndexOrThrow(PurchaseDBEntry.COLUMN_NAME_STORE_ID)));
                entry.setDateTime(cursor.getLong(cursor.getColumnIndexOrThrow(PurchaseDBEntry.COLUMN_NAME_DATETIME)));
                entry.setItemName(cursor.getString(cursor.getColumnIndexOrThrow(PurchaseDBEntry.COLUMN_NAME_ITEM_NAME)));
                entryList.add(entry);
            }
            comp.success(entryList);
        }
        catch(Exception ex){
            ex.printStackTrace();
            comp.failure();
        }
        finally {
            cursor.close();
        }
    }

    public void obtainAllStoreData(ICompletion comp){
        ArrayList<PurchaseDBEntry> entryList = new ArrayList<>();

        Cursor cursor = rdb.query(
                PurchaseDBEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null            // The sort order
        );

        try {
            while(cursor.moveToNext()) {
                PurchaseDBEntry entry = new PurchaseDBEntry();
                entry.setStoreName(cursor.getString(cursor.getColumnIndexOrThrow(PurchaseDBEntry.COLUMN_NAME_STORE_ID)));
                entry.setDateTime(cursor.getLong(cursor.getColumnIndexOrThrow(PurchaseDBEntry.COLUMN_NAME_DATETIME)));
                entry.setItemName(cursor.getString(cursor.getColumnIndexOrThrow(PurchaseDBEntry.COLUMN_NAME_ITEM_NAME)));
                entryList.add(entry);
            }
            comp.success(entryList);
        }
        catch(Exception ex){
            ex.printStackTrace();
            comp.failure();
        }
        finally {
            cursor.close();
        }
    }

    /***
     *
     * @return True if entry was deleted, false if no entry was deleted
     */
    public boolean deleteLastEntry(){

        long currTime = System.currentTimeMillis();

        Cursor cursor = getAllStores();

        long latestEntry = 0;
        if(cursor != null && cursor.getCount() > 0){
            long cursorTime = 0;
            while(cursor.moveToNext()){
                cursorTime = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(PurchaseDBEntry.COLUMN_NAME_DATETIME)));
                if(cursorTime > latestEntry){
                    latestEntry = cursorTime;
                }
            }
            cursor.close();

            return deleteEntry(latestEntry) > 0;
        }
        return false;
    }

    private int deleteEntry(long dateTime){
        // Define 'where' part of query.
        String selection = PurchaseDBEntry.COLUMN_NAME_DATETIME + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(dateTime) };
        // Issue SQL statement.
        int deletedRows = db.delete(PurchaseDBEntry.TABLE_NAME, selection, selectionArgs);

        if(deletedRows>0){
            Log.d(TAG, "updateEntry: updated " + deletedRows + " entries.");
        }
        else{
            Log.d(TAG, "updateOrInsertEntry: Added new store to db.");
        }

        return deletedRows;
    }

    public Cursor getAllStores() {

        Cursor res = rdb.rawQuery("SELECT * FROM " + PurchaseDBEntry.TABLE_NAME, new String[]{});

        return res;
    }

    public Cursor querySingleStore(String name){

        String selection = PurchaseDBEntry.COLUMN_NAME_STORE_ID + " = ?";
        String[] selectionArgs = { name };

        return rdb.query(
                PurchaseDBEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null            // The sort order
        );
    }

    public void deleteTable(){
        db.execSQL(DELETE_TABLE);
    }

}
