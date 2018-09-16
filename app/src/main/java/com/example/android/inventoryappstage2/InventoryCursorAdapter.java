package com.example.android.inventoryappstage2;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryappstage2.data.InventoryContract;



public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

//    public int itemId;
//    public String itemName;
//    public int itemPrice;
//    public int itemQuantity;

    /**
     * Create new blank list item view
     *
     * @param context app context
     * @param cursor  cursor from where we get data - cursor is already at correct position
     * @param parent  parent to which the new view is attached to
     * @return newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Layout inflater for list_item
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Method binds inventory data (in the current row pointed to by cursor)
     * to given list item layout. For example, the name for the current item can bet set on the
     * name TextView in the list item layout
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data - cursor already at correct row
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.text_view_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.text_view_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.text_view_quantity);
        Button saleButton = (Button) view.findViewById(R.id.button_sale);

        // Find columns of item attribute we're interested in
        int idColumnIndex =
                cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int nameColumnIndex =
                cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
        int priceColumnIndex =
                cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE);
        int quantityColumnIndex =
                cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY);

        // Read the item attributes from the cursor for the current item
        final int itemId = cursor.getInt(idColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);

        // Update text views with attributes
        nameTextView.setText(itemName);
        priceTextView.setText(Integer.toString(itemPrice));
        quantityTextView.setText(Integer.toString(itemQuantity));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity Activity = (MainActivity) context;
                Activity.sellItem(itemId, itemQuantity);
            }
        });
    }
}
