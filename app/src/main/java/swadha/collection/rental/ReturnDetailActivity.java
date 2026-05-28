package swadha.collection.rental;

import static android.text.format.DateUtils.formatDateTime;

import android.content.Intent;

import android.content.res.ColorStateList;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;


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
    private TextView tvBookingTime,tvBalance,tvAdvance;
    private FirebaseReturnRepository
            returnRepository;
    RentalBooking booking ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_detail);

        returnRepository =
                new FirebaseReturnRepository();


        String json =

                getIntent().getStringExtra(
                        "booking"
                );

        booking =

                new Gson().fromJson(

                        json,

                        RentalBooking.class
                );

        if(booking == null){

            Toast.makeText(

                    this,

                    "Invalid booking",

                    Toast.LENGTH_SHORT

            ).show();

            finish();

            return;
        }


       TextView tvCustomerHeader =
                findViewById(R.id.tvCustomerHeader);

        TextView tvPhoneHeader =
                findViewById(R.id.tvPhoneHeader);

        tvBookingTime =
                findViewById(
                        R.id.tvBookingTime
                );

        TextView tvAlternatePhone =
                findViewById(
                        R.id.tvAlternatePhone
                );




        Log.d("DETAIL_DEBUG", "Received timestamp: " + bookingTimestamp);


        tvBalance = findViewById(R.id.detBalance);
        TextView tvTotal = findViewById(R.id.detTotal);
        tvAdvance = findViewById(R.id.detAdvance);
        TextView tvDeposit = findViewById(R.id.detDeposit);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvOrder = findViewById(R.id.tvOrderId);
        layoutItemTimeline = findViewById(R.id.layoutItemTimeline);
        btnPickedUp = findViewById(R.id.btnPickedUp);
        btnCancel = findViewById(R.id.btnCancelBooking);
        BtnMarkReceived = findViewById(R.id.btnMarkReceived);

        itemNo =
                booking.getFirstItem();

        String name =
                booking.getName();

        String phone =
                booking.getPhone();

        String alternatePhone =
                booking.getAlternatePhone();

        long pickupMs =
                booking.getPickupMs();

        long returnMs =
                booking.getReturnMs();

        totalRent =
                booking.getTotalRent();

        deposit =
                booking.getDeposit();

        rentPaid =
                booking.getRentPaid();

        double balance =
                booking.getBalance();

        orderId =
                booking.getOrderId();

        long washMs =
                booking.getWashMs();

        long actualPickupMs =
                booking.getActualPickupMs();

        String status =
                booking.getStatus();

        bookingTimestamp =
                booking.getTimestamp();


        itemsList = new ArrayList<>(
                booking.getItems()
        );
        tvOrder.setText(orderId != null ? orderId : "Order ID: N/A");

        SimpleDateFormat format =

                new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                );


        tvBookingTime.setText(

                "Booked on: "

                        +

                        format.format(
                                new Date(
                                        booking.getCreatedAt()
                                )
                        )
        );



        tvTotal.setText("₹ " + String.format("%.2f", totalRent));
        tvAdvance.setText("₹ " + String.format("%.2f", rentPaid));
        tvDeposit.setText("₹ " + String.format("%.2f", deposit));
        tvPhoneHeader.setText(phone);
        tvCustomerHeader.setText(name);

        tvStatus.setText("Status: " + status);
        tvStatus.setText(status.toUpperCase());

        View dot =
                findViewById(
                        R.id.viewStatusDot
                );

        MaterialCardView cardStatus =
                findViewById(
                        R.id.cardStatus
                );

        switch(status){

            case "Booked":

                dot.setBackgroundTintList(
                        ColorStateList.valueOf(
                                Color.parseColor("#42A5F5")
                        )
                );

                break;

            case "PickedUp":

                dot.setBackgroundTintList(
                        ColorStateList.valueOf(
                                Color.parseColor("#FFA726")
                        )
                );

                break;

            case "Returned":

                dot.setBackgroundTintList(
                        ColorStateList.valueOf(
                                Color.parseColor("#66BB6A")
                        )
                );

                break;

            case "Cancelled":

                dot.setBackgroundTintList(
                        ColorStateList.valueOf(
                                Color.parseColor("#EF5350")
                        )
                );

                break;
        }

        if(status.equalsIgnoreCase("Booked")){

            tvStatus.setText(
                    "📝 BOOKED"
            );

            dot.setBackgroundTintList(

                    ColorStateList.valueOf(
                            Color.parseColor("#42A5F5")
                    )
            );
        }

        else if(status.equalsIgnoreCase("PickedUp")){

            tvStatus.setText(
                    "🚚 PICKED UP"
            );

            dot.setBackgroundTintList(

                    ColorStateList.valueOf(
                            Color.parseColor("#FFA726")
                    )
            );
        }

        else if(status.equalsIgnoreCase("Returned")){

            tvStatus.setText(
                    "✅ RETURNED"
            );

            dot.setBackgroundTintList(

                    ColorStateList.valueOf(
                            Color.parseColor("#66BB6A")
                    )
            );
        }

        else if(status.equalsIgnoreCase("Cancelled")){

            tvStatus.setText(
                    "❌ CANCELLED"
            );

            dot.setBackgroundTintList(

                    ColorStateList.valueOf(
                            Color.parseColor("#EF5350")
                    )
            );
        }

        else{

            tvStatus.setText(
                    status.toUpperCase()
            );
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
            sendWhatsappMessage();
        });

        if(alternatePhone != null
                &&
                !alternatePhone.isEmpty()){

            tvAlternatePhone.setVisibility(
                    View.VISIBLE
            );

            tvAlternatePhone.setText(
                    alternatePhone
            );

        }else{

            tvAlternatePhone.setVisibility(
                    View.GONE
            );
        }
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

                                            item.washMs,

                                            item.refundedRent,

                                            item.refundedDeposit

                                    )
                            );
                        }

                        renderItemTimeline(

                                itemsList,

                                0,

                                0
                        );

                        double activePending = 0;

                        double totalRefund = 0;

                        for(RentalBooking.ItemStatus item
                                : itemsList){

                            if(item.getStatus().equalsIgnoreCase("Cancelled")){

                                totalRefund +=
                                        item.getRefundedRent()
                                                +
                                                item.getRefundedDeposit();

                                continue;
                            }

                            if(item.getStatus().equalsIgnoreCase("Returned")){

                                totalRefund +=
                                        item.getRefundedDeposit();

                                continue;
                            }

                            activePending +=
                                    item.getBalance();
                        }

                        if(activePending > 0){

                            tvBalance.setTextColor(
                                    Color.parseColor("#D32F2F")
                            );

                            tvBalance.setText(

                                    "Collect ₹ "

                                            +

                                            String.format(
                                                    "%.2f",
                                                    activePending
                                            )
                            );

                        }else{

                            tvBalance.setTextColor(
                                    Color.parseColor("#2E7D32")
                            );

                            tvBalance.setText(

                                    "Refund ₹ "

                                            +

                                            String.format(
                                                    "%.2f",
                                                    totalRefund
                                            )
                            );
                        }

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

            Toast.makeText(

                    this,

                    "No items available",

                    Toast.LENGTH_SHORT

            ).show();

            return;
        }

        String[] itemNames =
                new String[items.size()];

        for(int i=0;i<items.size();i++){

            RentalBooking.ItemStatus item =
                    items.get(i);

            itemNames[i] =

                    item.getItemNo()

                            + " - "

                            + item.getItemName()

                            + "\n"

                            + "Rent ₹"

                            + (int)item.getRent()

                            + "  •  "

                            + item.getStatus();
        }

        View view = getLayoutInflater()

                .inflate(
                        R.layout.dialogue_return_item,
                        null
                );

        TextView tvRefundRent =
                view.findViewById(
                        R.id.layoutRefundRent
                );

        TextView tvRefundDeposit =
                view.findViewById(
                        R.id.layoutRefundDeposit
                );

        EditText etRefundRent =
                view.findViewById(
                        R.id.etRefundRent
                );

        EditText etRefundDeposit =
                view.findViewById(
                        R.id.etRefundDeposit
                );

        ListView listView =
                view.findViewById(
                        R.id.listItems
                );

        etRefundRent.setText("0");

        etRefundDeposit.setText("0");

        // =====================================
        // ACTION CONFIG
        // =====================================

        if(action.equals(ACTION_PICKUP)){

            tvRefundRent.setText(
                    "Collect Rent"
            );

            tvRefundDeposit.setText(
                    "Deposit Refund"
            );

            etRefundRent.setEnabled(false);

            etRefundDeposit.setEnabled(false);

            etRefundRent.setAlpha(0.7f);

            etRefundDeposit.setAlpha(0.5f);

            etRefundRent.setFocusable(false);

            etRefundRent.setClickable(false);

            etRefundRent.setCursorVisible(false);

            etRefundRent.setKeyListener(null);
        }

        else if(action.equals(ACTION_RETURN)){

            tvRefundRent.setText(
                    "Rent Refund"
            );

            tvRefundDeposit.setText(
                    "Deposit Refund"
            );

            etRefundRent.setEnabled(false);

            etRefundDeposit.setEnabled(true);

            etRefundRent.setAlpha(0.5f);
        }

        else if(action.equals(ACTION_CANCEL)){

            tvRefundRent.setText(
                    "Rent Refund"
            );

            tvRefundDeposit.setText(
                    "Deposit Refund"
            );

            etRefundRent.setEnabled(true);

            etRefundDeposit.setEnabled(true);

            etRefundRent.setAlpha(1f);

            etRefundDeposit.setAlpha(1f);
        }
        ReturnSelectAdapter adapter =

                new ReturnSelectAdapter(
                        this,
                        items
                );

        listView.setAdapter(adapter);

        listView.setChoiceMode(
                ListView.CHOICE_MODE_MULTIPLE
        );

        listView.post(() -> {

            for(int i=0;i<items.size();i++){

                RentalBooking.ItemStatus item =
                        items.get(i);

                boolean selectable =
                        isItemSelectable(
                                action,
                                item
                        );

                View itemView =
                        listView.getChildAt(i);

                if(itemView != null){

                    itemView.setEnabled(selectable);

                    itemView.setAlpha(
                            selectable ? 1f : 0.35f
                    );
                }

                if(!selectable){

                    listView.setItemChecked(
                            i,
                            false
                    );
                }
            }
        });

        // =====================================
        // AUTO CALCULATE
        // =====================================

        listView.setOnItemClickListener(

                (parent, v, position, id) -> {
                    adapter.notifyDataSetChanged();

                    RentalBooking.ItemStatus item =
                            items.get(position);

                    if(!isItemSelectable(action,item)){

                        listView.setItemChecked(
                                position,
                                false
                        );

                        Toast.makeText(

                                this,

                                "Action not allowed",

                                Toast.LENGTH_SHORT

                        ).show();

                        return;
                    }

                    double rentAmount = 0;

                    double depositAmount = 0;

                    for(int i=0;i<items.size();i++){

                        if(listView.isItemChecked(i)){

                            RentalBooking.ItemStatus selected =
                                    items.get(i);

                            if(action.equals(ACTION_PICKUP)){

                                rentAmount += Math.max(
                                        0,
                                        selected.getBalance()
                                );
                            }

                            else if(action.equals(ACTION_RETURN)){

                                depositAmount +=
                                        selected.getDeposit();
                            }

                            else if(action.equals(ACTION_CANCEL)){

                                rentAmount +=
                                        selected.getRentPaid();

                                depositAmount +=
                                        selected.getDeposit();
                            }
                        }
                    }

                    etRefundRent.setText(
                            String.valueOf(
                                    (int)rentAmount
                            )
                    );

                    etRefundDeposit.setText(
                            String.valueOf(
                                    (int)depositAmount
                            )
                    );
                });

        AlertDialog dialog =

                new AlertDialog.Builder(this)

                        .setTitle(title)

                        .setView(view)

                        .setPositiveButton(
                                "Confirm",
                                null
                        )

                        .setNegativeButton(
                                "Cancel",
                                null
                        )

                        .create();

        dialog.setOnShowListener(d -> {

            Button confirm =

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            confirm.setOnClickListener(v -> {

                ArrayList<String> selectedItems =

                        getSelectedItems(
                                listView,
                                items
                        );

                if(selectedItems.isEmpty()){

                    Toast.makeText(

                            this,

                            "Select at least one item",

                            Toast.LENGTH_SHORT

                    ).show();

                    return;
                }
                double refundedRent = 0;

                try{

                    String rentText =

                            etRefundRent
                                    .getText()
                                    .toString()
                                    .trim();

                    if(!rentText.isEmpty()){

                        refundedRent =
                                Double.parseDouble(
                                        rentText
                                );
                    }

                }catch (Exception e){

                    etRefundRent.setError(
                            "Invalid amount"
                    );

                    return;
                }

                if(refundedRent < 0){

                    etRefundRent.setError(
                            "Cannot be negative"
                    );

                    return;
                }

                double refundedDeposit = 0;

                try{

                    String depositText =

                            etRefundDeposit
                                    .getText()
                                    .toString()
                                    .trim();

                    if(!depositText.isEmpty()){

                        refundedDeposit =
                                Double.parseDouble(
                                        depositText
                                );
                    }

                }catch (Exception e){

                    etRefundDeposit.setError(
                            "Invalid amount"
                    );

                    return;
                }

                if(refundedDeposit < 0){

                    etRefundDeposit.setError(
                            "Cannot be negative"
                    );

                    return;
                }

                if(action.equals(ACTION_PICKUP)){

                    double remaining = 0;

                    for(RentalBooking.ItemStatus item
                            : itemsList){

                        remaining +=
                                item.getBalance();
                    }

                    if(refundedRent > remaining){

                        Toast.makeText(

                                this,

                                "Cannot collect more than pending balance",

                                Toast.LENGTH_SHORT

                        ).show();

                        return;
                    }
                }

                executeAction(

                        action,

                        orderId,

                        selectedItems,

                        refundedRent,

                        refundedDeposit
                );

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
    private void executeAction(

            String action,

            String orderId,

            ArrayList<String> items,

            double refundedRent,

            double refundedDeposit
    ){

        switch(action){

            case ACTION_PICKUP:

                markAsPickedUp(

                        orderId,

                        items,

                        refundedRent
                );

                break;

            case ACTION_RETURN:

                markItemAsReturned(

                        orderId,

                        items,

                        refundedRent,

                        refundedDeposit
                );

                break;

            case ACTION_CANCEL:

                cancelBookingWithRefund(

                        items,

                        orderId,

                        refundedRent,

                        refundedDeposit
                );

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

            List<String> items,

            String orderId,

            double refundedRent,

            double refundedDeposit
    ){

        if(isProcessing) return;

        isProcessing = true;

        disableActionButtons();

        showLoading("Cancelling booking...");

        returnRepository.cancelBooking(

                orderId,

                items,

                refundedRent,

                refundedDeposit,

                new FirebaseReturnRepository
                        .ActionCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ReturnDetailActivity.this,

                                "Booking updated",

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

            ArrayList<String> items,

            double refundedRent,

            double refundedDeposit
    ){

        if(isProcessing) return;

        isProcessing = true;

        disableActionButtons();

        showLoading("Processing...");

        returnRepository.markItemsReturned(

                orderId,

                items,

                refundedRent,

                refundedDeposit,

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

        if(loadingDialog != null
                &&
                loadingDialog.isShowing()){

            return;
        }

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

    private void sendWhatsappMessage(){

        String phone =
                booking.getPhone();

        if(phone == null
                ||
                phone.trim().isEmpty()){

            Toast.makeText(

                    this,

                    "Phone number unavailable",

                    Toast.LENGTH_SHORT

            ).show();

            return;
        }

        StringBuilder itemsBuilder =
                new StringBuilder();
        for(RentalBooking.ItemStatus item
                : itemsList){

            String status =
                    item.getStatus();

            double pending =

                    item.getCustomRent()

                            - item.getRentPaid();

            switch(status){

                case "Returned":

                    itemsBuilder

                            .append("✅ ")

                            .append(item.getItemName())

                            .append(" (")

                            .append(item.getItemNo())

                            .append(")")

                            .append(" - Returned\n")

                            .append("↳ Deposit Refunded : ₹")

                            .append(String.format(
                                    Locale.getDefault(),
                                    "%.0f",
                                    item.getCustomDeposit()
                            ))

                            .append("\n\n");

                    break;

                case "Cancelled":

                    itemsBuilder

                            .append("❌ ")

                            .append(item.getItemName())

                            .append(" (")

                            .append(item.getItemNo())

                            .append(")")
                            .append(" - Cancelled\n")

                            .append("↳ Refund Amount : ₹")

                            .append(String.format(
                                    Locale.getDefault(),
                                    "%.0f",
                                    item.getRentPaid()
                            ))

                            .append("\n\n");

                    break;

                case "PickedUp":

                    itemsBuilder

                            .append("🚚 ")

                            .append(item.getItemName())

                            .append(" (")

                            .append(item.getItemNo())

                            .append(")")
                            .append(" - Picked Up\n");

                    if(pending > 0){

                        itemsBuilder

                                .append("↳ Pending Balance : ₹")

                                .append(String.format(
                                        Locale.getDefault(),
                                        "%.0f",
                                        pending
                                ))

                                .append("\n");
                    }

                    itemsBuilder.append("\n");

                    break;

                case "Booked":

                    itemsBuilder

                            .append("📝 ")

                            .append(item.getItemName())

                            .append(" (")

                            .append(item.getItemNo())

                            .append(")")
                            .append(" - Booked\n\n");

                    break;
            }
        }

        String orderStatus =
                booking.getStatus();

        String title = "";

        switch(orderStatus){

            case "Booked":

                title =
                        "📝 BOOKING CONFIRMED";

                break;

            case "PickedUp":

                title =
                        "🚚 ITEMS PICKED UP";

                break;

            case "Returned":

                title =
                        "✅ ITEMS RETURNED";

                break;

            case "Cancelled":

                title =
                        "❌ BOOKING CANCELLED";

                break;

            default:

                title =
                        "📦 BOOKING UPDATE";
        }

        double pending =
                booking.getTotalRent()
                        - booking.getRentPaid();



        StringBuilder message =
                new StringBuilder();

        message.append(
                "✨ *SVADHA COLLECTION* ✨\n\n"
        );

        message.append(title)
                .append("\n\n");

        message.append("👤 Customer : ")
                .append(booking.getName())
                .append("\n");

        message.append("🆔 Order ID : ")
                .append(booking.getOrderId())
                .append("\n\n");

        message.append("📦 Items\n")
                .append(itemsBuilder)
                .append("\n");

        message.append(
                "━━━━━━━━━━━━━━\n\n"
        );

        message.append(
                        "💰 Total Rent : ₹ "
                )
                .append(String.format(
                        Locale.getDefault(),
                        "%.0f",
                        booking.getTotalRent()
                ))
                .append("\n");

        message.append(
                        "💳 Rent Paid : ₹ "
                )
                .append(String.format(
                        Locale.getDefault(),
                        "%.0f",
                        booking.getRentPaid()
                ))
                .append("\n");

        message.append(
                        "🔐 Total Deposit : ₹ "
                )
                .append(String.format(
                        Locale.getDefault(),
                        "%.0f",
                        booking.getDeposit()
                ))
                .append("\n\n");

        message.append(
                "\n\n🙏 Thank you for choosing Svadha Collection"
        );

        try{

            String url =

                    "https://wa.me/91"

                            + phone

                            + "?text="

                            + Uri.encode(
                            message.toString()
                    );

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

        }catch(Exception e){

            Toast.makeText(

                    this,

                    "WhatsApp not installed",

                    Toast.LENGTH_SHORT

            ).show();
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


            TextView tvItemNo = row.findViewById(R.id.tvItemNo);

            TextView tvItemName = row.findViewById(R.id.tvItemName);

            TextView tvItemStatus = row.findViewById(R.id.tvItemStatus);

            TextView tvPickup = row.findViewById(R.id.tvPickup);

            TextView tvReturn = row.findViewById(R.id.tvReturn);

            TextView tvWash = row.findViewById(R.id.tvWash);

            TextView tvBalance = row.findViewById(R.id.tvBalance);

            TextView tvBalanceLabel = row.findViewById( R.id.tvBalanceLabel );

            TextView tvDeposit = row.findViewById(R.id.tvDeposit);

            TextView tvRentPaid = row.findViewById(R.id.tvRentPaid);

            tvItemNo.setText(item.getItemNo());

            tvItemName.setText(item.getItemName());

            tvPickup.setText(format.format(new Date(item.getPickupMs())));

            tvReturn.setText(format.format(new Date(item.getReturnMs())));

            if(item.getWashMs() > 0){

                tvWash.setText(
                        format.format(
                                new Date(item.getWashMs())
                        )
                );
            }
            else{

                tvWash.setText("-");
            }
            if(item.getStatus().equalsIgnoreCase("Returned")
                    ||
                    item.getStatus().equalsIgnoreCase("Cancelled")){

                double refund =

                        item.getRefundedDeposit()

                                + item.getRefundedRent();

                tvBalance.setText(

                        "₹ " +

                                String.format(
                                        "%.0f",
                                        refund
                                )
                );

                tvBalance.setTextColor(
                        Color.parseColor("#2E7D32")
                );

                tvBalanceLabel.setText(
                        "REFUND"
                );
            }

            else{

                tvBalance.setText(

                        "₹ " +

                                String.format(
                                        "%.0f",
                                        item.getBalance()
                                )
                );

                tvBalanceLabel.setText(
                        "PENDING"
                );

                tvBalance.setTextColor(
                        Color.parseColor("#D32F2F")
                );
            }

            tvRentPaid.setText("₹ " + String.format("%.0f", item.getRentPaid()));

            tvDeposit.setText("₹ " + String.format("%.0f", item.getDeposit()));

            String status = item.getStatus();

            GradientDrawable bg = (GradientDrawable)

                    tvItemStatus.getBackground().mutate();

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

    @Override
    protected void onDestroy() {

        super.onDestroy();

        hideLoading();
    }

}