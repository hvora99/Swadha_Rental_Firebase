package swadha.collection.rental;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.gridlayout.widget.GridLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ItemDetailActivity extends AppCompatActivity {

    private String itemNo;
    private FirebaseItemRepository repository;
    EditText etName, etRent, etDeposit;
    boolean isLocked;
    private AlertDialog progressDialog;
    MaterialCardView btnRemove;

    private boolean requiresWash;
    private List<FirebaseOrderItemModel> calendarBookings = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        repository = new FirebaseItemRepository();

        itemNo = getIntent().getStringExtra("itemNo");
        loadItemDetails();

        loadCalendarBookings();

        TextView tvCode = findViewById(R.id.tvDetailItemCode);
        etName = findViewById(R.id.etDetailItemName);
        etRent = findViewById(R.id.etDetailRent);
        etDeposit = findViewById(R.id.etDetailDeposit);


        MaterialCardView btnToggleLock = findViewById(R.id.btnToggleLock);
       // btnToggleLock.setText(isLocked ? "Unlock Item" : "Lock Item");
        updateLockButtonUI(btnToggleLock);


        btnToggleLock.setOnClickListener(v -> {

            showLoading();

            boolean newLockState = !isLocked;

            repository.updateLockStatus(

                    itemNo,

                    newLockState,

                    new FirebaseItemRepository
                            .SimpleCallback() {

                        @Override
                        public void onSuccess() {

                            hideLoading();

                            isLocked = newLockState;

                            updateLockButtonUI(
                                    btnToggleLock
                            );

                            Toast.makeText(
                                    ItemDetailActivity.this,

                                    isLocked
                                            ? "Item Locked"
                                            : "Item Unlocked",

                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onError(
                                String error
                        ) {

                            hideLoading();

                            Toast.makeText(
                                    ItemDetailActivity.this,
                                    error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });

        tvCode.setText(itemNo);

        Button btnUpdate = findViewById(R.id.btnUpdateItem);
        btnRemove = findViewById(R.id.btnRemoveItem);
        btnRemove.setEnabled(false);
        btnRemove.setAlpha(0.4f);

            btnUpdate.setOnClickListener(v -> {

                String newName = etName.getText().toString().trim();
                String newRent = etRent.getText().toString().trim();
                String newDeposit = etDeposit.getText().toString().trim();

                if(newName.isEmpty()){

                    etName.setError(
                            "Required"
                    );

                    return;
                }

                if(newName.length() < 2){

                    etName.setError(
                            "Too short"
                    );

                    return;
                }

                double rentValue;

                try{

                    rentValue = Double.parseDouble(
                            newRent
                    );

                }catch (Exception e){

                    etRent.setError(
                            "Invalid amount"
                    );

                    return;
                }

                if(rentValue <= 0){

                    etRent.setError(
                            "Must be greater than 0"
                    );

                    return;
                }

                double depositValue;

                try{

                    depositValue = Double.parseDouble(
                            newDeposit
                    );

                }catch (Exception e){

                    etDeposit.setError(
                            "Invalid amount"
                    );

                    return;
                }

                if(depositValue < 0){

                    etDeposit.setError(
                            "Cannot be negative"
                    );

                    return;
                }

                updateItemOnServer(newName, newRent, newDeposit);
            });

        btnRemove.setOnClickListener(v -> {

                new AlertDialog.Builder(this)
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            deleteItemFromServer(itemNo);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
        });

        // Next step: load current booking
    }


    private void loadCalendarBookings(){

        calendarBookings.clear();

        FirebaseFirestore
                .getInstance()

                .collection("orders")

                .get()

                .addOnSuccessListener(orderQuery -> {

                    int totalOrders =
                            orderQuery.size();

                    if(totalOrders == 0){

                        generateCalendarGrid();

                        return;
                    }

                    final int[] processed = {0};

                    for(DocumentSnapshot orderDoc
                            : orderQuery){

                        orderDoc

                                .getReference()

                                .collection("items")

                                .whereEqualTo(
                                        "itemNo",
                                        itemNo
                                )

                                .get()

                                .addOnSuccessListener(itemQuery -> {

                                    for(DocumentSnapshot doc
                                            : itemQuery){

                                        FirebaseOrderItemModel item =

                                                doc.toObject(
                                                        FirebaseOrderItemModel.class
                                                );

                                        if(item == null)
                                            continue;

                                        if("Returned".equalsIgnoreCase(
                                                item.status
                                        )
                                                ||
                                                "Cancelled".equalsIgnoreCase(
                                                        item.status
                                                )){

                                            continue;
                                        }

                                        calendarBookings.add(item);
                                    }

                                    processed[0]++;

                                    if(processed[0]
                                            >= totalOrders){


                                        generateCalendarGrid();
                                    }

                                })

                                .addOnFailureListener(e -> {

                                    processed[0]++;

                                    if(processed[0]
                                            >= totalOrders){

                                        generateCalendarGrid();
                                    }
                                });

                    }

                });


    }

    private void generateCalendarGrid(){
        LinearLayout layout =

                findViewById(
                        R.id.layoutAvailability
                );


        layout.removeAllViews();

        Calendar calendar =
                Calendar.getInstance();

        SimpleDateFormat dayFormat =
                new SimpleDateFormat(
                        "dd",
                        Locale.getDefault()
                );

        SimpleDateFormat weekFormat =
                new SimpleDateFormat(
                        "EEE",
                        Locale.getDefault()
                );

        SimpleDateFormat monthFormat =
                new SimpleDateFormat(
                        "MMM",
                        Locale.getDefault()
                );

        // =========================
        // NEXT 30 DAYS
        // =========================

        for(int i = 0; i < 45; i++){
            View cell = getLayoutInflater()

                    .inflate(
                            R.layout.item_calendar_day,
                            layout,
                            false
                    );

            View dot =
                    cell.findViewById(
                            R.id.viewStatusDot
                    );

            TextView tvDay =
                    cell.findViewById(
                            R.id.tvDay
                    );

            TextView tvMonth =
                    cell.findViewById(
                            R.id.tvMonth
                    );

            TextView tvWeekDay =
                    cell.findViewById(
                            R.id.tvWeekDay
                    );


            tvDay.setText(
                    dayFormat.format(
                            calendar.getTime()
                    )
            );

            tvMonth.setText(
                    monthFormat.format(
                            calendar.getTime()
                    ).toUpperCase()
            );
            tvWeekDay.setText(

                    weekFormat.format(
                            calendar.getTime()
                    ).toUpperCase()
            );

            // =========================
            // TEMP AVAILABLE STATUS
            // =========================
            Calendar dayCalendar =
                    (Calendar) calendar.clone();

            dayCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    0
            );

            dayCalendar.set(
                    Calendar.MINUTE,
                    0
            );

            dayCalendar.set(
                    Calendar.SECOND,
                    0
            );

            dayCalendar.set(
                    Calendar.MILLISECOND,
                    0
            );

            long dayStart =
                    dayCalendar.getTimeInMillis();

            dayCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    23
            );

            dayCalendar.set(
                    Calendar.MINUTE,
                    59
            );

            dayCalendar.set(
                    Calendar.SECOND,
                    59
            );

            long dayEnd =
                    dayCalendar.getTimeInMillis();

            String status = "available";

            for(FirebaseOrderItemModel booking
                    : calendarBookings){

                boolean booked =

                        dayStart
                                < booking.returnMs

                                &&

                                dayEnd
                                        > booking.pickupMs;

                boolean washing =

                        requiresWash

                                &&

                                dayStart
                                        < booking.washMs

                                &&

                                dayEnd
                                        > booking.returnMs;

                if(booked){

                    status = "booked";

                    break;
                }

                if(washing){

                    status = "washing";
                }
            }

            MaterialCardView card =
                    (MaterialCardView) cell;

            GradientDrawable dotBg =

                    (GradientDrawable)
                            dot.getBackground();

            switch(status){

                case "booked":

                    dotBg.setColor(
                            Color.parseColor("#B71C1C")
                    );

                    card.setCardBackgroundColor(
                            Color.parseColor("#FFCDD2")
                    );

                    tvDay.setTextColor(
                            Color.parseColor("#7F1D1D")
                    );

                    tvMonth.setTextColor(
                            Color.parseColor("#AD1457")
                    );

                    tvWeekDay.setTextColor(
                            Color.parseColor("#AD1457")
                    );

                    break;

                case "washing":

                    dotBg.setColor(
                            Color.parseColor("#1565C0")
                    );

                    card.setCardBackgroundColor(
                            Color.parseColor("#BBDEFB")
                    );

                    tvDay.setTextColor(
                            Color.parseColor("#0D47A1")
                    );

                    tvMonth.setTextColor(
                            Color.parseColor("#1565C0")
                    );

                    tvWeekDay.setTextColor(
                            Color.parseColor("#1565C0")
                    );

                    break;

                default:

                    dotBg.setColor(
                            Color.parseColor("#43A047")
                    );
            }

            // =========================
            // GRID WIDTH
            // =========================


            layout.addView(cell);

            // NEXT DAY
            calendar.add(
                    Calendar.DAY_OF_MONTH,
                    1
            );
        }
    }

    private void loadItemDetails(){

        showLoading();



        com.google.firebase.firestore.FirebaseFirestore
                .getInstance()

                .collection("items")

                .document(itemNo)

                .get()

                .addOnSuccessListener(documentSnapshot -> {

                    hideLoading();

                    FirebaseItemModel item =
                            documentSnapshot.toObject(
                                    FirebaseItemModel.class
                            );

                    if(item == null){

                        Toast.makeText(
                                this,
                                "Item not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();

                        return;
                    }

                    isLocked = item.isLocked;

                    TextView tvCode =
                            findViewById(
                                    R.id.tvDetailItemCode
                            );

                    tvCode.setText(item.itemNo);

                    etName.setText(item.itemName);

                    etRent.setText(
                            String.valueOf(item.rent)
                    );

                    etDeposit.setText(
                            String.valueOf(item.deposit)
                    );

                    requiresWash = item.requiresWash;

                    MaterialCardView btnToggleLock =
                            findViewById(
                                    R.id.btnToggleLock
                            );

                    updateLockButtonUI(btnToggleLock);


                    loadActiveBookings(item);

                })

                .addOnFailureListener(e -> {

                    hideLoading();

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
    private void showLoading() {

        if(progressDialog != null
                &&
                progressDialog.isShowing()){

            return;
        }


        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50,50,50,50);
        layout.setGravity(Gravity.CENTER);

        ProgressBar bar = new ProgressBar(this);
        bar.setIndeterminate(true);
        bar.setPadding(0,0,30,0);

        TextView tv = new TextView(this);
        tv.setText("Loading...");
        tv.setTextSize(18);
        tv.setTextColor(Color.BLACK);

        layout.addView(bar);
        layout.addView(tv);

        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(layout)
                .create();

        progressDialog.show();
    }

    private void hideLoading() {

        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
    private void updateLockButtonUI(
            MaterialCardView button
    ){

        TextView tvLockText =
                button.findViewById(
                        R.id.tvLockText
                );

        ImageView ivLockIcon =
                button.findViewById(
                        R.id.ivLockIcon
                );

        if(isLocked){

            tvLockText.setText(
                    "Unlock Item"
            );

            tvLockText.setTextColor(
                    Color.parseColor("#2E7D32")
            );

            ivLockIcon.setColorFilter(
                    Color.parseColor("#2E7D32")
            );

            button.setCardBackgroundColor(
                    Color.parseColor("#EEF8F1")
            );

            button.setStrokeColor(
                    Color.parseColor("#B7DFC3")
            );

        }
        else{

            tvLockText.setText(
                    "Lock Item"
            );

            tvLockText.setTextColor(
                    Color.parseColor("#F57C00")
            );

            ivLockIcon.setColorFilter(
                    Color.parseColor("#F57C00")
            );

            button.setCardBackgroundColor(
                    Color.parseColor("#FFF8E8")
            );

            button.setStrokeColor(
                    Color.parseColor("#FFD89B")
            );
        }
    }

    private void loadActiveBookings(
            FirebaseItemModel item
    ){

        RecyclerView rv =
                findViewById(
                        R.id.rvCurrentBooking
                );
        rv.setLayoutManager(

                new LinearLayoutManager(

                        this,

                        LinearLayoutManager.HORIZONTAL,

                        false
                )
        );
        LinearLayout tvNoBooking =
                findViewById(
                        R.id.tvNoBooking
                );

        FirebaseFirestore.getInstance()

                .collection("orders")

                .get()

                .addOnSuccessListener(query -> {

                    List<CurrentBookingModel> list =
                            new ArrayList<>();

                    int totalOrders =
                            query.size();

                    if(totalOrders == 0){

                        tvNoBooking.setVisibility(
                                View.VISIBLE
                        );

                        rv.setVisibility(
                                View.GONE
                        );

                        return;
                    }

                    final int[] processed =
                            {0};

                    for(DocumentSnapshot orderDoc
                            : query){

                        FirebaseOrderModel order =

                                orderDoc.toObject(
                                        FirebaseOrderModel.class
                                );

                        if(order != null){

                            order.orderId =
                                    orderDoc.getId();

                        }

                        if(order == null){

                            processed[0]++;

                            continue;
                        }


                        FirebaseFirestore.getInstance()

                                .collection("orders")

                                .document(order.orderId)

                                .collection("items")

                                .whereEqualTo(
                                        "itemNo",
                                        item.itemNo
                                )

                                .get()

                                .addOnSuccessListener(itemQuery -> {


                                    boolean hasActiveItem = false;

                                    for(DocumentSnapshot itemDoc
                                            : itemQuery.getDocuments()){



                                        FirebaseOrderItemModel orderItem =

                                                itemDoc.toObject(
                                                        FirebaseOrderItemModel.class
                                                );

                                        if(orderItem == null)
                                            continue;
                                        Log.d(
                                                "ITEM_STATUS",
                                                item.itemNo
                                                        + " -> "
                                                        + orderItem.status
                                        );


                                        if("Returned".equalsIgnoreCase(
                                                orderItem.status
                                        )
                                                ||
                                                "Cancelled".equalsIgnoreCase(
                                                        orderItem.status
                                                )){

                                            continue;
                                        }

                                        hasActiveItem = true;

                                        break;
                                    }


                                    if(hasActiveItem){

                                        boolean alreadyAdded = false;

                                        for(CurrentBookingModel m : list){

                                            if(m.getOrderId().equals(
                                                    order.orderId
                                            )){

                                                alreadyAdded = true;

                                                break;
                                            }
                                        }

                                        if(alreadyAdded){

                                            processed[0]++;

                                            return;
                                        }

                                        list.add(

                                                new CurrentBookingModel(

                                                        order.orderId,

                                                        item.itemNo,

                                                        order.status,

                                                        order.customerName,

                                                        order.phone,

                                                        formatDate(
                                                                order.pickupMs
                                                        ),

                                                        formatDate(
                                                                order.returnMs
                                                        ),

                                                        "₹"
                                                                + order.totalRent,

                                                        "₹"
                                                                + order.totalRentPaid,

                                                        "₹"
                                                                + order.balanceRent
                                                )
                                        );
                                    }

                                    processed[0]++;

                                    if(processed[0] >= totalOrders){

                                        boolean hasBooking =
                                                !list.isEmpty();

                                        btnRemove.setEnabled(
                                                !hasBooking
                                        );

                                        btnRemove.setAlpha(
                                                hasBooking ? 0.45f : 1f
                                        );

                                        if(hasBooking){

                                            tvNoBooking.setVisibility(
                                                    View.GONE
                                            );

                                            rv.setVisibility(
                                                    View.VISIBLE
                                            );

                                            rv.setAdapter(

                                                    new CurrentBookingAdapter(
                                                            list
                                                    )
                                            );

                                        }else{

                                            tvNoBooking.setVisibility(
                                                    View.VISIBLE
                                            );

                                            rv.setVisibility(
                                                    View.GONE
                                            );

                                            TextView tvDesc =
                                                    findViewById(
                                                            R.id.tvNoBookingDesc
                                                    );

                                            tvDesc.setText(
                                                    "This item currently has no active bookings."
                                            );
                                        }
                                    }

                                });
                    }


                });
    }

    private String formatDate(long ms){

        java.text.SimpleDateFormat sdf =

                new java.text.SimpleDateFormat(

                        "dd MMM yyyy hh:mm a",

                        java.util.Locale.getDefault()
                );

        return sdf.format(
                new java.util.Date(ms)
        );
    }
    private void updateItemOnServer(

            String name,

            String rent,

            String deposit
    ){

        showLoading();

        repository.updateBasicInfo(

                itemNo,

                name,

                Double.parseDouble(rent),

                Double.parseDouble(deposit),

                requiresWash,

                new FirebaseItemRepository
                        .SimpleCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                "Item Updated",

                                Toast.LENGTH_SHORT

                        ).show();

                        setResult(RESULT_OK);

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }
    private void deleteItemFromServer(
            String itemNo
    ){

        showLoading();

        repository.deleteItem(

                itemNo,

                new FirebaseItemRepository
                        .SimpleCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                "Item Removed",

                                Toast.LENGTH_SHORT

                        ).show();

                        setResult(RESULT_OK);

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }

}