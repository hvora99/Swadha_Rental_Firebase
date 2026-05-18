package swadha.collection.rental;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private Spinner spinnerYear;
    private TextView tvTotalEarnings;

    private HistoryAdapter adapter;

    private List<OrderHistoryModel> fullList = new ArrayList<>();
    private List<OrderHistoryModel> filteredList = new ArrayList<>();

    private String BASE_URL = "https://script.google.com/macros/s/AKfycby9Bfc8ohJDS6bvWDu1I8E21yxzRg_GQBhpRXkzY9hLfcKrDlqzxYe2LyMl4Vmb6CXj/exec"; // 🔥 change this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        rvHistory = findViewById(R.id.rvHistory);
        spinnerYear = findViewById(R.id.spinnerYear);
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(filteredList, this);
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    // ================= LOAD API =================

    private void loadHistory() {

        String url = BASE_URL + "?mode=history";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {

                    try {

                        JSONArray arr = new JSONArray(response);

                        fullList.clear();

                        for (int i = 0; i < arr.length(); i++) {

                            JSONObject obj = arr.getJSONObject(i);

                            OrderHistoryModel m = new OrderHistoryModel();

                            m.timestamp = obj.optLong("timestamp");
                            m.orderId = obj.optString("orderId");
                            m.name = obj.optString("name");
                            m.phone = obj.optString("phone");

                            m.pickupDateTime = obj.optString("pickupDateTime");
                            m.returnDateTime = obj.optString("returnDateTime");

                            m.actualPickup = obj.optString("actualPickup");
                            m.actualReturn = obj.optString("actualReturn");

                            m.totalRent = obj.optDouble("totalRent");
                            m.deposit = obj.optDouble("deposit");
                            m.rentPaid = obj.optDouble("rentPaid");
                            m.balance = obj.optDouble("balance");

                            m.status = obj.optString("status");

                            // 🔥 SAFE items parsing
                            JSONArray itemsArr = obj.optJSONArray("items");

                            List<OrderHistoryModel.HistoryItem> itemList =
                                    new ArrayList<>();

                            if (itemsArr != null) {

                                for (int j = 0; j < itemsArr.length(); j++) {

                                    JSONObject itemObj =
                                            itemsArr.getJSONObject(j);

                                    OrderHistoryModel.HistoryItem item =
                                            new OrderHistoryModel.HistoryItem();

                                    item.itemNo =
                                            itemObj.optString("itemNo");

                                    item.pickupScheduled =
                                            itemObj.optString("pickupScheduled");

                                    item.returnScheduled =
                                            itemObj.optString("returnScheduled");

                                    item.actualPickup =
                                            itemObj.optString("actualPickup");

                                    item.actualReturn =
                                            itemObj.optString("actualReturn");

                                    item.status =
                                            itemObj.optString("status");

                                    item.rent =
                                            itemObj.optDouble("rent");

                                    item.deposit =
                                            itemObj.optDouble("deposit");

                                    itemList.add(item);
                                }
                            }

                            m.items = itemList;

                            fullList.add(m);
                        }

                        Log.d("PARSED_SIZE", "FullList size = " + fullList.size());

                        setupYearSpinner();
                        applyFilter("All");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d("API_RESPONSE", response);

                }, error -> {

        Log.e("API_ERROR", error.toString());

        Toast.makeText(this,
                "Failed to load history",
                Toast.LENGTH_LONG).show();
        });


        Volley.newRequestQueue(this).add(request);
    }

    // ================= YEAR SPINNER =================

    private void setupYearSpinner() {

        Set<Integer> yearSet = new HashSet<>();

        for (OrderHistoryModel m : fullList) {
            yearSet.add(getYear(m.timestamp));
        }

        List<String> years = new ArrayList<>();
        years.add("All");

        for (Integer y : yearSet) {
            years.add(String.valueOf(y));
        }

        Collections.sort(years, Collections.reverseOrder());

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        years);

        spinnerYear.setAdapter(adapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                applyFilter(years.get(pos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ================= FILTER =================

    private void applyFilter(String yearStr) {

        filteredList.clear();

        double total = 0;

        for (OrderHistoryModel m : fullList) {

            if (yearStr.equals("All") ||
                    getYear(m.timestamp) == Integer.parseInt(yearStr)) {

                filteredList.add(m);

                total += m.rentPaid;
            }
        }

        tvTotalEarnings.setText("₹ " + total);
        Log.d("c", "Filtered size = " + filteredList.size());
        adapter.notifyDataSetChanged();
    }

    private int getYear(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal.get(Calendar.YEAR);
    }

}