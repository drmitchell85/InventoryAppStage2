package com.example.android.inventoryappstage2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_INVENTORY_LOADER = 0;

    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private Uri mPhoneUri;

    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener will let us know if the user touches on a View, telling us that they are
     * modifying the view, and we change the mItemHasChanged boolean to true
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Editor intent that was used to launch the activity
        // in order to figure out if we are creating a new item, or editing an existing one
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain an item content URI, then new item interface
        if (mCurrentItemUri == null) {
            // This is a new item
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Make phone button invisible
            Button button = (Button) findViewById(R.id.call_supplier);
            button.setVisibility(View.INVISIBLE);


            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Edit existing item
            setTitle(getString(R.string.editor_activity_title_edit_item));

            getSupportLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

        // Button to increase quantity
        Button increaseButton = (Button) findViewById(R.id.button_quantity_plus_one);
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemIncrease();
                mItemHasChanged = true;
            }
        });

        // Button to decrease quantity
        Button decreaseButton = (Button) findViewById(R.id.button_quantity_minus_one);
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemDecrease();
                mItemHasChanged = true;
            }
        });

        // Initialize loader
    }

    /**
     * Increase quantity by 1
     */
    private void itemIncrease() {
        String oldQuantityString = mQuantityEditText.getText().toString();
        int oldQuantity;
        if (oldQuantityString.isEmpty()) {
            oldQuantity = 0;
        } else {
            oldQuantity = Integer.parseInt(oldQuantityString);
        }
        mQuantityEditText.setText(String.valueOf(oldQuantity + 1));
    }

    /**
     * Decrease quantity by 1
     */
    private void itemDecrease() {
        String oldQuantityString = mQuantityEditText.getText().toString();
        int oldQuantity;
        if (oldQuantityString.isEmpty()) {
            return;
        } else if (oldQuantityString.equals("0")) {
            return;
        } else {
            oldQuantity = Integer.parseInt(oldQuantityString);
            mQuantityEditText.setText(String.valueOf(oldQuantity - 1));
        }
    }


    /**
     * Get user input from editor and save new item into db
     */
    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // ! Code interrupted early and exits method
        // Check if this is supposed to be a new item
        // and check if all fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString)) {
            return;
        }

        if (TextUtils.isEmpty(nameString)) {
            mNameEditText.setError(getString(R.string.error_required));
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            mPriceEditText.setError(getString(R.string.error_required));
            return;
        }

        if (TextUtils.isEmpty(quantityString)) {
            mQuantityEditText.setError(getString(R.string.error_required));
            return;
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            mSupplierNameEditText.setError(getString(R.string.error_required));
            return;
        }

        if (TextUtils.isEmpty(supplierPhoneString)) {
            mSupplierPhoneEditText.setError(getString(R.string.error_required));
            return;
        }

        // Create a ContentValues object where column names are the keys
        // and item attributes from the editor are the values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE, supplierPhoneString);

        // Determine if this is a new or existing item by checking mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a new item
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Add toast message
            if (newUri == null) {
                // null means error
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Successful insertion
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(
                    mCurrentItemUri, values, null, null);

            // Add toast messages
            if (rowsAffected == 0) {
                // Failed to updated
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Success
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * Inflate menu options from res/menu/menu_editor.xml
     * Add menu items to the app bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the menu can be updated
     * (some menu items can be hidden or made invisible)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "delete" menu item
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Contains switch statement that directs actions as needed based on user input
     * with the options available
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu options in the app bar overflow menu
        // Switch gets id of what was selected
        switch (item.getItemId()) {
            // Respond to a click on the "save" menu option
            case R.id.action_save:
                String nameString = mNameEditText.getText().toString().trim();
                String priceString = mPriceEditText.getText().toString().trim();
                String quantityString = mQuantityEditText.getText().toString().trim();
                String supplierNameString = mSupplierNameEditText.getText().toString().trim();
                String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

                if (nameString.isEmpty() ||
                        priceString.isEmpty() ||
                        quantityString.isEmpty() ||
                        supplierNameString.isEmpty() ||
                        supplierPhoneString.isEmpty()) {
                    Toast.makeText(this, "Please fill out remaining fields", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // Save item to database
                saveItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop-up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item has NOT been changed, continue with navigating
                // up to parent activity which is MainActivity
                // This value comes from mItemHasChanged, passed from mTouchListener
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user
                // Create a click listener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User has clicked discard button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create a new CursorLoeader, passing in the uri and the projection
     * We need the Uri from onCreate(), so we need to put in an instance
     * variable called mCurrentItemUri
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all item attributes, define a projection
        // that contains all columns from the item table
        String[] projection = {
                BaseColumns._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    /**
     * Performs actions:
     * - Gets ColumnIndexes for entries
     * - Extracts values of those entries
     * - updates views on the screen with values from the database
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // This should be the only row in the cursor
        if (data.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int idColumnIndex = data.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int supplierNameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);

            // Extract out the value from the cursor for the given column index
            final int id = data.getInt(idColumnIndex);
            String name = data.getString(nameColumnIndex);
            int price = data.getInt(priceColumnIndex);
            final int quantity = data.getInt(quantityColumnIndex);
            String supplier = data.getString(supplierNameColumnIndex);
            int phone = data.getInt(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplier);
            mSupplierPhoneEditText.setText(Integer.toString(phone));
            mPhoneUri = Uri.parse(Integer.toString(phone));

            // Setup button to call supplier
            Button button = (Button) findViewById(R.id.call_supplier);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + mPhoneUri));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    /**
     * Check whether data has been manipulated and pop dialog box if so
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Prompt the user when information has been manipulated, but not saved
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked keep editing, so dismiss and reset dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clocked the delete button, so delete item
                deleteItem();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked cancel button
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast depending on what happened
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}































