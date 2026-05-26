package swadha.collection.rental;

import android.os.Bundle;
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

    EditText etHistorySearch;

    private List<OrderHistoryModel>
            currentYearList =
            new ArrayList<>();

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

        etHistorySearch = findViewById(R.id.etHistorySearch);
        etHistorySearch.addTextChangedListener(

                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {

                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        searchHistory(
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {

                    }
                });

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

        Set<Integer> yearSet =
                new HashSet<>();

        for(OrderHistoryModel m : fullList){

            // IGNORE INVALID TIMESTAMP

            if(m.archivedAt <= 0){
                continue;
            }

            yearSet.add(
                    getYear(m.archivedAt)
            );
        }

        List<String> years =
                new ArrayList<>();

        // ADD YEARS

        for(Integer y : yearSet){

            years.add(
                    String.valueOf(y)
            );
        }

        // SORT DESCENDING

        Collections.sort(
                years,
                Collections.reverseOrder()
        );

        ArrayAdapter<String> adapter =

                new ArrayAdapter<>(

                        this,

                        android.R.layout
                                .simple_spinner_dropdown_item,

                        years
                );

        spinnerYear.setAdapter(adapter);

        // SELECT CURRENT YEAR

        int currentYear =

                Calendar.getInstance()
                        .get(Calendar.YEAR);

        int selectedIndex = 0;

        for(int i = 0; i < years.size(); i++){

            if(years.get(i).equals(
                    String.valueOf(currentYear)
            )){

                selectedIndex = i;

                break;
            }
        }

        spinnerYear.setSelection(
                selectedIndex
        );

        // APPLY CURRENT YEAR FILTER

        applyFilter(
                years.get(selectedIndex)
        );

        spinnerYear.setOnItemSelectedListener(

                new AdapterView
                        .OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(

                            AdapterView<?> parent,

                            View view,

                            int pos,

                            long id
                    ) {

                        applyFilter(
                                years.get(pos)
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {

                    }
                });
    }
    // ================= FILTER =================

    private void applyFilter(
            String yearStr
    ){

        filteredList.clear();

        currentYearList.clear();

        double total = 0;

        for(OrderHistoryModel m : fullList){

            if(yearStr.equals("All")

                    ||

                    getYear(m.archivedAt)

                            == Integer.parseInt(
                            yearStr
                    )){

                filteredList.add(m);

                currentYearList.add(m);

                total += m.totalRentPaid;
            }
        }

        tvTotalEarnings.setText(
                "₹ " + total
        );

        adapter.notifyDataSetChanged();
    }
    private int getYear(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal.get(Calendar.YEAR);
    }

    private void searchHistory(
            String query
    ){

        query = query.toLowerCase().trim();

        filteredList.clear();

        double total = 0;

        for(OrderHistoryModel order
                : currentYearList){

            boolean matched = false;

            // CUSTOMER

            if(order.customerName != null

                    &&

                    order.customerName
                            .toLowerCase()
                            .contains(query)){

                matched = true;
            }

            // PHONE

            else if(order.phone != null

                    &&

                    order.phone.contains(query)){

                matched = true;
            }

            else if(order.alternatePhone != null

                    &&

                    order.alternatePhone.contains(query)){

                matched = true;
            }

            // ORDER ID

            else if(order.orderId != null

                    &&

                    order.orderId
                            .toLowerCase()
                            .contains(query)){

                matched = true;
            }

            // ITEM SEARCH

            else if(order.items != null){

                for(OrderHistoryModel.HistoryItem item
                        : order.items){

                    if(item.itemNo != null

                            &&

                            item.itemNo
                                    .toLowerCase()
                                    .contains(query)){

                        matched = true;

                        break;
                    }
                }
            }

            if(matched){

                filteredList.add(order);

                total += order.totalRentPaid;
            }
        }

        tvTotalEarnings.setText(
                "₹ " + total
        );

        adapter.notifyDataSetChanged();
    }
}