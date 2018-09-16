package com.example.android.inventoryappstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * Database helper object
     */
    private InventoryDbHelper mDbHelper;

    /**
     * URI matcher code for the Content URI for the Inventory table
     */
    private static final int ITEMS = 100;

    /**
     * URI matcher cord for the Content URI for a single item in the table
     */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code
     * The input passed into the constructor represents the code to return for the root URI
     * It's common to use NO_MATCH as the input for this case
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer - run the first time anything is called for this class
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // database query on table
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on cursor, so we know what content URI the Cursor was created for
        // If data changes at this URI, we know to update cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues
     * Acts as a switch to either call:
     * - helper insertItem which does the heavy lifting OR,
     * - throws an exception that argument passed is not supported
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Checks that values are not null and/or not negative
     * - use if statements to test values
     * - call a writeable database
     * - call a long id for database.insert
     * - check id with an if statement to verify insertion
     * - notify listeners that data has changed for the item content URI
     * - return ContentUri
     * (?) - why does this method call insert()...which calls this method???
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // TODO NEW CODE
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);

        // TODO OLD CODE
//        // Check that the name is not null
//        String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
//        if (name == null) {
//            return null;
//        }
//
//        Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
//        if (price == null || price < 0) {
//            return null;
//        }
//
//        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
//        if (quantity == null || quantity < 0) {
//            return null;
//        }
//
//        String supplierName = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
//        if (supplierName == null) {
//            return null;
//        }
//
//        Integer supplierPhone = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);
//        if (supplierPhone == null || supplierPhone < 0) {
//            return null;
//        }
//
//        SQLiteDatabase database = mDbHelper.getWritableDatabase();
//
//        // insert item with the given values
//        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
//
//        // if ID is -1, then insertion has failed
//        if (id == -1) {
//            Log.e(LOG_TAG, "Failed to insert for " + uri);
//            return null;
//        }
//
//        // Notify listeners that the data has changed for the content URI
//        getContext().getContentResolver().notifyChange(uri, null);
//
//        // Once we know id, return the new uri
//        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and arguments with passed values
     * Uses a switch for ITEMS, ITEM_ID, and default
     * return: updateItem() method
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                // Extract out ID from URI so we know which row to update
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If item name key is present, check that the value is not null
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If item price key is present, check that the value is not null / >0
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Produce requires a price");
            }
        }

        // If item quantity key is present, check that the value is not null / >0
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires a quantity");
            }
        }

        // If supplier name key is present, check that value is not null
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Supplier requires a name");
            }
        }

        // If supplier phone key is present, check that value is not null
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE)) {
            Integer supplierPhone = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);
            if (supplierPhone != null && supplierPhone < 0) {
                throw new IllegalArgumentException("Supplier requires a phone number");
            }
        }

        // If no values to update, do nothing
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform update on database and get number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were affected, then notify all listeners that data at given URI changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track number of rows that were deleted
        int rowsDeleted;

        // switch to direct action
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete a single row by the given ID
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If rows were affected, notify listeners
        if (rowsDeleted !=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return number of rows deleted
        return rowsDeleted;
    }

    /** Returns the MIME type of data for the content URI */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}








