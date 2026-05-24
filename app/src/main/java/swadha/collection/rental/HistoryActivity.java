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


import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QueryDocumentSnapshot;
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private Spinner spinnerYear;
    private TextView tvTotalEarnings;

    private HistoryAdapter adapter;

    private List<OrderHistoryModel> fullList = new ArrayList<>();
    private List<OrderHistoryModel> filteredList = new ArrayList<>();
    private FirebaseFirestore db =
            FirebaseFirestore.getInstance();

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

        db.collection("order_history")

                .get()

                .addOnSuccessListener(query -> {

                    fullList.clear();

                    for(QueryDocumentSnapshot doc
                            : query){

                        OrderHistoryModel model =

                                doc.toObject(
                                        OrderHistoryModel.class
                                );

                        fullList.add(model);
                    }

                    setupYearSpinner();

                    applyFilter("All");

                }).addOnFailureListener(e -> {

                    Toast.makeText(

                            this,

                            "Failed to load history",

                            Toast.LENGTH_LONG

                    ).show();
                });
    }

    // ================= YEAR SPINNER =================

    private void setupYearSpinner() {

        Set<Integer> yearSet = new HashSet<>();

        for (OrderHistoryModel m : fullList) {
            yearSet.add(getYear(m.archivedAt));        }

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

                    getYear(m.archivedAt)
                            == Integer.parseInt(yearStr)) {

                filteredList.add(m);

                total += m.totalRentPaid;
            }
        }

        tvTotalEarnings.setText("₹ " + total);

        adapter.notifyDataSetChanged();
    }

    private int getYear(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal.get(Calendar.YEAR);
    }

}