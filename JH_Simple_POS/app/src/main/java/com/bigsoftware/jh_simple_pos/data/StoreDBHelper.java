package com.bigsoftware.jh_simple_pos.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by shanesepac on 4/15/19.
 */

public class StoreDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "StoreDBHelper";
    public static int DATABASE_VERSION = 1;
    public static String DATABASE_NAME = "StoreInfo.db";

    public SQLiteDatabase rdb;
    public SQLiteDatabase db;

    @Override
    public synchronized void close() {
        rdb.close();
        db.close();
        super.close();
    }

    public StoreDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        rdb = getReadableDatabase();
        db = getWritableDatabase();
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

    public class StoreDBEntry{
        public static final String TABLE_NAME = "Stores";
        //The ID assigned by the SQLite API when a new row is created in this table
        public static final String COLUMN_NAME_ID = "ID";
        public static final String COLUMN_NAME_STORE_NAME = "Store_Name";
        public static final String COLUMN_NAME_STORE_ADDRESS = "Address";
        public static final String COLUMN_NAME_CONTACT_NAME = "Contact_Name";
        public static final String COLUMN_NAME_CONTACT_PHONE = "Contact_Phone";
        public static final String COLUMN_NAME_STORE_NOTES = "Notes";
        public static final String COLUMN_NAME_DATE_CREATED = "Date_Created";

        public String uid;
        public String storeName;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }

        public String getStoreAddress() {
            return storeAddress;
        }

        public void setStoreAddress(String storeAddress) {
            this.storeAddress = storeAddress;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public String getStoreNotes() {
            return storeNotes;
        }

        public void setStoreNotes(String storeNotes) {
            this.storeNotes = storeNotes;
        }

        public String getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(String dateCreated) {
            this.dateCreated = dateCreated;
        }

        public String storeAddress;
        public String contactName;
        public String contactPhone;
        public String storeNotes;
        public String dateCreated;

    }

    private static final String CREATE_TABLE =
            "CREATE TABLE " + StoreDBEntry.TABLE_NAME + " (" +
                    StoreDBEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
                    StoreDBEntry.COLUMN_NAME_STORE_NAME + " TEXT, " +
                    StoreDBEntry.COLUMN_NAME_STORE_ADDRESS + " TEXT, " +
                    StoreDBEntry.COLUMN_NAME_CONTACT_NAME + " TEXT, " +
                    StoreDBEntry.COLUMN_NAME_CONTACT_PHONE + " TEXT, " +
                    StoreDBEntry.COLUMN_NAME_STORE_NOTES + " TEXT, " +
                    StoreDBEntry.COLUMN_NAME_DATE_CREATED + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StoreDBEntry.TABLE_NAME;

    /***
     *
     * @param name Name of the store.
     * @param address Address of the store.
     * @param contactName Store contact name
     * @param contactPhone Store phone number
     * @param notes Misc. Notes
     */
    public void insertData(String name, String address, String contactName, String contactPhone, String notes,
                           Runnable success, Runnable failure){

        ContentValues values = new ContentValues();
        values.put(StoreDBEntry.COLUMN_NAME_STORE_NAME, name);
        values.put(StoreDBEntry.COLUMN_NAME_STORE_ADDRESS, address);
        values.put(StoreDBEntry.COLUMN_NAME_CONTACT_NAME, contactName);
        values.put(StoreDBEntry.COLUMN_NAME_CONTACT_PHONE, contactPhone);
        values.put(StoreDBEntry.COLUMN_NAME_STORE_NOTES, notes);
        values.put(StoreDBEntry.COLUMN_NAME_DATE_CREATED, Utils.getDateTime());
        long res = db.insert(StoreDBEntry.TABLE_NAME, null, values);

        if(res!=-1){
            success.run();
        }
        else{
            failure.run();
        }
    }

    public boolean hasStores(){

        String[] projection = {StoreDBEntry.COLUMN_NAME_ID};
        Cursor cursor = rdb.query(
                StoreDBEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );
        boolean hasVals = cursor.getCount() > 0;
        cursor.close();

        return hasVals;
    }


    /***
     * Call this only after checking there are stores available in the DB using hasStores()!
     * @return Returns the names of the stores in the DB.
     */
    public String[] getStoreNames(){

        ArrayList<String> strs = new ArrayList<>();

        String[] projection = {StoreDBEntry.COLUMN_NAME_STORE_NAME};
        String sortOrder =
                StoreDBEntry.COLUMN_NAME_STORE_NAME + " DESC";
        Cursor cursor = rdb.query(
                StoreDBEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder             // The sort order
        );

        while(cursor.moveToNext()){
            strs.add(cursor.getString(
                    cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_STORE_NAME)
            ));

            Log.d(TAG, "getStoreNames: " + cursor.getString(
                    cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_STORE_NAME)
            ));
        }
        cursor.close();
        return strs.toArray(new String[strs.size()]);
    }

    public StoreDBEntry getEntry(String storeName, Runnable failure){

        String selection = StoreDBEntry.COLUMN_NAME_STORE_NAME + " = ?";
        String[] selectionArgs = { storeName };

        Cursor cursor = rdb.query(
                StoreDBEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null            // The sort order
        );

        StoreDBEntry entry = new StoreDBEntry();

        try {
            while(cursor.moveToNext()) {
                entry.setContactName(cursor.getString(cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_CONTACT_NAME)));
                entry.setContactPhone(cursor.getString(cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_CONTACT_PHONE)));
                entry.setDateCreated(cursor.getString(cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_DATE_CREATED)));
                entry.setStoreAddress(cursor.getString(cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_STORE_ADDRESS)));
                entry.setStoreName(cursor.getString(cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_STORE_NAME)));
                entry.setStoreNotes(cursor.getString(cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_STORE_NOTES)));
                entry.setUid(cursor.getString(cursor.getColumnIndexOrThrow(StoreDBEntry.COLUMN_NAME_ID)));
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            failure.run();
        }
        cursor.close();

        return entry;
    }

    public void deleteEntry(String storeName){
        // Define 'where' part of query.
        String selection = StoreDBEntry.COLUMN_NAME_STORE_NAME + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { storeName };
        // Issue SQL statement.
        int deletedRows = db.delete(StoreDBEntry.TABLE_NAME, selection, selectionArgs);

        if(deletedRows>0){
            Log.d(TAG, "updateEntry: updated " + deletedRows + " entries.");
        }
        else{
            Log.d(TAG, "updateOrInsertEntry: Added new store to db.");
        }
    }

    public void updateOrInsertEntry(String name, String address, String contactName, String contactPhone, String notes,
                            Runnable success, Runnable failure){

        /* Delete entry, then add entry */
        deleteEntry(name);

        ContentValues values = new ContentValues();
        values.put(StoreDBEntry.COLUMN_NAME_STORE_NAME, name);
        values.put(StoreDBEntry.COLUMN_NAME_STORE_ADDRESS, address);
        values.put(StoreDBEntry.COLUMN_NAME_CONTACT_NAME, contactName);
        values.put(StoreDBEntry.COLUMN_NAME_CONTACT_PHONE, contactPhone);
        values.put(StoreDBEntry.COLUMN_NAME_STORE_NOTES, notes);
        values.put(StoreDBEntry.COLUMN_NAME_DATE_CREATED, Utils.getDateTime());
        long res = db.insert(StoreDBEntry.TABLE_NAME, null, values);

        if(res!=-1){
            success.run();
        }
        else{
            failure.run();
        }
    }

    public void deleteTable(){
        db.execSQL(SQL_DELETE_ENTRIES);
    }
}
