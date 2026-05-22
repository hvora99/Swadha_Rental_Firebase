package swadha.collection.rental;

import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.button.MaterialButton;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReturnDetailActivity extends AppCompatActivity {

    private String itemNo;
    MaterialButton BtnMarkReceived;
    MaterialButton btnCancel;
    MaterialButton btnPickedUp;
    private String bookingTimestamp;
    private LinearLayout layoutItemTimeline;
    private String orderId;
    ArrayList<RentalBooking.ItemStatus> itemsList;
    private double totalRent;
    private double deposit;
    private double rentPaid;
    private AlertDialog loadingDialog;
    private boolean isProcessing = false;

    private static final String ACTION_PICKUP = "pickup";
    private static final String ACTION_RETURN = "return";
    private static final String ACTION_CANCEL = "cancel";

    private FirebaseReturnRepository
            returnRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_detail);

        returnRepository =
                new FirebaseReturnRepository();



        itemNo = getIntent().getStringExtra("itemNo");
        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        long pickupMs = getIntent().getLongExtra("pickupDateTime", 0);
        long returnMs = getIntent().getLongExtra("returnDateTime", 0);



         totalRent = getIntent().getDoubleExtra("totalRent", 0.0);
         deposit = getIntent().getDoubleExtra("deposit", 0.0);
         rentPaid = getIntent().getDoubleExtra("rentPaid", 0.0);
        double balance = getIntent().getDoubleExtra("balance", 0.0);
        orderId = getIntent().getStringExtra("orderId");
        String items = getIntent().getStringExtra("items");

        long washMs = getIntent().getLongExtra("washDateTime",0);
        long actualPickupMs = getIntent().getLongExtra("actualPickupDateTime",0);

       TextView tvCustomerHeader =
                findViewById(R.id.tvCustomerHeader);

        TextView tvPhoneHeader =
                findViewById(R.id.tvPhoneHeader);


        tvCustomerHeader.setText(name);

        tvPhoneHeader.setText(phone);

        bookingTimestamp = getIntent().getStringExtra("timestamp");
        if (bookingTimestamp == null) {
            Toast.makeText(this, "Invalid booking data", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            return;
        }


        String status = getIntent().getStringExtra("status");

        Log.d("DETAIL_DEBUG", "Received timestamp: " + bookingTimestamp);


        TextView tvBalance = findViewById(R.id.detBalance);
        TextView tvTotal = findViewById(R.id.detTotal);
        TextView tvAdvance = findViewById(R.id.detAdvance);
        TextView tvDeposit = findViewById(R.id.detDeposit);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvOrder = findViewById(R.id.tvOrderId);
        layoutItemTimeline = findViewById(R.id.layoutItemTimeline);
        btnPickedUp = findViewById(R.id.btnPickedUp);
        btnCancel = findViewById(R.id.btnCancelBooking);
        BtnMarkReceived = findViewById(R.id.btnMarkReceived);


        itemsList = new ArrayList<>();

        tvOrder.setText(orderId != null ? "Order ID: " + orderId : "Order ID: N/A");

        Date pickupDate = new Date(pickupMs);
        Date returnDate = new Date(returnMs);

        SimpleDateFormat format =
                new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());


        tvTotal.setText("₹ " + String.format("%.2f", totalRent));
        tvAdvance.setText("₹ " + String.format("%.2f", rentPaid));
        tvDeposit.setText("₹ " + String.format("%.2f", deposit));

        tvStatus.setText("Status: " + status);
        tvStatus.setText(status.toUpperCase());
        GradientDrawable bg =
                (GradientDrawable) tvStatus.getBackground().mutate();

        int color;

        if(status.equalsIgnoreCase("Booked")){
            color = Color.parseColor("#FB8C00");
            tvStatus.setText("📝 BOOKED");
        }
        else if(status.equalsIgnoreCase("PickedUp")){
            color = Color.parseColor("#1976D2");
            tvStatus.setText("🚚 PICKED UP");
        }
        else if(status.equalsIgnoreCase("Returned")){
            color = Color.parseColor("#2E7D32");
            tvStatus.setText("✔ RETURNED");
        }
        else{
            color = Color.parseColor("#9E9E9E");
            tvStatus.setText(status.toUpperCase());
        }

        bg.setStroke(2, color);

        double rentDue = totalRent - rentPaid;

        if (rentDue > 0) {

            tvBalance.setTextColor(Color.parseColor("#D32F2F"));
            tvBalance.setText("Collect ₹ " + String.format("%.2f", rentDue));

        } else {

            double refund = deposit + Math.abs(rentDue);

            tvBalance.setTextColor(Color.parseColor("#2E7D32"));
            tvBalance.setText("Refund ₹ " + String.format("%.2f", refund));
        }


        loadOrderItems();


        BtnMarkReceived.setOnClickListener(v -> {

            showItemActionDialog(
                    "Return Items",
                    "return",
                    getReturnItems(),
                    deposit
            );

        });

        btnPickedUp.setOnClickListener(v -> {

            double remainingRent = totalRent - rentPaid;

            showItemActionDialog(
                    "Pick Up Items",
                    "pickup",
                    getPickupItems(),
                    remainingRent
            );

        });

        btnCancel.setOnClickListener(v -> {

            double refund = deposit + rentPaid;

            showItemActionDialog(
                    "Cancel Items",
                    "cancel",
                    getCancelableItems(),
                    refund
            );

        });


// Call Button Logic
        Button btnCall = findViewById(R.id.btnCallCustomer);
        btnCall.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty() && !phone.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:" + phone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton btnWhatsapp = findViewById(R.id.btnSendWhatsapp);

        btnWhatsapp.setOnClickListener(v -> {
            sendToWhatsApp(
                    phone,
                    itemNo,
                    name,
                    pickupDate,
                    returnDate,
                    totalRent,
                    rentPaid,
                    deposit,
                    balance
            );
        });
    }

    private void loadOrderItems(){

        showLoading("Loading items...");

        returnRepository.loadOrderItems(

                orderId,

                new FirebaseReturnRepository
                        .OnItemsLoaded() {

                    @Override
                    public void onLoaded(

                            List<FirebaseOrderItemModel> items
                    ) {

                        hideLoading();

                        itemsList.clear();

                        for(FirebaseOrderItemModel item
                                : items){

                            itemsList.add(

                                    new RentalBooking.ItemStatus(

                                            item.itemNo,

                                            item.itemName,

                                            item.status,

                                            item.customRent,

                                            item.customDeposit,

                                            item.rentPaid,

                                            item.pickupMs,

                                            item.returnMs,

                                            item.washMs
                                    )
                            );
                        }

                        renderItemTimeline(

                                itemsList,

                                0,

                                0
                        );

                        updateButtonStates();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }

    private double calculateAmount(String action, ArrayList<RentalBooking.ItemStatus> items, ListView listView){

        double amount = 0;

        for(int i=0;i<items.size();i++){

            if(listView.isItemChecked(i)){

                RentalBooking.ItemStatus item = items.get(i);

                switch(action){

                    case ACTION_PICKUP:
                        amount += item.getBalance();
                        break;

                    case ACTION_RETURN:
                        amount += item.getDeposit();
                        break;

                    case ACTION_CANCEL:
                        amount += item.getDeposit() + item.getRentPaid();
                        break;
                }
            }
        }

        return amount;
    }

    private void updateButtonStates(){

        boolean hasBooked = false;
        boolean hasPickedUp = false;

        for(RentalBooking.ItemStatus item : itemsList){

            if(item.getStatus().equals("Booked")){
                hasBooked = true;
            }

            if(item.getStatus().equals("PickedUp")){
                hasPickedUp = true;
            }
        }

        setButtonState(btnPickedUp, hasBooked);
        setButtonState(btnCancel, hasBooked);
        setButtonState(BtnMarkReceived, hasPickedUp);
    }

    private ArrayList<RentalBooking.ItemStatus> getPickupItems(){

        ArrayList<RentalBooking.ItemStatus> list = new ArrayList<>();

        for(RentalBooking.ItemStatus item : itemsList){
            Log.d("ITEM_STATUS_CHECK",
                    item.getItemNo() + " -> " + item.getStatus());
            if(item.getStatus().equalsIgnoreCase("Booked")){
                list.add(item);
            }
        }

        return list;
    }

    private ArrayList<RentalBooking.ItemStatus> getReturnItems(){

        ArrayList<RentalBooking.ItemStatus> list = new ArrayList<>();

        for(RentalBooking.ItemStatus item : itemsList){

            if(item.getStatus().equals("PickedUp")){
                list.add(item);
            }
        }

        return list;
    }

    private ArrayList<RentalBooking.ItemStatus> getCancelableItems(){

        ArrayList<RentalBooking.ItemStatus> list = new ArrayList<>();

        for(RentalBooking.ItemStatus item : itemsList){

            if(item.getStatus().equals("Booked")){
                list.add(item);
            }
        }

        return list;
    }




    private void showItemActionDialog(
            String title,
            String action,
            ArrayList<RentalBooking.ItemStatus> items,
            double suggestedAmount
    ){



        if(items.isEmpty()){
            Toast.makeText(this,"No items available for this action",Toast.LENGTH_SHORT).show();
            return;
        }

        String[] itemNames = new String[items.size()];
        boolean[] checked = new boolean[items.size()];

        for(int i=0;i<items.size();i++){

            RentalBooking.ItemStatus item = items.get(i);

            itemNames[i] =
                    item.getItemNo()
                            + " - "
                            + item.getItemName()
                            + "  |  "
                            + "Rent ₹" + item.getRent()
                            + "  |  "
                            + item.getStatus();

            checked[i] = false;
        }

        View view = getLayoutInflater().inflate(R.layout.dialogue_return_item,null);
        TextView tvTitle = view.findViewById(R.id.textviewtitle_dialoue);

        if(action.equals("pickup")){
            tvTitle.setText("Collect Rent");
        }
        else if(action.equals("cancel")){
            tvTitle.setText("Cancel Booking");
        }
        else if(action.equals("return")){
            tvTitle.setText("Return Items");
        }

        ListView listView = view.findViewById(R.id.listItems);
        EditText amountInput = view.findViewById(R.id.etRefund);


        amountInput.setText("0");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_multiple_choice,
                        itemNames);

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.post(() -> {

            for(int i=0;i<items.size();i++){

                RentalBooking.ItemStatus item = items.get(i);

                boolean selectable = isItemSelectable(action, item);

                View itemView = listView.getChildAt(i);

                if(itemView != null){

                    itemView.setEnabled(selectable);

                    if(!selectable){
                        itemView.setAlpha(0.4f);
                    }
                }

                if(!selectable){
                    listView.setItemChecked(i,false);
                }
            }
        });

        listView.setOnItemClickListener((parent, view1, position, id) -> {

            RentalBooking.ItemStatus item = items.get(position);

            if(!isItemSelectable(action, item)){

                listView.setItemChecked(position,false);

                Toast.makeText(this,
                        "Action not allowed for this item",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            double amount = calculateAmount(action, items, listView);

            amountInput.setText(String.valueOf((int)amount));
            amountInput.setSelection(amountInput.getText().length());
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("Confirm",null)
                .setNegativeButton("Cancel",null)
                .create();

        dialog.setOnShowListener(d -> {

            Button confirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            confirm.setOnClickListener(v -> {
                confirm.setEnabled(false);
                ArrayList<String> selectedItems = getSelectedItems(listView, items);

                if(selectedItems.isEmpty()){
                    Toast.makeText(this,"Select at least one item",Toast.LENGTH_SHORT).show();
                    return;
                }

                double amount =
                        amountInput.getText().toString().isEmpty()
                                ? 0
                                : Double.parseDouble(amountInput.getText().toString());

                if(action.equals("pickup")){

                    double remaining = 0;

                    for(RentalBooking.ItemStatus item : itemsList){
                        remaining += item.getBalance();
                    }
                    if(amount > remaining){

                        Toast.makeText(this,
                                "Cannot collect more than remaining rent",
                                Toast.LENGTH_SHORT).show();

                        return;
                    }
                }

                executeAction(action, orderId, selectedItems, amount);

                dialog.dismiss();
            });

        });

        dialog.show();
    }

    private boolean isItemSelectable(String action, RentalBooking.ItemStatus item){

        switch(action){

            case ACTION_PICKUP:
                return item.getStatus().equalsIgnoreCase("Booked");

            case ACTION_RETURN:
                return item.getStatus().equalsIgnoreCase("PickedUp");

            case ACTION_CANCEL:
                return item.getStatus().equalsIgnoreCase("Booked");

            default:
                return false;
        }
    }
    private void executeAction(String action, String orderId, ArrayList<String> items, double amount){

        switch(action){

            case ACTION_PICKUP:
                markAsPickedUp(orderId, items, amount);
                break;

            case ACTION_RETURN:
                markItemAsReturned(orderId, items, amount);
                break;

            case ACTION_CANCEL:
                cancelBookingWithRefund(orderId, items, amount);
                break;
        }
    }

    private void markAsPickedUp(

            String orderId,

            ArrayList<String> items,

            double paidNow
    ){

        if(isProcessing) return;

        isProcessing = true;

        disableActionButtons();

        showLoading("Processing...");

        returnRepository.markItemsPickedUp(

                orderId,

                items,

                paidNow,

                new FirebaseReturnRepository
                        .ActionCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                "Items picked up",

                                Toast.LENGTH_SHORT

                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        isProcessing = false;

                        enableActionButtons();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }


    private void enableActionButtons(){

        btnPickedUp.setEnabled(true);
        btnCancel.setEnabled(true);
        BtnMarkReceived.setEnabled(true);

        btnPickedUp.setAlpha(1f);
        btnCancel.setAlpha(1f);
        BtnMarkReceived.setAlpha(1f);
    }
    private ArrayList<String> getSelectedItems(ListView listView, ArrayList<RentalBooking.ItemStatus> items){

        ArrayList<String> selected = new ArrayList<>();

        for(int i=0;i<items.size();i++){

            if(listView.isItemChecked(i)){
                selected.add(items.get(i).getItemNo());
            }
        }

        return selected;
    }
    private void cancelBookingWithRefund(

            String orderId,

            List<String> items,

            double refundAmount
    ){

        if(isProcessing) return;

        isProcessing = true;

        disableActionButtons();

        showLoading("Cancelling booking...");

        returnRepository.cancelBooking(

                orderId,

                items,

                refundAmount,

                new FirebaseReturnRepository
                        .ActionCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                "Booking cancelled",

                                Toast.LENGTH_SHORT

                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        isProcessing = false;

                        enableActionButtons();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }
    private void markItemAsReturned(

            String orderId,

            List<String> items,

            double refundAmount
    ){

        if(isProcessing) return;

        isProcessing = true;

        disableActionButtons();

        showLoading("Processing...");

        returnRepository.markItemsReturned(

                orderId,

                items,

                refundAmount,

                new FirebaseReturnRepository
                        .ActionCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                "Items returned",

                                Toast.LENGTH_SHORT

                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        isProcessing = false;

                        enableActionButtons();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }
    private void disableActionButtons(){

        btnPickedUp.setEnabled(false);
        btnCancel.setEnabled(false);
        BtnMarkReceived.setEnabled(false);

        btnPickedUp.setAlpha(0.4f);
        btnCancel.setAlpha(0.4f);
        BtnMarkReceived.setAlpha(0.4f);
    }


    private void setButtonState(MaterialButton button, boolean enabled) {
        button.setEnabled(enabled);

        if (enabled) {
            button.setAlpha(1f);
        } else {
            button.setAlpha(0.4f);   // dark faded look
        }
    }


    private void showLoading(String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(false);

        builder.setMessage(message);

        loadingDialog = builder.create();

        loadingDialog.show();
    }

    private void hideLoading(){

        if(loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }



    private void sendToWhatsApp(String phone,
                                String item,
                                String name,
                                Date pickup,
                                Date returnDate,
                                double totalRent,
                                double rentPaid,
                                double deposit,
                                double balance) {

        if (phone == null || phone.trim().isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove spaces if any
        phone = phone.replace(" ", "");

        String message =
                "✨ *SVADHA COLLECTION* ✨\n" +
                        "------------------------------\n\n" +

                        "👗 *Item:* " + item + "\n" +
                        "👤 *Customer:* " + name + "\n\n" +

                        "📅 *Pickup:* " + pickup + "\n" +
                        "📅 *Return:* " + returnDate + "\n\n" +

                        "💰 *Total Rent:* ₹" + totalRent + "\n" +
                        "💳 *Rent Paid:* ₹" + rentPaid + "\n" +
                        "🔐 *Deposit:* ₹" + deposit + "\n" +
                        "📊 *Settlement:* ₹" + balance + "\n\n" +

                        "Thank you for choosing Swadha Collection ❤️";

        try {
            String url = "https://wa.me/91" + phone + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }


    private void renderItemTimeline(ArrayList<RentalBooking.ItemStatus> items,
                                    long pickupMs,
                                    long returnMs){


        SimpleDateFormat format =
                new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());

        if(layoutItemTimeline == null){
            Log.e("UI_ERROR","layoutItemTimeline not found");
            return;
        }

        layoutItemTimeline.removeAllViews();

        for(RentalBooking.ItemStatus item : items){

            View row = getLayoutInflater()
                    .inflate(
                            R.layout.item_return_timeline,
                            layoutItemTimeline,
                            false
                    );



            TextView tvItemNo =
                    row.findViewById(R.id.tvItemNo);

            TextView tvItemName =
                    row.findViewById(R.id.tvItemName);

            TextView tvItemStatus =
                    row.findViewById(R.id.tvItemStatus);

            TextView tvPickup =
                    row.findViewById(R.id.tvPickup);

            TextView tvReturn =
                    row.findViewById(R.id.tvReturn);

            TextView tvWash =
                    row.findViewById(R.id.tvWash);

            TextView tvBalance =
                    row.findViewById(R.id.tvBalance);

            TextView tvDeposit =
                    row.findViewById(R.id.tvDeposit);

            tvItemNo.setText(
                    item.getItemNo()
            );

            tvItemName.setText(
                    item.getItemName()
            );

            tvPickup.setText(
                    format.format(
                            new Date(item.getPickupMs())
                    )
            );

            tvReturn.setText(
                    format.format(
                            new Date(item.getReturnMs())
                    )
            );

            tvWash.setText(
                    format.format(
                            new Date(item.getWashMs())
                    )
            );

            tvBalance.setText(
                    "₹ " + String.format(
                            "%.0f",
                            item.getBalance()
                    )
            );

            tvDeposit.setText(
                    "₹ " + String.format(
                            "%.0f",
                            item.getDeposit()
                    )
            );

            String status = item.getStatus();

            GradientDrawable bg =
                    (GradientDrawable)

                            tvItemStatus
                                    .getBackground()
                                    .mutate();

            int color;

            if(status.equalsIgnoreCase("Booked")){

                color = Color.parseColor("#FB8C00");

                tvItemStatus.setText(
                        "BOOKED"
                );
            }
            else if(status.equalsIgnoreCase("PickedUp")){

                color = Color.parseColor("#1976D2");

                tvItemStatus.setText(
                        "PICKED UP"
                );
            }
            else if(status.equalsIgnoreCase("Returned")){

                color = Color.parseColor("#2E7D32");

                tvItemStatus.setText(
                        "RETURNED"
                );
            }
            else if(status.equalsIgnoreCase("Cancelled")){

                color = Color.parseColor("#D32F2F");

                tvItemStatus.setText(
                        "CANCELLED"
                );
            }
            else{

                color = Color.parseColor("#9E9E9E");

                tvItemStatus.setText(
                        status.toUpperCase()
                );
            }

            bg.setColor(color);

            layoutItemTimeline.addView(row);
        }
    }

}