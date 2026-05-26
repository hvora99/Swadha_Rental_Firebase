package swadha.collection.rental;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.android.material.card.MaterialCardView;

public class BackupActivity extends AppCompatActivity {
    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();
    private JSONObject backupJson =
            new JSONObject();
    MaterialCardView cardExport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_backup);

        cardExport =
                findViewById(
                        R.id.cardExport
                );

        cardExport.setOnClickListener(v -> {

            exportBackup();
        });

        loadLastBackupInfo();
    }

    private void exportBackup(){

        Toast.makeText(

                this,

                "Preparing backup...",

                Toast.LENGTH_SHORT

        ).show();

        fetchItems();
    }

    private void fetchItems(){

        db.collection("items")

                .get()

                .addOnSuccessListener(query -> {

                    JSONArray itemsArray =
                            new JSONArray();

                    for(DocumentSnapshot doc
                            : query.getDocuments()){

                        try{

                            JSONObject obj =

                                    new JSONObject(
                                            doc.getData()
                                    );

                            itemsArray.put(obj);

                        }catch (Exception e){

                            e.printStackTrace();
                        }
                    }

                    try{

                        backupJson.put(
                                "items",
                                itemsArray
                        );

                    }catch (Exception e){

                        e.printStackTrace();
                    }

                    fetchOrders();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(

                            this,

                            e.getMessage(),

                            Toast.LENGTH_LONG

                    ).show();
                });
    }

    private void fetchOrders(){

        db.collection("orders")

                .get()

                .addOnSuccessListener(orderQuery -> {

                    JSONArray ordersArray =
                            new JSONArray();

                    List<DocumentSnapshot> orders =
                            orderQuery.getDocuments();

                    if(orders.isEmpty()){

                        try{

                            backupJson.put(
                                    "orders",
                                    ordersArray
                            );

                        }catch (Exception e){

                            e.printStackTrace();
                        }

                        fetchHistory();

                        return;
                    }

                    final int[] processed = {0};

                    for(DocumentSnapshot orderDoc
                            : orders){

                        JSONObject orderJson =
                                new JSONObject();

                        try{

                            orderJson = new JSONObject(
                                    orderDoc.getData()
                            );

                        }catch (Exception e){

                            e.printStackTrace();
                        }

                        JSONObject finalOrderJson =
                                orderJson;

                        orderDoc

                                .getReference()

                                .collection("items")

                                .get()

                                .addOnSuccessListener(itemQuery -> {

                                    JSONArray itemsArray =
                                            new JSONArray();

                                    for(DocumentSnapshot itemDoc
                                            : itemQuery.getDocuments()){

                                        try{

                                            JSONObject itemJson =

                                                    new JSONObject(
                                                            itemDoc.getData()
                                                    );

                                            itemsArray.put(
                                                    itemJson
                                            );

                                        }catch (Exception e){

                                            e.printStackTrace();
                                        }
                                    }

                                    try{

                                        finalOrderJson.put(
                                                "items",
                                                itemsArray
                                        );

                                    }catch (Exception e){

                                        e.printStackTrace();
                                    }

                                    ordersArray.put(
                                            finalOrderJson
                                    );

                                    processed[0]++;

                                    if(processed[0]
                                            >= orders.size()){

                                        try{

                                            backupJson.put(
                                                    "orders",
                                                    ordersArray
                                            );

                                        }catch (Exception e){

                                            e.printStackTrace();
                                        }

                                        fetchHistory();
                                    }
                                });
                    }
                });
    }

    private void fetchHistory(){

        db.collection("order_history")

                .get()

                .addOnSuccessListener(query -> {

                    JSONArray historyArray =
                            new JSONArray();

                    for(DocumentSnapshot doc
                            : query.getDocuments()){

                        try{

                            JSONObject obj =

                                    new JSONObject(
                                            doc.getData()
                                    );

                            historyArray.put(obj);

                        }catch (Exception e){

                            e.printStackTrace();
                        }
                    }

                    try{

                        backupJson.put(
                                "order_history",
                                historyArray
                        );

                    }catch (Exception e){

                        e.printStackTrace();
                    }

                    saveBackupFile();

                });
    }

    private void saveBackupFile(){

        try{

            // =====================
            // CREATE FOLDER
            // =====================
            File folder =

                    new File(

                            android.os.Environment

                                    .getExternalStoragePublicDirectory(

                                            android.os.Environment
                                                    .DIRECTORY_DOCUMENTS
                                    ),

                            "SvadhaBackup"
                    );

            if(!folder.exists()){

                folder.mkdirs();
            }

            // =====================
            // FILE NAME
            // =====================

            String timeStamp =

                    new SimpleDateFormat(

                            "dd_MM_yyyy_HH_mm",

                            Locale.getDefault()

                    ).format(new Date());

            String fileName =

                    "svadha_backup_"

                            + timeStamp

                            + ".json";

            File backupFile =
                    new File(folder, fileName);

            // =====================
            // WRITE JSON
            // =====================

            FileWriter writer =
                    new FileWriter(backupFile);

            writer.write(
                    backupJson.toString(4)
            );

            writer.flush();

            writer.close();

            android.media.MediaScannerConnection
                    .scanFile(

                            this,

                            new String[]{
                                    backupFile.getAbsolutePath()
                            },

                            null,

                            null
                    );
            // =====================
            // SUCCESS
            // =====================

            Toast.makeText(

                    this,

                    "Backup saved:\n"
                            + backupFile.getAbsolutePath(),

                    Toast.LENGTH_LONG

            ).show();

            loadLastBackupInfo();

        }

        catch (Exception e){

            e.printStackTrace();

            Toast.makeText(

                    this,

                    e.getMessage(),

                    Toast.LENGTH_LONG

            ).show();
        }
    }

    private void loadLastBackupInfo(){

        TextView tvLastBackup =
                findViewById(
                        R.id.tvLastBackup
                );

        File folder =

                new File(

                        android.os.Environment

                                .getExternalStoragePublicDirectory(

                                        android.os.Environment
                                                .DIRECTORY_DOCUMENTS
                                ),

                        "SvadhaBackup"
                );

        if(!folder.exists()){

            tvLastBackup.setText(
                    "Last Backup : Never"
            );

            return;
        }

        File[] files =
                folder.listFiles();

        if(files == null
                ||
                files.length == 0){

            tvLastBackup.setText(
                    "Last Backup : Never"
            );

            return;
        }

        File latestFile = files[0];

        for(File file : files){

            if(file.lastModified()
                    > latestFile.lastModified()){

                latestFile = file;
            }
        }

        String formattedDate =

                new SimpleDateFormat(

                        "dd MMM yyyy hh:mm a",

                        Locale.getDefault()

                ).format(

                        new Date(
                                latestFile.lastModified()
                        )
                );

        tvLastBackup.setText(

                "Last Backup : "
                        + formattedDate
        );
    }
}