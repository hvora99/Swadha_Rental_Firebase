package swadha.collection.rental;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    TextView tvOrderId,tvTotalRent, tvRentPaid, tvDeposit,tvCustomerHeader,tvPhoneHeader,tvStatus;

    LinearLayout layoutItemsTimeline;
    MaterialCardView btnSendWhatsapp;
    private MaterialButton btnCallCustomer;
    private TextView tvAlternatePhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);



        String json = getIntent().getStringExtra("data");

        OrderHistoryModel m =
                new Gson().fromJson(json, OrderHistoryModel.class);

        tvAlternatePhone =
                findViewById(
                        R.id.tvAlternatePhone
                );

        layoutItemsTimeline =
                findViewById(R.id.layoutItemsTimeline);

        addTimelineItems(m.items);



        Log.d("TEST123", new Gson().toJson(m));

        // Bind views
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus =
                findViewById(R.id.tvStatus);


        tvCustomerHeader =
                findViewById(
                        R.id.tvCustomerHeader
                );

        tvPhoneHeader =
                findViewById(
                        R.id.tvPhoneHeader
                );

        tvAlternatePhone =
                findViewById(
                        R.id.tvAlternatePhone
                );
        btnSendWhatsapp = findViewById(R.id.btnSendWhatsapp);
        btnCallCustomer =
                findViewById(
                        R.id.btnCallCustomer
                );


        tvTotalRent = findViewById(R.id.tvTotalRent);

        tvRentPaid = findViewById(
                R.id.tvOrderRentPaid
        );

        tvDeposit = findViewById(
                R.id.tvOrderDeposit
        );

        TextView tvRefundAmount =
                findViewById(
                        R.id.tvRefundAmount
                );



        double totalDepositRefund = 0;

        for(OrderHistoryModel.HistoryItem item
                : m.items){

            totalDepositRefund +=
                    item.refundedDeposit;
        }

        tvRefundAmount.setText(

                "₹ " + totalDepositRefund

        );

        // Set values
        tvOrderId.setText(m.orderId);

        tvStatus.setText(
                m.status.toUpperCase()
        );



        // ✅ SHOW ITEMS

        tvCustomerHeader.setText(
                m.customerName
        );

        tvPhoneHeader.setText(
                m.phone
        );
        if(m.alternatePhone != null
                &&
                !m.alternatePhone.isEmpty()){

            tvAlternatePhone.setVisibility(
                    View.VISIBLE
            );

            tvAlternatePhone.setText(

                    "Alternate : "
                            + m.alternatePhone
            );

        }else{

            tvAlternatePhone.setVisibility(
                    View.GONE
            );
        }

        tvTotalRent.setText("₹ " + m.totalRent);
        tvRentPaid.setText(
                "₹ " + m.totalRentPaid
        );

        tvDeposit.setText(
                "₹ " + m.totalDeposit
        );


        btnCallCustomer.setOnClickListener(v -> {

            Intent intent = new Intent(

                    Intent.ACTION_DIAL,

                    Uri.parse(
                            "tel:" + m.phone
                    )
            );

            startActivity(intent);
        });

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


                double pending =

                        item.customRent
                                - item.rentPaid;

                itemsText

                        .append(icon)
                        .append(" *")
                        .append(item.itemNo)
                        .append("*");

                if(item.itemName != null
                        &&
                        !item.itemName.isEmpty()){

                    itemsText

                            .append(" - ")
                            .append(item.itemName);
                }

                itemsText

                        .append("\n")
                        .append("Status : ")
                        .append(item.status)
                        .append("\n");

                // =========================
                // REFUNDED ITEM
                // =========================

                if(item.status.equalsIgnoreCase(
                        "Returned"
                )
                        ||
                        item.status.equalsIgnoreCase(
                                "Cancelled"
                        )){

                    itemsText

                            .append("Refunded : ₹")
                            .append(item.totalRefund)
                            .append("\n");

                    itemsText

                            .append("Rent Paid : ₹")
                            .append(item.rentPaid)
                            .append("\n");

                    itemsText

                            .append("Deposit Refunded : ₹")
                            .append(item.refundedDeposit)
                            .append("\n");
                }

                // =========================
                // ACTIVE ITEM
                // =========================

                else{

                    itemsText

                            .append("Pending : ₹")
                            .append(pending)
                            .append("\n");

                    itemsText

                            .append("Rent Paid : ₹")
                            .append(item.rentPaid)
                            .append("\n");

                    itemsText

                            .append("Deposit : ₹")
                            .append(item.customDeposit)
                            .append("\n");
                }

                itemsText.append("\n");
            }

            String message =

                    "✨ *Svadha Collection*\n\n"

                            + "Hello "
                            + m.customerName
                            + ",\n\n"

                            + "Your booking summary is below.\n\n"

                            + "*Order ID:* "
                            + m.orderId
                            + "\n\n"

                            + "━━━━━━━━━━━━━━\n"

                            + itemsText

                            + "━━━━━━━━━━━━━━\n\n"

                            + "*Order Summary*\n"

                            + "Total Rent : ₹"
                            + m.totalRent
                            + "\n"

                            + "Collected : ₹"
                            + m.totalRentPaid
                            + "\n"

                            + "Deposit Refund : ₹"
                            + m.refundAmount
                            + "\n";

            if(m.balanceRent > 0){

                message +=

                        "Pending Balance : ₹"
                                + m.balanceRent
                                + "\n";
            }

            message +=

                    "\n📞 Contact : "
                            + m.phone;

            if(m.alternatePhone != null
                    &&
                    !m.alternatePhone.isEmpty()){

                message +=

                        "\n📞 Alternate : "
                                + m.alternatePhone;
            }

            message +=

                    "\n\nThank you for choosing *Svadha Collection* 💛";
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
                    R.layout.item_return_timeline,
                    layoutItemsTimeline,
                    false
            );
            TextView tvBalanceLabel =
                    v.findViewById(
                            R.id.tvBalanceLabel
                    );

            TextView tvDepositLabel =
                    v.findViewById(
                            R.id.tvDepositLabel
                    );

            TextView tvRentPaidLabel =
                    v.findViewById(
                            R.id.tvRentPaidLabel
                    );

            TextView tvItemCode =
                    v.findViewById(R.id.tvItemNo);

            TextView tvPickup =
                    v.findViewById(R.id.tvPickup);

            TextView tvReturn =
                    v.findViewById(R.id.tvReturn);

            TextView tvWash =
                    v.findViewById(R.id.tvWash);

            TextView tvBalance =
                    v.findViewById(R.id.tvBalance);

            TextView tvRentPaid =
                    v.findViewById(R.id.tvRentPaid);

            TextView tvDeposit =
                    v.findViewById(R.id.tvDeposit);

            TextView tvItemName =
                    v.findViewById(R.id.tvItemName);

            TextView tvItemStatus =
                    v.findViewById(R.id.tvItemStatus);

            tvItemStatus.setText(item.status);

            tvItemCode.setText(item.itemNo);

            tvItemName.setText(item.itemName);

            tvPickup.setText(
                    formatDateTime(item.pickupMs)
            );

            tvReturn.setText(
                    formatDateTime(item.returnMs)
            );

            if(item.status.equalsIgnoreCase(
                    "Cancelled"
            )){

                tvReturn.setText(
                        "Cancelled"
                );

                tvWash.setText("-");

            }else{

                tvWash.setText(
                        formatDateTime(item.washMs)
                );
            }

            double pendingRent =

                    item.customRent
                            - item.rentPaid;

            boolean refunded =

                    item.status.equalsIgnoreCase(
                            "Returned"
                    )

                            ||

                            item.status.equalsIgnoreCase(
                                    "Cancelled"
                            );


            if(refunded){

                if(item.status.equalsIgnoreCase(
                        "Cancelled"
                )){

                    tvBalanceLabel.setText(
                            "Refunded"
                    );
                }
                else if(item.status.equalsIgnoreCase(
                        "Returned"
                )){

                    tvBalanceLabel.setText(
                            "Pending"
                    );
                }

                tvDepositLabel.setText(
                        "Deposit Refunded"
                );
            }
            else{

                tvBalanceLabel.setText(
                        "Pending"
                );

                tvDepositLabel.setText(
                        "Deposit"
                );
            }

            if(item.status.equalsIgnoreCase(
                    "Cancelled"
            )){

                tvBalance.setText(
                        "₹ " + item.totalRefund
                );
            }
            else if(item.status.equalsIgnoreCase(
                    "Returned"
            )){

                tvBalance.setText(
                        "₹ 0"
                );
            }
            else{

                tvBalance.setText(
                        "₹ " + pendingRent
                );
            }

            tvDeposit.setText(

                    refunded

                            ?

                            "₹ " + item.refundedDeposit

                            :

                            "₹ " + item.customDeposit
            );

            tvRentPaidLabel.setText(
                    "Rent Paid"
            );

            tvRentPaid.setText(
                    "₹ " + item.rentPaid
            );
            GradientDrawable bg =
                    (GradientDrawable)

                            tvItemStatus
                                    .getBackground();


            if(item.status.equalsIgnoreCase("Returned")){

                tvItemStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                );

                bg.setColor(
                        Color.parseColor("#E8F5E9")
                );


            }else if(item.status.equalsIgnoreCase("Cancelled")){

                tvItemStatus.setTextColor(
                        Color.parseColor("#757575")
                );

                bg.setColor(
                        Color.parseColor("#EEEEEE")
                );

                tvReturn.setText(
                        "Cancelled"
                );

                tvWash.setText(
                        "-"
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