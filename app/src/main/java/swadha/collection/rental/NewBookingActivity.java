package swadha.collection.rental;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog; // Added for Time
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.button.MaterialButton;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewBookingActivity extends AppCompatActivity {

    private EditText  etCustomerName, etPhone, etTotalRent,etdeposit,SuggestedDeposit,RentPaidNow;
    private Button btnSaveBooking, btnPickDate, btnReturnDate, btnPickTime, btnReturnTime,btnWashDate,btnWashTime;
    private String selectedPickupDate = "";
    private String selectedReturnDate = "";
    private String selectedPickupTime = ""; // New variable
    private String selectedReturnTime = ""; // New variable
    private String lastSearchKey = "";
    private LinearLayout layoutSelectedItems;

    private List<ItemModel> availableItems = new ArrayList<>();
    MaterialButton btnSearchItems;

    private AlertDialog progressDialog;

    private boolean isItemValid = false; // Your Booking Button
    private Calendar pickupCalendar = Calendar.getInstance();
    private Calendar returnCalendar = Calendar.getInstance();

    private static final int TYPE_PICKUP = 1;
    private static final int TYPE_RETURN = 2;
    private static final int TYPE_WASH   = 3;

    private FirebaseOrderRepository
            orderRepository;

    private Calendar washCalendar = Calendar.getInstance();

    private List<SelectedBookingItemModel>
            selectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_booking);

        // Initialize Views
        etCustomerName = findViewById(R.id.etCustomerName);
        etPhone = findViewById(R.id.etPhone);
        etTotalRent = findViewById(R.id.etTotalRent);
        etdeposit = findViewById(R.id.etDepositCollected);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnReturnDate = findViewById(R.id.btnReturnDate);
        btnPickTime = findViewById(R.id.btnPickTime); // New
        btnReturnTime = findViewById(R.id.btnReturnTime); // New
        btnSaveBooking = findViewById(R.id.btnSaveBooking);
        SuggestedDeposit = findViewById(R.id.etSuggestedDeposit);
        RentPaidNow = findViewById(R.id.etRentPaidNow);
        btnWashDate = findViewById(R.id.btnWashDate);
        btnWashTime = findViewById(R.id.btnWashTime);
        btnSearchItems = findViewById(R.id.btnSearchItems);
        layoutSelectedItems = findViewById(R.id.layoutSelectedItems);

        orderRepository =
                new FirebaseOrderRepository();

        setupCurrencyFormatter(etTotalRent);
        setupCurrencyFormatter(etdeposit);
        setupCurrencyFormatter(RentPaidNow);


        btnWashDate.setEnabled(false);
        btnWashTime.setEnabled(false);

        btnWashDate.setAlpha(0.4f);
        btnWashTime.setAlpha(0.4f);

        washCalendar.setTimeInMillis(returnCalendar.getTimeInMillis());
        updateWashButtonText();

        // Click Listeners
        btnPickDate.setOnClickListener(v -> showDatePicker(TYPE_PICKUP));
        btnPickTime.setOnClickListener(v -> showTimePicker(TYPE_PICKUP));

        btnReturnDate.setOnClickListener(v -> showDatePicker(TYPE_RETURN));
        btnReturnTime.setOnClickListener(v -> showTimePicker(TYPE_RETURN));

        btnWashDate.setOnClickListener(v -> showDatePicker(TYPE_WASH));
        btnWashTime.setOnClickListener(v -> showTimePicker(TYPE_WASH));

        btnSaveBooking.setOnClickListener(v -> {

            // 🔒 Disable immediately (prevent double click)
            btnSaveBooking.setEnabled(false);

            // Call booking function
            saveBooking();

            // ⏳ Re-enable after 3 seconds (fallback safety)
            new Handler().postDelayed(() -> {
                btnSaveBooking.setEnabled(true);
            }, 6000);
        });

        btnSaveBooking.setEnabled(false); // Disable by default
        btnSaveBooking.setBackgroundColor(Color.GRAY);

        btnSearchItems.setOnClickListener(v -> {

            if(!validateDateTime()){
                return;
            }

            fetchAvailableItems();

        });


    }

    private void clearItemCache(){

        availableItems.clear();
        selectedItems.clear();

        layoutSelectedItems.removeAllViews();

        btnSaveBooking.setEnabled(false);
        btnSaveBooking.setBackgroundColor(Color.GRAY);

        btnWashDate.setEnabled(false);
        btnWashTime.setEnabled(false);

        btnWashDate.setAlpha(0.4f);
        btnWashTime.setAlpha(0.4f);
    }

    private void fetchAvailableItems() {

        if (selectedPickupDate.isEmpty()
                || selectedReturnDate.isEmpty()
                || selectedPickupTime.isEmpty()
                || selectedReturnTime.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select all Dates & Times first!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String currentSearchKey =

                selectedPickupDate + "_" +
                        selectedPickupTime + "_" +
                        selectedReturnDate + "_" +
                        selectedReturnTime;

        if(currentSearchKey.equals(lastSearchKey)
                && !availableItems.isEmpty()){

            showSearchableDialog();

            return;
        }

        lastSearchKey = currentSearchKey;

        showLoading();

        availableItems.clear();

        long pickupMs =
                pickupCalendar.getTimeInMillis();

        FirebaseFirestore
                .getInstance()

                .collection("items")

                .get()

                .addOnSuccessListener(querySnapshot -> {

                    hideLoading();

                    for(QueryDocumentSnapshot doc
                            : querySnapshot){

                        FirebaseItemModel firebaseItem =

                                doc.toObject(
                                        FirebaseItemModel.class
                                );

                        ItemModel item = new ItemModel(

                                firebaseItem.itemNo,

                                firebaseItem.itemName,

                                firebaseItem.rent,

                                firebaseItem.deposit,

                                firebaseItem.requiresWash,

                                firebaseItem.isLocked,

                                firebaseItem.nextAvailableMs,

                                firebaseItem.currentStatus
                        );

                        // ====================
                        // AVAILABILITY LOGIC
                        // ====================

                        if(firebaseItem.isLocked){

                            item.setStatus("locked");

                        }else if(

                                firebaseItem
                                        .nextAvailableMs

                                        > pickupMs
                        ){

                            item.setStatus("booked");

                        }else{

                            item.setStatus("available");
                        }

                        availableItems.add(item);
                    }

                    if(availableItems.isEmpty()){

                        Toast.makeText(

                                this,

                                "No inventory found",

                                Toast.LENGTH_SHORT

                        ).show();

                        return;
                    }

                    showSearchableDialog();
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
    private void updateWashButtonText() {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd/MM", Locale.getDefault());

        SimpleDateFormat timeFormat =
                new SimpleDateFormat("HH:mm", Locale.getDefault());

        btnWashDate.setText("Wash: " + dateFormat.format(washCalendar.getTime()));
        btnWashTime.setText("Time: " + timeFormat.format(washCalendar.getTime()));
    }

    private void showSearchableDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40,40,40,40);

        EditText searchBar = new EditText(this);
        searchBar.setHint("Type to search (e.g. CH01)...");
        layout.addView(searchBar);

        TextView suggestTitle = new TextView(this);
        suggestTitle.setText("Suggested Available Items");
        suggestTitle.setTextSize(16);
        suggestTitle.setTypeface(null, Typeface.BOLD);
        suggestTitle.setPadding(0,20,0,10);
        layout.addView(suggestTitle);

        ListView listView = new ListView(this);
        layout.addView(listView);

        ArrayAdapter<ItemModel> adapter =
                new ArrayAdapter<ItemModel>(this,
                        R.layout.item_select_row,
                        availableItems) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {

                        if (convertView == null) {
                            convertView = LayoutInflater.from(getContext())
                                    .inflate(R.layout.item_select_row, parent, false);
                        }

                        CheckBox check = convertView.findViewById(R.id.checkItem);
                        TextView title = convertView.findViewById(R.id.tvItemTitle);
                        TextView status = convertView.findViewById(R.id.tvItemStatus);

                        ItemModel item = getItem(position);

                        title.setText(item.getItemNo() + " - " + item.getItemName());

                        if(item.getNextAvailableMs() > System.currentTimeMillis()){
                            status.setText("Booked till " +
                                    new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                                            .format(new Date(item.getNextAvailableMs())));
                            status.setTextColor(Color.RED);
                            check.setChecked(false);
                            check.setEnabled(false);
                        }
                        // 🔒 LOCKED
                        String statusValue = item.getStatus();

                        switch (statusValue){

                            case "locked":

                                status.setText("🔒 Locked");
                                status.setTextColor(Color.parseColor("#616161"));
                                check.setEnabled(false);
                                check.setChecked(false);
                                break;

                            case "washing":

                                status.setText("🧼 Washing");
                                status.setTextColor(Color.parseColor("#2196F3"));
                                check.setEnabled(false);
                                check.setChecked(false);
                                break;

                            case "booked":

                                status.setText("Booked till " +
                                        new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                                                .format(new Date(item.getNextAvailableMs())));
                                status.setTextColor(Color.RED);
                                check.setEnabled(false);
                                check.setChecked(false);
                                break;

                            default:

                                status.setText("Available");
                                status.setTextColor(Color.parseColor("#2E7D32"));
                                check.setEnabled(true);
                        }

                        boolean alreadySelected = false;

                        for(SelectedBookingItemModel s : selectedItems){

                            if(s.item.getItemNo()
                                    .equals(item.getItemNo())){

                                alreadySelected = true;

                                break;
                            }
                        }

                        check.setChecked(alreadySelected);
                        check.setOnClickListener(v -> {

                            if(check.isChecked()){

                                showRentDepositDialog(item, check);

                            }else{

                                selectedItems.removeIf(s ->
                                        s.item.getItemNo()
                                                .equals(item.getItemNo())
                                );                                updateSelectedItemsUI();
                            }
                        });

                        return convertView;
                    }
                };

        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Items")
                .setView(layout)
                .setPositiveButton("Done", (d,w)->{})
                .create();

        searchBar.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                adapter.getFilter().filter(s);

                listView.postDelayed(() -> {

                    String query = s.toString().trim().toUpperCase();

                    if (query.length() < 2) return;


                    List<ItemModel> suggestions = new ArrayList<>();

                    try {

                        String prefix = query.replaceAll("[0-9]", "");
                        int number = Integer.parseInt(query.replaceAll("[^0-9]", ""));

                        for (ItemModel item : availableItems) {

                            String itemCode = item.getItemNo().toUpperCase();

                            if (!itemCode.startsWith(prefix)) continue;

                            int itemNumber = Integer.parseInt(itemCode.replaceAll("[^0-9]", ""));

                            if (itemNumber <= number) continue;

                            if(item.getStatus().equals("available")){
                                suggestions.add(item);
                            }

                            if(suggestions.size() >= 5) break;
                        }

                    } catch (Exception ignored) {}

                    if (!suggestions.isEmpty()) {

                        ArrayAdapter<ItemModel> suggestionAdapter =
                                new ArrayAdapter<>(NewBookingActivity.this,
                                        R.layout.item_available_row,
                                        suggestions);

                        listView.setAdapter(suggestionAdapter);
                    }

                },120);
            }

            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void afterTextChanged(Editable s){}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {

            ItemModel selected = (ItemModel) parent.getItemAtPosition(position);

            if (!selected.getStatus()
                    .equals("available"))  {

                Toast.makeText(this,
                        "Next available: " +
                                new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                                        .format(new Date(selected.getNextAvailableMs())),
                        Toast.LENGTH_LONG).show();
                return;
            }

            boolean exists = false;

            for(SelectedBookingItemModel s
                    : selectedItems){

                if(s.item.getItemNo()
                        .equals(selected.getItemNo())){

                    exists = true;

                    break;
                }
            }

            if(!exists){

                showRentDepositDialog(
                        selected,
                        null
                );

            }else{

                selectedItems.removeIf(s ->

                        s.item.getItemNo()

                                .equals(
                                        selected.getItemNo()
                                )
                );

                updateSelectedItemsUI();
            }

        });

        dialog.show();
    }

    private void showRentDepositDialog(
            ItemModel item,
            CheckBox check
    ){

        View view = getLayoutInflater()
                .inflate(
                        R.layout.dialog_rent_deposit,
                        null
                );

        EditText etRent =
                view.findViewById(R.id.etRent);

        EditText etDeposit =
                view.findViewById(R.id.etDeposit);

        etRent.setText(
                String.valueOf(item.getRent())
        );

        etDeposit.setText(
                String.valueOf(item.getDeposit())
        );

        new AlertDialog.Builder(this)

                .setTitle(
                        "Item : " +
                                item.getItemNo()
                )

                .setView(view)

                .setPositiveButton(
                        "Save",
                        (d,w)->{

                            try{

                                double customRent =
                                        Double.parseDouble(

                                                etRent.getText()
                                                        .toString()
                                        );

                                double customDeposit =
                                        Double.parseDouble(

                                                etDeposit.getText()
                                                        .toString()
                                        );

                                SelectedBookingItemModel
                                        selected =
                                        new SelectedBookingItemModel();

                                selected.item = item;

                                selected.customRent =
                                        customRent;

                                selected.customDeposit =
                                        customDeposit;

                                selected.rentPaid = 0;

                                selected.pickupMs =
                                        pickupCalendar.getTimeInMillis();

                                selected.returnMs =
                                        returnCalendar.getTimeInMillis();

                                selected.washMs =
                                        washCalendar.getTimeInMillis();

                                // remove old if exists

                                selectedItems.removeIf(s ->

                                        s.item.getItemNo()

                                                .equals(
                                                        item.getItemNo()
                                                )
                                );

                                selectedItems.add(selected);

                                updateSelectedItemsUI();

                            }catch(Exception e){

                                Toast.makeText(

                                        this,

                                        "Invalid value",

                                        Toast.LENGTH_SHORT

                                ).show();

                                if(check != null){

                                    check.setChecked(false);
                                }
                            }
                        })

                .setNegativeButton(
                        "Cancel",
                        (d,w)->{

                            if(check != null){

                                check.setChecked(false);
                            }
                        })

                .show();
    }
    private void updateSelectedItemsUI(){

        layoutSelectedItems.removeAllViews();

        double totalRent = 0;
        double totalDeposit = 0;

        boolean washRequired = false;

        for(SelectedBookingItemModel selected
        : selectedItems){
            if(selected.item.isRequiresWash()){
                washRequired = true;
                break;
            }
        }

        if(washRequired){

            btnWashDate.setEnabled(true);
            btnWashTime.setEnabled(true);

            btnWashDate.setAlpha(1f);
            btnWashTime.setAlpha(1f);

        }else{

            washCalendar.setTimeInMillis(returnCalendar.getTimeInMillis());
            updateWashButtonText();

            btnWashDate.setEnabled(false);
            btnWashTime.setEnabled(false);

            btnWashDate.setAlpha(0.4f);
            btnWashTime.setAlpha(0.4f);
        }

        for(SelectedBookingItemModel selected
        : selectedItems){

            totalRent += selected.customRent;

            totalDeposit +=
                    selected.customDeposit;


        }



        etTotalRent.setText(String.valueOf(totalRent));
        SuggestedDeposit.setText(String.format(Locale.getDefault(),"₹ %,.0f", totalDeposit));
        if(RentPaidNow.getText().toString().isEmpty()){
            RentPaidNow.setText("0");
        }

        SimpleDateFormat format =
                new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());

        for(int i=0;i<selectedItems.size();i++){

            SelectedBookingItemModel item =
                    selectedItems.get(i);

            View card = getLayoutInflater()
                    .inflate(R.layout.item_timeline_chip,null);

            TextView tvItem = card.findViewById(R.id.tvItemCode);
            TextView tvTime = card.findViewById(R.id.tvTimeline);

            tvItem.setText(
                    item.item.getItemNo()
            );

            String timeline =
                    format.format(new Date(item.pickupMs))
                            + " → "
                            + format.format(new Date(item.returnMs));
            tvTime.setText(timeline);

            int index = i;

            card.setOnClickListener(v -> {

                showTimelineEditor(
                        selectedItems.get(index)
                );
            });

            layoutSelectedItems.addView(card);

        }

        btnSaveBooking.setEnabled(!selectedItems.isEmpty());
        btnSaveBooking.setBackgroundColor(Color.parseColor("#4CAF50"));
    }



    private void showTimelineEditor(
            SelectedBookingItemModel item
    ){

        View view = getLayoutInflater().inflate(
                R.layout.dialog_edit_timeline,null);

        TextView tvItem = view.findViewById(R.id.tvItemTitle);

        Button btnPickupDate = view.findViewById(R.id.btnPickupDate);
        Button btnPickupTime = view.findViewById(R.id.btnPickupTime);
        Button btnReturnDate = view.findViewById(R.id.btnReturnDate);
        Button btnReturnTime = view.findViewById(R.id.btnReturnTime);

        tvItem.setText(
                "Item : " +
                        item.item.getItemNo()
        );
        Calendar pickupCal = Calendar.getInstance();
        pickupCal.setTimeInMillis(item.pickupMs);

        Calendar returnCal = Calendar.getInstance();
        returnCal.setTimeInMillis(item.returnMs);

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        SimpleDateFormat timeFormat =
                new SimpleDateFormat("HH:mm", Locale.getDefault());

        btnPickupDate.setText("Pickup : " + dateFormat.format(pickupCal.getTime()));
        btnPickupTime.setText("Time : " + timeFormat.format(pickupCal.getTime()));

        btnReturnDate.setText("Return : " + dateFormat.format(returnCal.getTime()));
        btnReturnTime.setText("Time : " + timeFormat.format(returnCal.getTime()));

        btnPickupDate.setOnClickListener(v -> {

            DatePickerDialog dp = new DatePickerDialog(
                    this,
                    (view1,y,m,d)->{

                        pickupCal.set(y,m,d);

                        btnPickupDate.setText(
                                "Pickup : " +
                                        dateFormat.format(pickupCal.getTime())
                        );

                    },
                    pickupCal.get(Calendar.YEAR),
                    pickupCal.get(Calendar.MONTH),
                    pickupCal.get(Calendar.DAY_OF_MONTH)
            );

// ⭐ restrict date range
            dp.getDatePicker().setMinDate(pickupCalendar.getTimeInMillis());
            dp.getDatePicker().setMaxDate(returnCalendar.getTimeInMillis());

            dp.show();
        });

        btnPickupTime.setOnClickListener(v -> {

            TimePickerDialog tp = new TimePickerDialog(
                    this,
                    (view12,h,min)->{

                        pickupCal.set(Calendar.HOUR_OF_DAY,h);
                        pickupCal.set(Calendar.MINUTE,min);

                        btnPickupTime.setText(
                                "Time : " +
                                        timeFormat.format(pickupCal.getTime())
                        );

                    },
                    pickupCal.get(Calendar.HOUR_OF_DAY),
                    pickupCal.get(Calendar.MINUTE),
                    true
            );

            tp.show();
        });

        btnReturnDate.setOnClickListener(v -> {

            DatePickerDialog dp = new DatePickerDialog(
                    this,
                    (view13,y,m,d)->{

                        returnCal.set(y,m,d);

                        btnReturnDate.setText(
                                "Return : " +
                                        dateFormat.format(returnCal.getTime())
                        );

                    },
                    returnCal.get(Calendar.YEAR),
                    returnCal.get(Calendar.MONTH),
                    returnCal.get(Calendar.DAY_OF_MONTH)
            );

// restrict range
            dp.getDatePicker().setMinDate(pickupCalendar.getTimeInMillis());
            dp.getDatePicker().setMaxDate(returnCalendar.getTimeInMillis());

            dp.show();
        });

        btnReturnTime.setOnClickListener(v -> {

            TimePickerDialog tp = new TimePickerDialog(
                    this,
                    (view14,h,min)->{

                        returnCal.set(Calendar.HOUR_OF_DAY,h);
                        returnCal.set(Calendar.MINUTE,min);

                        btnReturnTime.setText(
                                "Time : " +
                                        timeFormat.format(returnCal.getTime())
                        );

                    },
                    returnCal.get(Calendar.HOUR_OF_DAY),
                    returnCal.get(Calendar.MINUTE),
                    true
            );

            tp.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Timeline")
                .setView(view)
                .setPositiveButton("Save",(d,w)->{

                    item.pickupMs = pickupCal.getTimeInMillis();
                    item.returnMs = returnCal.getTimeInMillis();
                    updateSelectedItemsUI();

                })
                .setNegativeButton("Cancel",null)
                .show();
    }

    private void showLoading() {
        // Create a layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(Gravity.CENTER);

        // Create the ProgressBar
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, 30, 0);

        // Create the "Loading..." Text
        TextView tvText = new TextView(this);
        tvText.setText("Fetching Inventory...");
        tvText.setTextColor(Color.BLACK);
        tvText.setTextSize(18);

        layout.addView(progressBar);
        layout.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false); // Prevents user from clicking away
        builder.setView(layout);

        progressDialog = builder.create();
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    // --- NEW METHOD FOR TIME ---

    private void showDatePicker(int type) {

        if (type == TYPE_PICKUP || type == TYPE_RETURN) {
            btnSaveBooking.setEnabled(false);
            btnSaveBooking.setBackgroundColor(Color.GRAY);

        }

        final Calendar c = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    String formattedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    if (type == TYPE_PICKUP) {

                        pickupCalendar.set(year, month, day);
                        selectedPickupDate = formattedDate;
                        btnPickDate.setText("Pickup: " + day + "/" + (month + 1));
                        clearItemCache();

                    } else if (type == TYPE_RETURN) {

                        returnCalendar.set(year, month, day);
                        selectedReturnDate = formattedDate;
                        btnReturnDate.setText("Return: " + day + "/" + (month + 1));
                        clearItemCache();

                        washCalendar.setTimeInMillis(returnCalendar.getTimeInMillis());
                        updateWashButtonText();

                    } else if (type == TYPE_WASH) {

                        washCalendar.set(year, month, day);

                        updateWashButtonText();
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    private void showTimePicker(int type) {

        if (type == TYPE_PICKUP || type == TYPE_RETURN) {
            btnSaveBooking.setEnabled(false);
            btnSaveBooking.setBackgroundColor(Color.GRAY);
        }

        final Calendar c = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hour, minute) -> {

                    String formattedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hour,
                            minute
                    );

                    if (type == TYPE_PICKUP) {

                        pickupCalendar.set(Calendar.HOUR_OF_DAY, hour);
                        pickupCalendar.set(Calendar.MINUTE, minute);
                        selectedPickupTime = formattedTime;
                        btnPickTime.setText("Time: " + formattedTime);
                        clearItemCache();

                    } else if (type == TYPE_RETURN) {

                        returnCalendar.set(Calendar.HOUR_OF_DAY, hour);
                        returnCalendar.set(Calendar.MINUTE, minute);
                        selectedReturnTime = formattedTime;
                        btnReturnTime.setText("Time: " + formattedTime);
                        clearItemCache();


                        washCalendar.setTimeInMillis(returnCalendar.getTimeInMillis());
                        updateWashButtonText();

                    }else if (type == TYPE_WASH) {

                        washCalendar.set(Calendar.HOUR_OF_DAY, hour);
                        washCalendar.set(Calendar.MINUTE, minute);

                        updateWashButtonText();
                    }
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private boolean validateDateTime() {

        long now = System.currentTimeMillis();
        long pickupMs = pickupCalendar.getTimeInMillis();
        long returnMs = returnCalendar.getTimeInMillis();
        long washMs   = washCalendar.getTimeInMillis();

        if (pickupMs < now) {
            Toast.makeText(this, "Pickup cannot be in past", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (returnMs <= pickupMs) {
            Toast.makeText(this, "Return must be after Pickup", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (washMs < returnMs) {
            Toast.makeText(this, "Wash time must be after Return", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveBooking() {

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Get selection from the EditText

        String name = etCustomerName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String totalStr = etTotalRent.getText().toString().trim();

        // Get rent paid now
        double rentPaidValue = getCurrencyValue(RentPaidNow);

        // Get deposit collected

        double depositValue = getCurrencyValue(etdeposit);


        // 2. Final Client-Side Validation
        if (name.isEmpty() || selectedPickupDate.isEmpty() || selectedPickupTime.isEmpty()) {
            Toast.makeText(this, "Please fill all customer details and dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateDateTime()) {
            return;
        }
        showLoading();
        // 3. Prepare JSON


        long globalWashMs = washCalendar.getTimeInMillis();

        for (SelectedBookingItemModel item
                : selectedItems) {

            if (item.item.isRequiresWash()) {

                item.washMs = globalWashMs;

            } else {

                item.washMs = item.returnMs;
            }
        }


        long now = System.currentTimeMillis();

        FirebaseOrderModel order =
                new FirebaseOrderModel();

        order.orderId =
                "SVD-" + now;

        order.customerName = name;

        order.phone = phone;

        order.totalRent =
                getCurrencyValue(etTotalRent);

        order.totalDeposit =
                getCurrencyValue(SuggestedDeposit);

        order.totalRentPaid =
                getCurrencyValue(RentPaidNow);

        order.balanceRent =
                order.totalRent
                        - order.totalRentPaid;

        order.pickupMs =
                pickupCalendar.getTimeInMillis();

        order.returnMs =
                returnCalendar.getTimeInMillis();

        order.washBlockMs =
                washCalendar.getTimeInMillis();

        order.status =
                Constants.STATUS_BOOKED;

        order.createdAt = now;

        order.updatedAt = now;

        double remainingRentPaid =
                order.totalRentPaid;

        for(SelectedBookingItemModel item
                : selectedItems){

            if(remainingRentPaid <= 0){

                item.rentPaid = 0;

                continue;
            }

            double payable =
                    item.customRent;

            if(remainingRentPaid >= payable){

                item.rentPaid = payable;

                remainingRentPaid -= payable;

            }else{

                item.rentPaid =
                        remainingRentPaid;

                remainingRentPaid = 0;
            }
        }

        showLoading();

        orderRepository.createBooking(

                order,

                selectedItems,

                new FirebaseOrderRepository
                        .OrderCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                NewBookingActivity.this,

                                "Booking Confirmed",

                                Toast.LENGTH_LONG

                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        btnSaveBooking.setEnabled(true);

                        Toast.makeText(

                                NewBookingActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }

    private void setupCurrencyFormatter(EditText editText){

        editText.setOnFocusChangeListener((v, hasFocus) -> {

            String text = editText.getText().toString().replaceAll("[₹,\\s]", "");

            if(hasFocus){

                // Remove formatting when user starts editing
                if(!text.isEmpty()){
                    editText.setText(text);
                    editText.setSelection(editText.getText().length());
                }

            }else{

                if(text.isEmpty()) return;

                try{

                    double value = Double.parseDouble(text);

                    String formatted = String.format(Locale.getDefault(),"₹ %,.2f", value);

                    editText.setText(formatted);

                }catch(Exception ignored){}
            }
        });
    }
    private double getCurrencyValue(EditText et){

        String clean = et.getText().toString().replaceAll("[₹,\\s]", "");

        if(clean.isEmpty()) return 0;

        return Double.parseDouble(clean);
    }
}