package swadha.collection.rental;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import androidx.appcompat.widget.SearchView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvDailyReturns;
    private TextView tvReturnCount, tvTotalBalance;
    private FloatingActionButton fabNewBooking;
    private List<RentalBooking> bookingList;
    private BookingAdapter adapter; // Global variable
    private boolean isLoading = false;
    private double balance;
    private SearchView searchView;
    private TextView tvPickupTodayCount;
    private RequestQueue queue;
    private Handler autoRefreshHandler = new Handler();
    private Runnable autoRefreshRunnable;


    // Put your URL here once so you don't have to keep pasting it
    private static final String webAppUrl = "https://script.google.com/macros/s/AKfycby9Bfc8ohJDS6bvWDu1I8E21yxzRg_GQBhpRXkzY9hLfcKrDlqzxYe2LyMl4Vmb6CXj/exec";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        queue = Volley.newRequestQueue(this);


        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {

                fetchData();   // refresh dashboard

                autoRefreshHandler.postDelayed(this, 15000);
            }
        };


        ImageButton btnOpenInventory = findViewById(R.id.btnOpenInventory); // New Button
        rvDailyReturns = findViewById(R.id.rvDailyReturns);
        tvReturnCount = findViewById(R.id.tvReturnCount);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        fabNewBooking = findViewById(R.id.fabNewBooking);
        tvPickupTodayCount = findViewById(R.id.tvPickupTodayCount);

        ImageButton btnOpenHistory = findViewById(R.id.btnOpenHistory);
        btnOpenHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        btnOpenInventory.setOnClickListener(v -> {
            startActivity(new Intent(this, ItemActivity.class));
        });



        rvDailyReturns.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();

        // FIXED: Removed "BookingAdapter" from the front to use the global variable
        adapter = new BookingAdapter(bookingList);
        rvDailyReturns.setAdapter(adapter);

        fabNewBooking.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, NewBookingActivity.class);
            startActivity(intent);
        });

        // Set the Long Click Listener BEFORE loading data
        adapter.setOnItemLongClickListener(booking -> {

            new AlertDialog.Builder(this)
                    .setTitle("Mark Order as Returned?")
                    .setMessage(
                            "Order: " + booking.getOrderId() +
                                    "\nCustomer: " + booking.getName() +
                                    "\nItems:\n" + booking.getItemsString()
                    )
                    .setPositiveButton("Yes, Returned", (dialog, which) -> {

                        updateStatusInSheet(booking);

                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        loadDailyStats();
    }
    private void updateStatusInSheet(RentalBooking booking) {

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("action", "markReceived");
            jsonBody.put("orderId", booking.getOrderId());

            JSONArray arr = new JSONArray();
            for(String code : booking.getItemCodes()){
                arr.put(code);
            }

            jsonBody.put("items", arr);

        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                webAppUrl,
                jsonBody,

                response -> {

                    int removedPosition = -1;

                    for (int i = 0; i < bookingList.size(); i++) {

                        if (bookingList.get(i).getOrderId().equals(booking.getOrderId())) {
                            bookingList.remove(i);
                            removedPosition = i;
                            break;
                        }
                    }

                    if (removedPosition != -1) {
                        adapter.notifyItemRemoved(removedPosition);
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                    tvReturnCount.setText(String.valueOf(bookingList.size()));
                    calculateTotalBalance();
                    updateCacheLocally();

                    Toast.makeText(this, "Booking marked as Returned", Toast.LENGTH_SHORT).show();
                },

                error -> {

                    if (error.networkResponse != null &&
                            (error.networkResponse.statusCode == 302 ||
                                    error.networkResponse.statusCode == 301)) {

                        fetchData(); // force refresh
                    } else {
                        Toast.makeText(this, "Sync Error. Refreshing...", Toast.LENGTH_SHORT).show();
                        fetchData();
                    }

                    Log.e("API_ERROR", error.toString());
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                0,
                1f
        ));


        queue.add(request);    }

    private void updateCacheLocally() {

        try {

            JSONArray updatedArray = new JSONArray();

            for (RentalBooking b : bookingList) {

                JSONObject obj = new JSONObject();

                obj.put("timestamp", b.getTimestamp());
                obj.put("orderId", b.getOrderId());
                obj.put("items", b.getItemsString());

                obj.put("name", b.getName());
                obj.put("phone", b.getPhone());

                obj.put("pickupMs", b.getPickupMs());
                obj.put("returnMs", b.getReturnMs());
                obj.put("washMs", b.getWashMs());
                obj.put("actualPickupMs", b.getActualPickupMs());

                obj.put("totalRent", b.getTotalRent());
                obj.put("deposit", b.getDeposit());
                obj.put("rentPaid", b.getRentPaid());
                obj.put("balance", b.getBalance());

                obj.put("status", b.getStatus());

                updatedArray.put(obj);
            }

            saveToCache(updatedArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFromCache();
        fetchData();   // immediate refresh
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){

            if(data != null && data.getBooleanExtra("refresh", false)){

                forceRefresh();   // always reload from server
            }
        }
    }



    private void removeOrderLocally(String orderId){

        for(int i=0;i<bookingList.size();i++){

            if(bookingList.get(i).getOrderId().equals(orderId)){

                bookingList.remove(i);

                adapter.notifyItemRemoved(i);

                break;
            }
        }

        tvReturnCount.setText(String.valueOf(bookingList.size()));

        calculateTotalBalance();
    }

    private void loadDailyStats() {
        // 1. Stop any current scrolling
        rvDailyReturns.stopScroll();
        fetchData();
    }

    private void forceRefresh(){

        // clear cached data
        getSharedPreferences("RentalPrefs", MODE_PRIVATE)
                .edit()
                .remove("cache_data")
                .apply();

        bookingList.clear();
        adapter.notifyDataSetChanged();

        fetchData(); // fetch fresh data
    }

    private void saveToCache(String jsonResponse) {
        getSharedPreferences("RentalPrefs", MODE_PRIVATE)
                .edit()
                .putString("cache_data", jsonResponse)
                .apply();
    }

    private void loadFromCache() {
        String cachedData = getSharedPreferences("RentalPrefs", MODE_PRIVATE).getString("cache_data", null);
        if (cachedData != null) {
            try {
                parseJson(cachedData); // We'll move the parsing logic to a separate method
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void fetchData() {
        if(isLoading) return;

        isLoading = true;
        // Note: Removed start/limit params from URL
        JsonArrayRequest request =  new JsonArrayRequest(Request.Method.GET,
                webAppUrl + "?mode=dashboard",
                null,
                response -> {

                    try {
                        saveToCache(response.toString());
                        parseJson(response.toString());
                    } finally {
                        isLoading = false;
                    }
                },
                error -> {

                    isLoading = false;

                    loadFromCache();

                    Toast.makeText(this,
                            "Offline mode: showing cached data",
                            Toast.LENGTH_SHORT).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                0,
                1f
        ));
        queue.add(request);
    }



    // Create this helper method to avoid duplicating code
    private void parseJson(String jsonString) {

        try {

            JSONArray response = new JSONArray(jsonString);

            bookingList.clear();

            int todayReturn = 0;
            int todayPickup = 0;

            for (int i = 0; i < response.length(); i++) {

                JSONObject obj = response.getJSONObject(i);

                RentalBooking booking = new RentalBooking(
                        obj.optString("timestamp"),
                        obj.optString("orderId"),
                        obj.optString("name"),
                        obj.optString("phone"),
                        obj.optLong("pickupMs"),
                        obj.optLong("returnMs"),
                        obj.optLong("washMs"),
                        obj.optLong("actualPickupMs"),
                        obj.optDouble("totalRent"),
                        obj.optDouble("deposit"),
                        obj.optDouble("rentPaid"),
                        obj.optDouble("balance"),
                        obj.optString("status")
                );

                JSONArray items = obj.optJSONArray("items");

                if(items != null){

                    for(int j=0;j<items.length();j++){

                        JSONObject item = items.getJSONObject(j);

                        booking.addItem(
                                item.getString("itemNo"),
                                item.optString("itemName",""),
                                item.getString("status"),
                                item.optDouble("rent",0),
                                item.optDouble("deposit",0),
                                item.optDouble("rentPaid",0)
                        );
                    }
                }

                bookingList.add(booking);

                if(isToday(booking.getPickupMs())) todayPickup++;
                if(isToday(booking.getReturnMs())) todayReturn++;
            }

            Collections.reverse(bookingList);

            adapter.refreshData(bookingList);

            tvReturnCount.setText(String.valueOf(todayReturn));
            tvPickupTodayCount.setText(String.valueOf(todayPickup));

            calculateTotalBalance();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isToday(long ms){

        Calendar today = Calendar.getInstance();
        Calendar compare = Calendar.getInstance();
        compare.setTimeInMillis(ms);

        return today.get(Calendar.YEAR) == compare.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR);
    }
    // Helper to update the Total Balance at the top


    private void calculateTotalBalance() {

        double totalDue = 0;

        for (RentalBooking b : bookingList) {

            double rentDue = b.getTotalRent() - b.getRentPaid();

            if (rentDue > 0) {
                totalDue += rentDue;
            }
        }

        tvTotalBalance.setText("₹ " + String.format("%.2f", totalDue));
    }
}