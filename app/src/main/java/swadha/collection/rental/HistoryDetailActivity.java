package swadha.collection.rental;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    TextView tvOrderId, tvItemNo, tvCustomer, tvPhone,
            tvPickupScheduled, tvReturnScheduled,
            tvTotalRent, tvRentPaid, tvDeposit, tvFinalSettlement,tvActualPickup, tvActualReturn;

    LinearLayout layoutItemsTimeline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        String json = getIntent().getStringExtra("data");

        OrderHistoryModel m =
                new Gson().fromJson(json, OrderHistoryModel.class);

        layoutItemsTimeline =
                findViewById(R.id.layoutItemsTimeline);

        addTimelineItems(m.items);

        Log.d("TEST123", new Gson().toJson(m));

        // Bind views
        tvOrderId = findViewById(R.id.tvOrderId);

        tvItemNo = findViewById(R.id.tvItemNo);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvPhone = findViewById(R.id.tvPhone);

        tvPickupScheduled = findViewById(R.id.tvPickupScheduled);
        tvReturnScheduled = findViewById(R.id.tvReturnScheduled);

        tvTotalRent = findViewById(R.id.tvTotalRent);
        tvRentPaid = findViewById(R.id.tvRentPaid);
        tvDeposit = findViewById(R.id.tvDeposit);
        tvFinalSettlement = findViewById(R.id.tvFinalSettlement);

        tvActualPickup = findViewById(R.id.tvActualPickup);
        tvActualReturn = findViewById(R.id.tvActualReturn);
        // Set values
        tvOrderId.setText(m.orderId);

        // ✅ SHOW ITEMS
        List<String> itemNos = new ArrayList<>();

        for (OrderHistoryModel.HistoryItem item : m.items) {
            itemNos.add(item.itemNo);
        }

        tvItemNo.setText(
                TextUtils.join(", ", itemNos)
        );
        tvCustomer.setText(m.name);
        tvPhone.setText(m.phone);

        tvPickupScheduled.setText(
                formatDateTime(m.pickupDateTime)
        );

        tvReturnScheduled.setText(
                formatDateTime(m.returnDateTime)
        );
        tvActualPickup.setText(
                formatDateTime(m.actualPickup)
        );

        tvActualReturn.setText(
                formatDateTime(m.actualReturn)
        );

        tvTotalRent.setText("₹ " + m.totalRent);
        tvRentPaid.setText("₹ " + m.rentPaid);
        tvDeposit.setText("₹ " + m.deposit);

        double finalSettlement = m.rentPaid - m.deposit;

        tvFinalSettlement.setText("₹ " + finalSettlement);
    }

    private void addTimelineItems(
            List<OrderHistoryModel.HistoryItem> items
    ) {

        layoutItemsTimeline.removeAllViews();

        LayoutInflater inflater =
                LayoutInflater.from(this);

        for (OrderHistoryModel.HistoryItem item : items) {

            View v = inflater.inflate(
                    R.layout.item_timeline_chip,
                    layoutItemsTimeline,
                    false
            );

            TextView tvItemCode =
                    v.findViewById(R.id.tvItemCode);

            TextView tvTimeline =
                    v.findViewById(R.id.tvTimeline);

            tvItemCode.setText(item.itemNo);

            String pickup =
                    formatDateTime(item.actualPickup);

            String returned =
                    formatDateTime(item.actualReturn);

            tvTimeline.setText(
                    "Pickup : " + pickup +
                            "\nReturn : " + returned
            );

            layoutItemsTimeline.addView(v);
        }
    }
    private String formatDateTime(String input) {

        if(input == null || input.trim().isEmpty()){
            return "-";
        }

        try {

            // ISO FORMAT
            java.text.SimpleDateFormat isoFormat =
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            java.util.Locale.ENGLISH
                    );

            isoFormat.setTimeZone(
                    java.util.TimeZone.getTimeZone("UTC")
            );

            java.text.SimpleDateFormat output =
                    new java.text.SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            java.util.Locale.getDefault()
                    );

            return output.format(
                    isoFormat.parse(input)
            );

        } catch (Exception e) {

            try {

                // OLD FORMAT SUPPORT
                java.text.SimpleDateFormat oldFormat =
                        new java.text.SimpleDateFormat(
                                "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
                                java.util.Locale.ENGLISH
                        );

                java.text.SimpleDateFormat output =
                        new java.text.SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                java.util.Locale.getDefault()
                        );

                return output.format(
                        oldFormat.parse(input)
                );

            } catch (Exception ex) {

                return input;
            }
        }
    }
}