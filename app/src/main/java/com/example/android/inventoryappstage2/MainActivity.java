package com.example.android.inventoryappstage2;


import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    // Adapter for ListView
    InventoryCursorAdapter mCursorAdapter;

   RelativeLayout emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Find the ListView which will be populated with item data
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Find and set emptyView on the ListView, so that it only shows when the list has 0 items
        emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of item data
        // There is no item data yet, so pass in null for the Cursor
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        // Setup item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Will need id to ID which item was clicked on
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form content URI for specific item
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);

                Log.v("MainActivity", "Intent has been selected");
                startActivity(intent);
            }
        });


        // Initialize loader
        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    /**
     * Sell item count - call this in InventoryCursorAdapter.class - > bindView()
     * Use ContentValues to update quantity count
     * Use ContentResolver() to verify changes
     */
    public void sellItem(int id, int quantity) {
        if (quantity > 0) {
            quantity -= 1;
            ContentValues values = new ContentValues();

            // Update values
            values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);

            // Get URI
            Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

            // Get value of rows affected
            int rowsAffected = getContentResolver().update(uri, values, null, null);
            Toast.makeText(this, "Sale made", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Rows affected :" + rowsAffected + ", with item ID: " + id);
        } else {
            Toast.makeText(this, "Product no longer in stock - please order more",
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Creates dummy map to test content
     */
    private void insertItem() {
        // Create a new map of values, where column names are keys
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME, "PRODUCT NAME");
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, 10);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, 100);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, "SUPPLIER NAME");
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE, 1234);

        // Insert new row, returning the primary key value of the new row
        Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all items in the inventory
     */
    private void deleteAllItems() {
        // get int for rows deleted, and pass to a Log.v
        int rowsDeleted = getContentResolver().delete(
                InventoryEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from inventory");
    }

    /**
     * Inflate menu_catalog
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Switch attached to two cases
     * - insert dummy data - insertItem()
     * - delete all entries - deleteAllItems()
     * return stays as is
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User selected on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Performs two actions
     * - Creates projection with name, price, quantity
     * - returns CursorLoader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BaseColumns._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY};

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    /**
     * Update CursorAdapter with new cursor containing updated data
     * .swapCursor passes cursor data into the CursorAdapter
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    /**
     * Callback called when the data needs to be deleted
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}











