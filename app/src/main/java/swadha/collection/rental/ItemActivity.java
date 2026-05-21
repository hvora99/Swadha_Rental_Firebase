package swadha.collection.rental;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest; // Added
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List; // Added

public class ItemActivity extends AppCompatActivity {

    private List<ItemModel> itemList = new ArrayList<>();
    private ItemAdapter adapter;
    // Make sure this URL matches your script URL

    private FirebaseItemRepository repository;
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

            String itemNo =
                    etNo.getText()
                            .toString()
                            .trim()
                            .toUpperCase();

            String itemName =
                    etName.getText()
                            .toString()
                            .trim();

            String rentText =
                    etRent.getText()
                            .toString()
                            .trim();

            String depositText =
                    etDeposit.getText()
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

            // =========================
            // CREATE MODEL
            // =========================

            FirebaseItemModel item =
                    new FirebaseItemModel();

            item.itemNo = itemNo;

            item.itemName = itemName;

            item.rent =
                    Double.parseDouble(rentText);

            item.deposit =
                    Double.parseDouble(depositText);

            item.requiresWash =
                    cbWash.isChecked();

            item.isLocked = false;

            item.currentStatus = "Available";

            item.currentOrderId = "";

            item.nextAvailableMs = 0;

            long now = System.currentTimeMillis();

            item.createdAt = now;

            item.updatedAt = now;
            // =========================
            // SAVE TO FIREBASE
            // =========================

            repository.addItem(
                    item,

                    new FirebaseItemRepository
                            .SimpleCallback() {

                        @Override
                        public void onSuccess() {

                            runOnUiThread(() -> {

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

                                btnSave.setEnabled(true);

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
        });

        dialog.show();
    }

}