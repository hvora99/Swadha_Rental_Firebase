package swadha.collection.rental;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    TextView tvOrderId, tvItemNo, tvCustomer, tvPhone,tvTotalRent, tvRentPaid, tvDeposit;

    LinearLayout layoutItemsTimeline;
    private RelativeLayout layoutPendingBalance;
    MaterialCardView btnSendWhatsapp;

    private TextView tvPendingBalance;
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

        layoutPendingBalance =
                findViewById(
                        R.id.layoutPendingBalance
                );

        tvPendingBalance =
                findViewById(
                        R.id.tvPendingBalance
                );

        Log.d("TEST123", new Gson().toJson(m));

        // Bind views
        tvOrderId = findViewById(R.id.tvOrderId);

        tvItemNo = findViewById(R.id.tvItemNo);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvPhone = findViewById(R.id.tvPhone);
        btnSendWhatsapp = findViewById(R.id.btnSendWhatsapp);

        tvTotalRent = findViewById(R.id.tvTotalRent);
        tvRentPaid = findViewById(R.id.tvRentPaid);
        tvDeposit = findViewById(R.id.tvDeposit);

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
        tvCustomer.setText(m.customerName);
        tvPhone.setText(m.phone);



        tvTotalRent.setText("₹ " + m.totalRent);
        tvRentPaid.setText(
                "₹ " + m.totalRentPaid
        );

        tvDeposit.setText(
                "₹ " + m.totalDeposit
        );

        if(m.balanceRent > 0){

            layoutPendingBalance.setVisibility(
                    View.VISIBLE
            );

            tvPendingBalance.setText(

                    "₹ " + m.balanceRent
            );

        }else{

            layoutPendingBalance.setVisibility(
                    View.GONE
            );
        }

        btnSendWhatsapp.setOnClickListener(v -> {

            StringBuilder itemsText =
                    new StringBuilder();

            for(OrderHistoryModel.HistoryItem item
                    : m.items){

                String icon = "•";

                if(item.status.equalsIgnoreCase(
                        "Returned"
                )){

                    icon = "✅";
                }

                else if(item.status.equalsIgnoreCase(
                        "Cancelled"
                )){

                    icon = "❌";
                }

                itemsText

                        .append(icon)

                        .append(" ")

                        .append(item.itemNo);

                if(item.itemName != null
                        &&
                        !item.itemName.isEmpty()){

                    itemsText

                            .append(" - ")

                            .append(item.itemName);
                }

                itemsText

                        .append(" (")

                        .append(item.status)

                        .append(")");

                itemsText.append("\n");
            }

            String message =

                    "✨ *Svadha Collection*\n\n"

                            + "Hello "
                            + m.customerName
                            + ",\n\n"

                            + "Your rental booking has been updated successfully.\n\n"

                            + "*Order ID:* "
                            + m.orderId
                            + "\n\n"

                            + "*Items:*\n"
                            + itemsText
                            + "\n"

                            + "*Total Rental:* ₹"
                            + m.totalRent
                            + "\n"

                            + "*Collected:* ₹"
                            + m.totalRentPaid
                            + "\n"

                            + "*Deposit Refunded:* ₹"
                            + m.refundAmount
                            + "\n";

            if(m.balanceRent > 0){

                message +=

                        "\n*Pending Balance:* ₹"
                                + m.balanceRent
                                + "\n";
            }

            message +=

                    "\nThank you for choosing *Svadha Collection* 💛"

                            + "\nWe look forward to serving you again.";

            Intent intent = new Intent(
                    Intent.ACTION_VIEW
            );

            intent.setData(

                    Uri.parse(

                            "https://wa.me/91"
                                    + m.phone
                                    + "?text="
                                    + Uri.encode(message)
                    )
            );

            startActivity(intent);
        });
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
            TextView tvItemStatus =
                    v.findViewById(R.id.tvItemStatus);

            tvItemStatus.setText(item.status);

            tvItemCode.setText(item.itemNo);

            if(item.status.equalsIgnoreCase("Returned")){

                tvItemStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                );

            }else if(item.status.equalsIgnoreCase("Cancelled")){

                tvItemStatus.setTextColor(
                        Color.parseColor("#9E9E9E")
                );

            }else{

                tvItemStatus.setTextColor(
                        Color.parseColor("#EF6C00")
                );
            }

            if(item.status.equalsIgnoreCase(
                    "Cancelled"
            )){

                tvTimeline.setText(
                        "❌ Cancelled Before Pickup"
                );
            }
            else if(item.status.equalsIgnoreCase(
                    "Returned"
            )){

                String pickup =
                        formatDateTime(item.pickupMs);

                String returned =
                        formatDateTime(item.returnMs);

                tvTimeline.setText(

                        "Pickup : " + pickup +

                                "\nReturn : " + returned
                );
            }
            else{

                tvTimeline.setText(
                        item.status
                );
            }
            layoutItemsTimeline.addView(v);
        }
    }

    private String formatDateTime(long ms){

        if(ms <= 0){
            return "-";
        }

        SimpleDateFormat sdf =

                new SimpleDateFormat(

                        "dd MMM yyyy, hh:mm a",

                        Locale.getDefault()
                );

        return sdf.format(ms);
    }
}