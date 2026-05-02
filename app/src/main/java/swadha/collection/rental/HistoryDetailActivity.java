package swadha.collection.rental;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class HistoryDetailActivity extends AppCompatActivity {

    TextView tvItemNo, tvCustomer, tvPhone,
            tvPickupScheduled, tvReturnScheduled,
            tvTotalRent, tvRentPaid, tvDeposit, tvFinalSettlement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        // ✅ FIRST get JSON
        String json = getIntent().getStringExtra("data");

        // ✅ Convert to OrderHistoryModel
        OrderHistoryModel m =
                new Gson().fromJson(json, OrderHistoryModel.class);

        // Bind views
        tvItemNo = findViewById(R.id.tvItemNo);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvPhone = findViewById(R.id.tvPhone);

        tvPickupScheduled = findViewById(R.id.tvPickupScheduled);
        tvReturnScheduled = findViewById(R.id.tvReturnScheduled);

        tvTotalRent = findViewById(R.id.tvTotalRent);
        tvRentPaid = findViewById(R.id.tvRentPaid);
        tvDeposit = findViewById(R.id.tvDeposit);
        tvFinalSettlement = findViewById(R.id.tvFinalSettlement);

        // Set values
        tvItemNo.setText(m.orderId);
        tvCustomer.setText(m.name);
        tvPhone.setText(m.phone);

        tvPickupScheduled.setText(formatDateTime(m.pickupDateTime));
        tvReturnScheduled.setText(formatDateTime(m.returnDateTime));

        tvTotalRent.setText("₹ " + m.totalRent);
        tvRentPaid.setText("₹ " + m.rentPaid);
        tvDeposit.setText("₹ " + m.deposit);

        double finalSettlement = m.rentPaid - m.deposit;
        tvFinalSettlement.setText("₹ " + finalSettlement);
    }

    private String formatDateTime(String input) {

        try {
            // Input example: "4/1/2026 10:33:00 PM"
            java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat("M/d/yyyy hh:mm:ss a");

            java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a");

            return outputFormat.format(inputFormat.parse(input));

        } catch (Exception e) {
            return input; // fallback
        }
    }
}