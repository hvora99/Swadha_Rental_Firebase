package swadha.collection.rental;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView rvDailyReturns;
    private TextView tvReturnCount, tvTotalBalance;
    private FloatingActionButton fabNewBooking;
    private List<RentalBooking> bookingList;
    private BookingAdapter adapter; // Global variable
    private double balance;
    private SearchView searchView;
    private TextView tvPickupTodayCount;

    private FirebaseDashboardRepository
            dashboardRepository;

    // Put your URL here once so you don't have to keep pasting it
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dashboardRepository =
                new FirebaseDashboardRepository();





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


    @Override
    protected void onResume() {

        super.onResume();

        loadDailyStats();
    }

    @Override
    protected void onPause() {

        super.onPause();

        dashboardRepository.removeListener();
    }



    private void loadDailyStats() {

        dashboardRepository.listenActiveOrders(

                new FirebaseDashboardRepository
                        .DashboardCallback() {

                    @Override
                    public void onDataChanged(

                            List<RentalBooking> bookings
                    ) {

                        bookingList.clear();

                        bookingList.addAll(bookings);

                        Collections.sort(

                                bookingList,

                                (a, b) -> {

                                    int p1 =
                                            getBookingPriority(a);

                                    int p2 =
                                            getBookingPriority(b);

                                    // lower number = higher priority

                                    if(p1 != p2){

                                        return Integer.compare(
                                                p1,
                                                p2
                                        );
                                    }

                                    // secondary sorting

                                    return Long.compare(

                                            a.getReturnMs(),

                                            b.getReturnMs()
                                    );
                                }
                        );

                        adapter.refreshData(
                                bookingList
                        );

                        updateDashboardStats();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        Toast.makeText(

                                DashboardActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }

    private void updateDashboardStats(){

        int todayReturn = 0;

        int todayPickup = 0;

        for(RentalBooking booking
                : bookingList){

            if(isToday(
                    booking.getPickupMs()
            )){

                todayPickup++;
            }

            if(isToday(
                    booking.getReturnMs()
            )){

                todayReturn++;
            }
        }

        tvReturnCount.setText(
                String.valueOf(todayReturn)
        );

        tvPickupTodayCount.setText(
                String.valueOf(todayPickup)
        );

        calculateTotalBalance();
    }






    // Create this helper method to avoid duplicating code
    private boolean isToday(long ms){

        Calendar today = Calendar.getInstance();
        Calendar compare = Calendar.getInstance();
        compare.setTimeInMillis(ms);

        return today.get(Calendar.YEAR) == compare.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR);
    }
    // Helper to update the Total Balance at the top

    private int getBookingPriority(
            RentalBooking booking
    ){

        long now = System.currentTimeMillis();

        String status =
                booking.getStatus();

        // =========================
        // RETURN OVERDUE
        // =========================

        if(status.equalsIgnoreCase(
                "PickedUp"
        )){

            if(booking.getReturnMs() < now){

                return 1;
            }
        }

        // =========================
        // PICKUP OVERDUE
        // =========================

        if(status.equalsIgnoreCase(
                "Booked"
        )){

            if(booking.getPickupMs() < now){

                return 2;
            }
        }

        // =========================
        // PICKUP TODAY
        // =========================

        if(status.equalsIgnoreCase(
                "Booked"
        )){

            if(isToday(
                    booking.getPickupMs()
            )){

                return 3;
            }
        }

        // =========================
        // RETURN TODAY
        // =========================

        if(status.equalsIgnoreCase(
                "PickedUp"
        )){

            if(isToday(
                    booking.getReturnMs()
            )){

                return 4;
            }
        }

        // =========================
        // PICKED UP
        // =========================

        if(status.equalsIgnoreCase(
                "PickedUp"
        )){

            return 5;
        }

        // =========================
        // FUTURE BOOKINGS
        // =========================

        return 6;
    }

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