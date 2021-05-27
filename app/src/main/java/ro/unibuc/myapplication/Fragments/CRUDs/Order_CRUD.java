package ro.unibuc.myapplication.Fragments.CRUDs;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ro.unibuc.myapplication.AccountActivity;
import ro.unibuc.myapplication.Adapters.ItemSelectionAdapter;
import ro.unibuc.myapplication.Adapters.TableSelectionAdapter;
import ro.unibuc.myapplication.Dao.RestaurantDatabase;
import ro.unibuc.myapplication.Models.Customer;
import ro.unibuc.myapplication.Models.Employee;
import ro.unibuc.myapplication.Models.Item;
import ro.unibuc.myapplication.Models.Order;
import ro.unibuc.myapplication.Models.Table;
import ro.unibuc.myapplication.R;

import static ro.unibuc.myapplication.AccountActivity.getSharedPreferencesInstance;

public class Order_CRUD extends Fragment {
    protected TextView tableNum;
    protected RecyclerView selectTable;
    protected TableSelectionAdapter tableSelectionAdapter;
    protected RecyclerView selectItems;
    protected ItemSelectionAdapter itemSelectionAdapter;
    protected Button saveOrder;
    protected Button deleteOrder;
    protected Button scanQRBtn;
    protected Button changeViewBtn;

    public Order_CRUD() { super(R.layout.fragment_add_order);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tableNum = view.findViewById(R.id.tableNumber);
        selectTable = view.findViewById(R.id.selectTableRecycler);
        selectItems = view.findViewById(R.id.orderItemsRecycler);
        saveOrder = view.findViewById(R.id.save_order_btn);
        deleteOrder = view.findViewById(R.id.delete_order_btn);
        scanQRBtn = view.findViewById(R.id.scanQRButton);
        changeViewBtn = view.findViewById(R.id.changeTABLEsELECTvIEW);

        Bundle bundle = this.getArguments();

        scanQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.QRScanFragment, bundle);
            }
        });

        changeViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeView();
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        selectTable.setHasFixedSize(true);
        selectTable.setLayoutManager(manager);

        manager = new LinearLayoutManager(requireContext());
        selectItems.setHasFixedSize(true);
        selectItems.setLayoutManager(manager);

        RestaurantDatabase db = RestaurantDatabase.getInstance(view.getContext());
        List<Table> tableList = db.tableDAO().getAllTables();
        tableSelectionAdapter = new TableSelectionAdapter(tableList);
        selectTable.setAdapter(tableSelectionAdapter);

        if(bundle != null) {
            Order order = bundle.getParcelable(OrdersViewFragment.getBundleKey());
            assert order != null;
            List<Item> itemList = order.getItems();
            if (itemList == null){
                itemList = db.itemDao().getAllItems();
            }
            itemSelectionAdapter = new ItemSelectionAdapter(itemList);
            selectItems.setAdapter(itemSelectionAdapter);

            buttonUpdateItem(order);
        }
        else{
            List<Item> itemList = db.itemDao().getAllItems();
            itemSelectionAdapter = new ItemSelectionAdapter(itemList);
            selectItems.setAdapter(itemSelectionAdapter);

            buttonInsertNewItem();
        }
    }

    private void changeView() {
        // Function changes visibility of two componenets
        // that have role to input table number
        // selectTable is a recylcer view where user can click on table number
        // tableNum displays id from scanQR

        int visibility = selectTable.getVisibility();

        // Set those two to the other comopnent's visibility
        tableNum.setVisibility(visibility);
        scanQRBtn.setVisibility(visibility);

        // Change other component's visibility to its inverse
        int visible = View.GONE;
        if (visibility != View.VISIBLE)
            visible = View.VISIBLE;

        selectTable.setVisibility(visible);
    }

    // Returns the object if the data is ok
    // else returns null
    protected Order verifyDataInserted(View view){
        List<Table> tables = tableSelectionAdapter.getTableList();

        Table table = null;
        for (Table i : tables){
            if (i.isSelected()){
                table = i;
                break;
            }
        }

        if (tableNum.getText().equals(R.string.no_number)){
            new AlertDialog.Builder(requireContext())
                    .setTitle("No table found")
                    .setMessage("Please scan the QR code.")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return null;
        }

        RestaurantDatabase db = RestaurantDatabase.getInstance(requireContext());

        int qrNumber = 0;
        String text = tableNum.getText().toString();

        for (int i = 0; i < text.length(); i++){
            try{
                qrNumber = (qrNumber * 10) +  Integer.parseInt(String.valueOf(text.charAt(i)));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // If table has not been selected from hidden menu, we try to get from the QR
        if (table == null)
            table = db.tableDAO().getTable(qrNumber);

        if (table == null) {
            Toast.makeText(view.getContext(), "Table has to be selected to complete order.",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        List<Item> itemList = itemSelectionAdapter.getItemList();
        List<Item> boughtItems = new ArrayList<>();

        for (Item itm : itemList){
            if (itm.isSelected()){
                boughtItems.add(itm);
            }
        }

        if (boughtItems.size() == 0){
            Toast.makeText(view.getContext(), "Atelast one item is required per order.",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        SharedPreferences sharedPreferences = getSharedPreferencesInstance(requireContext());
        String currentUserName = sharedPreferences.getString(AccountActivity.SPKEY_NAME, null);

        // Search for employee username
        Employee emp = db.employeeDAO().getEmployeeByName(currentUserName);

        int id = 0;
        if (emp != null){
            id = emp.getUid();

            Customer customer = db.customerDAO().getCustomerByName(currentUserName);
            if (customer != null){
                Toast.makeText(requireContext(), String.valueOf(customer.getUid()), Toast.LENGTH_SHORT).show();
                id = customer.getUid();
            }
        }

        if (id == 0){
            Toast.makeText(requireContext(), "sorry not found", Toast.LENGTH_SHORT).show();
            id = 1;
        }

        Calendar calendar = Calendar.getInstance();
        String orderDate = calendar.getTime().toString();

        Order order = new Order(boughtItems, table.getQRCodeValue(), id, orderDate);
        order.setTotalPrice(findTotal(boughtItems));

        return order;
    }

    protected void updateTableInDatabase(RestaurantDatabase db, Order order){
        int tableId = order.getTableQRValue();
        Table table = db.tableDAO().getTable(tableId);

        // Update table in database
        table.setOccupied(true);
        table.setOrderId(order.getOid());
        table.setServingEmployeeId(order.getAccountId());
        db.tableDAO().updateTable(table);
    }

    protected void buttonInsertNewItem(){
        saveOrder.setText("Add order");
        saveOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Order order = verifyDataInserted(view);

                if (order == null) {
                    return;
                }

                RestaurantDatabase db = RestaurantDatabase.getInstance(view.getContext());
                // Returns new generated id
                long newGeneratedId = db.orderDAO().insertOrder(order);
                updateTableInDatabase(db, order);
                Toast.makeText(view.getContext(),
                        "Order succesfully inserated!", Toast.LENGTH_SHORT).show();

                // Go back to list view
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    protected void buttonUpdateItem(Order order){
        String tableIdStr = "Table " + String.valueOf(order.getTableQRValue());
        tableNum.setText(tableIdStr);

        saveOrder.setText("Send order");

        saveOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Order newOrder = verifyDataInserted(view);

                if (newOrder == null) {
                    return;
                }
                // Item generated is new so it has no id
                newOrder.setOid(order.getOid());

                RestaurantDatabase db = RestaurantDatabase.getInstance(view.getContext());
                // Confilt strategy is REPLACE so is the same as update
                db.orderDAO().insertOrder(newOrder);
                updateTableInDatabase(db, newOrder);

                Toast.makeText(view.getContext(),
                        "Order succesfully updated!", Toast.LENGTH_SHORT).show();

                try {
                    Navigation.findNavController(view).navigate(R.id.tableFragment);
                }
                catch (java.lang.IllegalArgumentException e){
                    //
                }

                try {
                    Navigation.findNavController(view).navigate(R.id.orderHistoryCustomerFragment);
                }

                catch (java.lang.IllegalArgumentException e){
                    //
                }
            }
        });

        // Delete button is hidden by default in view
        deleteOrder.setVisibility(View.VISIBLE);
        deleteOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RestaurantDatabase db = RestaurantDatabase.getInstance(view.getContext());
                db.orderDAO().deleteOrder(order);
                Toast.makeText(getContext(), "Order deleted", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }


    // Function that calculates the total price
    // of the items from array list items.
    protected float findTotal(List<Item> itemList){
        float total = 0;
        for (Item item : itemList){
            float price = item.getPrice();

            int discount = item.getDiscount();
            // If price has a discount
            if (discount != 0){
                // Then we update the price
                price -= discount * price / 100;
            }
            total += price;
        }

        return total;
    }
}