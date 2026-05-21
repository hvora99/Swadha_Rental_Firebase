package swadha.collection.rental;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;
import java.util.List;

public class ItemDetailActivity extends AppCompatActivity {

    private String itemNo;
    private FirebaseItemRepository repository;
    EditText etName, etRent, etDeposit;
    boolean isLocked;
    private AlertDialog progressDialog;
    Button btnRemove;

    private boolean requiresWash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        repository = new FirebaseItemRepository();

        itemNo = getIntent().getStringExtra("itemNo");
        loadItemDetails();

        TextView tvCode = findViewById(R.id.tvDetailItemCode);
        etName = findViewById(R.id.etDetailItemName);
        etRent = findViewById(R.id.etDetailRent);
        etDeposit = findViewById(R.id.etDetailDeposit);


        Button btnToggleLock = findViewById(R.id.btnToggleLock);
       // btnToggleLock.setText(isLocked ? "Unlock Item" : "Lock Item");
        updateLockButtonUI(btnToggleLock);


        btnToggleLock.setOnClickListener(v -> {

            showLoading();

            boolean newLockState = !isLocked;

            repository.updateLockStatus(

                    itemNo,

                    newLockState,

                    new FirebaseItemRepository
                            .SimpleCallback() {

                        @Override
                        public void onSuccess() {

                            hideLoading();

                            isLocked = newLockState;

                            updateLockButtonUI(
                                    btnToggleLock
                            );

                            Toast.makeText(
                                    ItemDetailActivity.this,

                                    isLocked
                                            ? "Item Locked"
                                            : "Item Unlocked",

                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onError(
                                String error
                        ) {

                            hideLoading();

                            Toast.makeText(
                                    ItemDetailActivity.this,
                                    error,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });

        tvCode.setText(itemNo);

        Button btnUpdate = findViewById(R.id.btnUpdateItem);
        btnRemove = findViewById(R.id.btnRemoveItem);
        btnRemove.setEnabled(false);
        btnRemove.setAlpha(0.4f);

            btnUpdate.setOnClickListener(v -> {

                String newName = etName.getText().toString().trim();
                String newRent = etRent.getText().toString().trim();
                String newDeposit = etDeposit.getText().toString().trim();

                if (newName.isEmpty()) {
                    Toast.makeText(this, "Item name required", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateItemOnServer(newName, newRent, newDeposit);
            });

        btnRemove.setOnClickListener(v -> {

                new AlertDialog.Builder(this)
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            deleteItemFromServer(itemNo);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
        });

        // Next step: load current booking
    }

    private void loadItemDetails(){

        showLoading();



        com.google.firebase.firestore.FirebaseFirestore
                .getInstance()

                .collection("items")

                .document(itemNo)

                .get()

                .addOnSuccessListener(documentSnapshot -> {

                    hideLoading();

                    FirebaseItemModel item =
                            documentSnapshot.toObject(
                                    FirebaseItemModel.class
                            );

                    if(item == null){

                        Toast.makeText(
                                this,
                                "Item not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();

                        return;
                    }

                    isLocked = item.isLocked;

                    TextView tvCode =
                            findViewById(
                                    R.id.tvDetailItemCode
                            );

                    tvCode.setText(item.itemNo);

                    etName.setText(item.itemName);

                    etRent.setText(
                            String.valueOf(item.rent)
                    );

                    etDeposit.setText(
                            String.valueOf(item.deposit)
                    );

                    requiresWash = item.requiresWash;

                    Button btnToggleLock =
                            findViewById(
                                    R.id.btnToggleLock
                            );

                    updateLockButtonUI(btnToggleLock);

                    updateBookingUI(item);
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
    private void showLoading() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50,50,50,50);
        layout.setGravity(Gravity.CENTER);

        ProgressBar bar = new ProgressBar(this);
        bar.setIndeterminate(true);
        bar.setPadding(0,0,30,0);

        TextView tv = new TextView(this);
        tv.setText("Loading...");
        tv.setTextSize(18);
        tv.setTextColor(Color.BLACK);

        layout.addView(bar);
        layout.addView(tv);

        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(layout)
                .create();

        progressDialog.show();
    }

    private void hideLoading() {

        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
    private void updateLockButtonUI(Button button) {

        if (isLocked) {
            button.setText("Unlock Item");
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.RED)
            );
        } else {
            button.setText("Lock Item");
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
            );
        }
    }

    private void updateBookingUI(
            FirebaseItemModel item
    ){

        RecyclerView rv =
                findViewById(
                        R.id.rvCurrentBooking
                );

        TextView tvNoBooking =
                findViewById(
                        R.id.tvNoBooking
                );

        rv.setLayoutManager(
                new LinearLayoutManager(this)
        );

        if(item.currentOrderId == null ||
                item.currentOrderId.isEmpty()){

            rv.setVisibility(View.GONE);

            tvNoBooking.setVisibility(View.VISIBLE);

            btnRemove.setEnabled(true);

            btnRemove.setAlpha(1f);

            return;
        }

        tvNoBooking.setVisibility(View.GONE);

        rv.setVisibility(View.VISIBLE);

        btnRemove.setEnabled(false);

        btnRemove.setAlpha(0.4f);

        List<CurrentBookingModel> list =
                new ArrayList<>();

        list.add(

                new CurrentBookingModel(

                        "Order: " + item.currentOrderId,

                        "",

                        item.currentStatus,

                        "",

                        "",

                        "",

                        ""
                )
        );

        rv.setAdapter(
                new CurrentBookingAdapter(list)
        );

        expandRecyclerView(rv);
    }
    private void updateItemOnServer(

            String name,

            String rent,

            String deposit
    ){

        showLoading();

        repository.updateBasicInfo(

                itemNo,

                name,

                Double.parseDouble(rent),

                Double.parseDouble(deposit),

                requiresWash,

                new FirebaseItemRepository
                        .SimpleCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                "Item Updated",

                                Toast.LENGTH_SHORT

                        ).show();

                        setResult(RESULT_OK);

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }
    private void deleteItemFromServer(
            String itemNo
    ){

        showLoading();

        repository.deleteItem(

                itemNo,

                new FirebaseItemRepository
                        .SimpleCallback() {

                    @Override
                    public void onSuccess() {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                "Item Removed",

                                Toast.LENGTH_SHORT

                        ).show();

                        setResult(RESULT_OK);

                        finish();
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        hideLoading();

                        Toast.makeText(

                                ItemDetailActivity.this,

                                error,

                                Toast.LENGTH_LONG

                        ).show();
                    }
                }
        );
    }
    private void expandRecyclerView(RecyclerView recyclerView){

        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if(adapter == null) return;

        int totalHeight = 0;

        for(int i=0;i<adapter.getItemCount();i++){

            RecyclerView.ViewHolder holder =
                    adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));

            adapter.onBindViewHolder(holder,i);

            holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
            );

            totalHeight += holder.itemView.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = totalHeight + (adapter.getItemCount() * 20);

        recyclerView.setLayoutParams(params);
    }
}