package swadha.collection.rental;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List; // Added

public class ItemActivity extends AppCompatActivity {

    private List<ItemModel> itemList = new ArrayList<>();
    private ItemAdapter adapter;
    // Make sure this URL matches your script URL

    private FirebaseItemRepository repository;

    private AlertDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_master);

        repository = new FirebaseItemRepository();

        RecyclerView rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new GridLayoutManager(this, 2));


        adapter = new ItemAdapter(itemList);
        rv.setAdapter(adapter);

        listenToItems();

        findViewById(R.id.fabAddItem).setOnClickListener(v -> showAddItemDialog());
    }


    private void listenToItems(){

        repository.listenToItems(

                new FirebaseItemRepository.ItemListener() {

                    @Override
                    public void onItemsLoaded(
                            List<FirebaseItemModel> list
                    ) {

                        itemList.clear();

                        for(FirebaseItemModel item : list){

                            itemList.add(

                                    new ItemModel(

                                            item.itemNo,

                                            item.itemName,

                                            item.rent,

                                            item.deposit,

                                            item.requiresWash,

                                            item.isLocked,

                                            item.nextAvailableMs,

                                            item.currentStatus
                                    )
                            );
                        }

                        runOnUiThread(() -> {

                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onError(String error) {

                        Toast.makeText(
                                ItemActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void showAddItemDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater()
                .inflate(
                        R.layout.dialog_add_item,
                        null
                );

        builder.setView(dialogView);

        EditText etNo =
                dialogView.findViewById(
                        R.id.etDialogItemNo
                );

        EditText etName =
                dialogView.findViewById(
                        R.id.etDialogItemName
                );

        EditText etRent =
                dialogView.findViewById(
                        R.id.etDialogRent
                );

        EditText etDeposit =
                dialogView.findViewById(
                        R.id.etDialogDeposit
                );

        CheckBox cbWash =
                dialogView.findViewById(
                        R.id.cbDialogRequiresWash
                );

        Button btnSave =
                dialogView.findViewById(
                        R.id.btnSaveItem
                );

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {

            // =========================
            // READ INPUTS
            // =========================

            String itemNo = normalizeItemCode(

                    etNo.getText()
                            .toString()
            );

            String itemName = etName.getText()
                    .toString()
                    .trim();

            String rentText = etRent.getText()
                    .toString()
                    .trim();

            String depositText = etDeposit.getText()
                    .toString()
                    .trim();

            // =========================
            // VALIDATION
            // =========================

            if(itemNo.isEmpty()){

                etNo.setError("Required");

                return;
            }

            if(itemName.isEmpty()){

                etName.setError("Required");

                return;
            }

            if(rentText.isEmpty()){

                etRent.setError("Required");

                return;
            }

            if(depositText.isEmpty()){

                etDeposit.setError("Required");

                return;
            }

            // =========================
            // DISABLE BUTTON
            // =========================

            btnSave.setEnabled(false);

            btnSave.setText("Saving...");

            showLoading();

            // =========================
            // CHECK DUPLICATE
            // =========================

            com.google.firebase.firestore.FirebaseFirestore
                    .getInstance()

                    .collection("items")

                    .document(itemNo)

                    .get()

                    .addOnSuccessListener(documentSnapshot -> {

                        // =====================
                        // DUPLICATE FOUND
                        // =====================

                        if(documentSnapshot.exists()){

                            hideLoading();

                            btnSave.setEnabled(true);

                            btnSave.setText("Add Item");

                            etNo.setError(
                                    "Item already exists"
                            );

                            return;
                        }

                        // =====================
                        // CREATE MODEL
                        // =====================

                        FirebaseItemModel item =
                                new FirebaseItemModel();

                        item.itemNo = itemNo;

                        item.itemName = itemName;

                        item.rent =
                                Double.parseDouble(
                                        rentText
                                );

                        item.deposit =
                                Double.parseDouble(
                                        depositText
                                );

                        item.requiresWash =
                                cbWash.isChecked();

                        item.isLocked = false;

                        item.currentStatus =
                                Constants.STATUS_AVAILABLE;

                        item.currentOrderId = "";

                        item.nextAvailableMs = 0;

                        long now =
                                System.currentTimeMillis();

                        item.createdAt = now;

                        item.updatedAt = now;

                        // =====================
                        // SAVE ITEM
                        // =====================

                        repository.addItem(

                                item,

                                new FirebaseItemRepository
                                        .SimpleCallback() {

                                    @Override
                                    public void onSuccess() {

                                        runOnUiThread(() -> {

                                            hideLoading();

                                            Toast.makeText(

                                                    ItemActivity.this,

                                                    "Item Added",

                                                    Toast.LENGTH_SHORT

                                            ).show();

                                            dialog.dismiss();
                                        });
                                    }

                                    @Override
                                    public void onError(
                                            String error
                                    ) {

                                        runOnUiThread(() -> {

                                            hideLoading();

                                            btnSave.setEnabled(
                                                    true
                                            );

                                            btnSave.setText(
                                                    "Add Item"
                                            );

                                            Toast.makeText(

                                                    ItemActivity.this,

                                                    error,

                                                    Toast.LENGTH_LONG

                                            ).show();
                                        });
                                    }
                                }
                        );
                    })

                    .addOnFailureListener(e -> {

                        hideLoading();

                        btnSave.setEnabled(true);

                        btnSave.setText("Add Item");

                        Toast.makeText(

                                ItemActivity.this,

                                e.getMessage(),

                                Toast.LENGTH_LONG

                        ).show();
                    });
        });

        dialog.show();
    }

    private String normalizeItemCode(
            String code
    ){

        return code

                .replaceAll("\\s+", "")

                .replace("-", "")

                .trim()

                .toUpperCase();
    }

    private void showLoading() {

        if(progressDialog != null &&
                progressDialog.isShowing()){

            return;
        }

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        layout.setPadding(
                60,
                50,
                60,
                50
        );

        layout.setGravity(Gravity.CENTER);

        ProgressBar progressBar =
                new ProgressBar(this);

        progressBar.setIndeterminate(true);

        progressBar.setPadding(
                0,
                0,
                30,
                0
        );

        TextView tv =
                new TextView(this);

        tv.setText("Loading...");

        tv.setTextSize(18);

        tv.setTextColor(Color.BLACK);

        layout.addView(progressBar);

        layout.addView(tv);

        progressDialog =
                new AlertDialog.Builder(this)

                        .setView(layout)

                        .setCancelable(false)

                        .create();

        progressDialog.show();
    }

    private void hideLoading() {

        if(progressDialog != null &&
                progressDialog.isShowing()){

            progressDialog.dismiss();
        }
    }
}