package swadha.collection.rental;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseItemRepository {

    private final FirebaseFirestore db;

    public FirebaseItemRepository() {

        db = FirebaseFirestore.getInstance();
    }

    // =====================================================
    // LISTEN ALL ITEMS (Realtime)
    // =====================================================

    public ListenerRegistration listenToItems(
            ItemListener listener
    ){

        return db.collection("items")

                .addSnapshotListener((value, error) -> {

                    if(error != null){

                        listener.onError(error.getMessage());

                        return;
                    }

                    List<FirebaseItemModel> list =
                            new ArrayList<>();

                    if(value != null){

                        for(DocumentSnapshot doc :
                                value.getDocuments()){

                            FirebaseItemModel item =
                                    doc.toObject(
                                            FirebaseItemModel.class
                                    );

                            if(item != null){

                                list.add(item);
                            }
                        }
                    }

                    listener.onItemsLoaded(list);
                });
    }

    // =====================================================
    // ADD ITEM
    // =====================================================

    public void addItem(
            FirebaseItemModel item,
            SimpleCallback callback
    ){

        db.collection("items")
                .document(item.itemNo)
                .set(item)

                .addOnSuccessListener(unused -> {

                    callback.onSuccess();
                })

                .addOnFailureListener(e -> {

                    callback.onError(e.getMessage());
                });
    }

    // =====================================================
    // UPDATE ITEM
    // =====================================================




    public void updateBasicInfo(

            String itemNo,

            String itemName,

            double rent,

            double deposit,

            boolean requiresWash,

            SimpleCallback callback
    ){

        db.collection("items")

                .document(itemNo)

                .update(

                        "itemName", itemName,

                        "rent", rent,

                        "deposit", deposit,

                        "requiresWash",
                        requiresWash,

                        "updatedAt",
                        System.currentTimeMillis()
                )

                .addOnSuccessListener(unused -> {

                    callback.onSuccess();
                })

                .addOnFailureListener(e -> {

                    callback.onError(
                            e.getMessage()
                    );
                });
    }

    // =====================================================
    // DELETE ITEM
    // =====================================================

    public void deleteItem(

            String itemNo,

            SimpleCallback callback
    ){

        db.collection("items")

                .document(itemNo)

                .get()

                .addOnSuccessListener(documentSnapshot -> {

                    FirebaseItemModel item =
                            documentSnapshot.toObject(
                                    FirebaseItemModel.class
                            );

                    if(item == null){

                        callback.onError(
                                "Item not found"
                        );

                        return;
                    }

                    // =====================
                    // SAFETY CHECK
                    // =====================

                    if(item.currentOrderId != null &&
                            !item.currentOrderId.isEmpty()){

                        callback.onError(
                                "Booked item cannot be removed"
                        );

                        return;
                    }

                    // =====================
                    // DELETE
                    // =====================

                    db.collection("items")

                            .document(itemNo)

                            .delete()

                            .addOnSuccessListener(unused -> {

                                callback.onSuccess();
                            })

                            .addOnFailureListener(e -> {

                                callback.onError(
                                        e.getMessage()
                                );
                            });
                })

                .addOnFailureListener(e -> {

                    callback.onError(
                            e.getMessage()
                    );
                });
    }
    // =====================================================
    // LOCK / UNLOCK ITEM
    // =====================================================

    public void updateLockStatus(

            String itemNo,

            boolean isLocked,

            SimpleCallback callback
    ){

        db.collection("items")

                .document(itemNo)

                .update(

                        "isLocked", isLocked,

                        "updatedAt",
                        System.currentTimeMillis()
                )

                .addOnSuccessListener(unused -> {

                    callback.onSuccess();
                })

                .addOnFailureListener(e -> {

                    callback.onError(
                            e.getMessage()
                    );
                });
    }

    public void updateItemStatus(

            String itemNo,

            String status,

            String orderId,

            long nextAvailableMs,

            SimpleCallback callback
    ){

        db.collection("items")

                .document(itemNo)

                .update(

                        "currentStatus", status,

                        "currentOrderId", orderId,

                        "nextAvailableMs",
                        nextAvailableMs,

                        "updatedAt",
                        System.currentTimeMillis()
                )

                .addOnSuccessListener(unused -> {

                    callback.onSuccess();
                })

                .addOnFailureListener(e -> {

                    callback.onError(
                            e.getMessage()
                    );
                });
    }

    public void markItemAvailable(

            String itemNo,

            SimpleCallback callback
    ){

        db.collection("items")

                .document(itemNo)

                .update(

                        "currentStatus",
                        Constants.STATUS_AVAILABLE,

                        "currentOrderId",
                        "",

                        "nextAvailableMs",
                        0,

                        "updatedAt",
                        System.currentTimeMillis()
                )

                .addOnSuccessListener(unused -> {

                    callback.onSuccess();
                })

                .addOnFailureListener(e -> {

                    callback.onError(
                            e.getMessage()
                    );
                });
    }

    // =====================================================
    // CALLBACKS
    // =====================================================

    public interface ItemListener {

        void onItemsLoaded(
                List<FirebaseItemModel> list
        );

        void onError(String error);
    }

    public interface SimpleCallback {

        void onSuccess();

        void onError(String error);
    }
}