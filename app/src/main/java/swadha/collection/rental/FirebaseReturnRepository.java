package swadha.collection.rental;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.List;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
public class FirebaseReturnRepository {

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    public interface ActionCallback {

        void onSuccess();

        void onError(String error);
    }

    public void markItemsPickedUp(

            String orderId,

            List<String> itemNos,

            double paidNow,

            ActionCallback callback
    ){

        db.runTransaction(

                (Transaction.Function<Void>) transaction -> {

                    DocumentReference orderRef =

                            db.collection("orders")
                                    .document(orderId);

                    // =========================
                    // READ ORDER FIRST
                    // =========================

                    FirebaseOrderModel order =

                            transaction

                                    .get(orderRef)

                                    .toObject(
                                            FirebaseOrderModel.class
                                    );

                    if(order == null){

                        throw new RuntimeException(
                                "Order not found"
                        );
                    }

                    // =========================
                    // READ ALL ITEMS FIRST
                    // =========================

                    List<FirebaseOrderItemModel>
                            loadedItems =
                            new ArrayList<>();

                    List<DocumentReference>
                            orderItemRefs =
                            new ArrayList<>();

                    for(String itemNo : itemNos){

                        DocumentReference orderItemRef =

                                orderRef

                                        .collection("items")

                                        .document(itemNo);

                        FirebaseOrderItemModel item =

                                transaction

                                        .get(orderItemRef)

                                        .toObject(
                                                FirebaseOrderItemModel.class
                                        );

                        if(item == null){

                            throw new RuntimeException(
                                    "Item not found : " + itemNo
                            );
                        }

                        if(!item.status.equalsIgnoreCase(
                                "Booked"
                        )){

                            throw new RuntimeException(
                                    itemNo + " already processed"
                            );
                        }

                        loadedItems.add(item);

                        orderItemRefs.add(orderItemRef);
                    }

                    // =========================
                    // NOW START WRITES
                    // =========================

                    for(int i=0;i<loadedItems.size();i++){

                        FirebaseOrderItemModel item =
                                loadedItems.get(i);

                        DocumentReference orderItemRef =
                                orderItemRefs.get(i);

                        transaction.update(

                                orderItemRef,

                                "status",
                                "PickedUp"
                        );

                        DocumentReference inventoryRef =

                                db.collection("items")

                                        .document(item.itemNo);

                        transaction.update(

                                inventoryRef,

                                "currentStatus",
                                "PickedUp",

                                "updatedAt",
                                System.currentTimeMillis()
                        );
                    }

                    // =========================
                    // UPDATE ORDER
                    // =========================

                    double updatedPaid =

                            order.totalRentPaid
                                    + paidNow;

                    double updatedBalance =

                            order.totalRent
                                    - updatedPaid;

                    transaction.update(

                            orderRef,

                            "totalRentPaid",
                            updatedPaid,

                            "balanceRent",
                            updatedBalance,

                            "status",
                            "PickedUp",

                            "actualPickupMs",
                            System.currentTimeMillis(),

                            "updatedAt",
                            System.currentTimeMillis()
                    );

                    return null;

                }).addOnSuccessListener(unused -> {

            callback.onSuccess();

        }).addOnFailureListener(e -> {

            callback.onError(
                    e.getMessage()
            );
        });
    }
    public void markItemsReturned(

            String orderId,

            List<String> itemNos,

            double refundAmount,

            ActionCallback callback
    ){

        db.runTransaction(

                (Transaction.Function<Void>) transaction -> {

                    DocumentReference orderRef =

                            db.collection("orders")
                                    .document(orderId);

                    // =========================
                    // READ ALL ITEMS FIRST
                    // =========================

                    List<FirebaseOrderItemModel>
                            loadedItems =
                            new ArrayList<>();

                    List<DocumentReference>
                            orderItemRefs =
                            new ArrayList<>();

                    boolean allReturned = true;

                    for(String itemNo : itemNos){

                        DocumentReference orderItemRef =

                                orderRef

                                        .collection("items")

                                        .document(itemNo);

                        FirebaseOrderItemModel item =

                                transaction

                                        .get(orderItemRef)

                                        .toObject(
                                                FirebaseOrderItemModel.class
                                        );

                        if(item == null){

                            throw new RuntimeException(

                                    "Item not found : " + itemNo
                            );
                        }

                        if(!item.status.equalsIgnoreCase(
                                "PickedUp"
                        )){

                            throw new RuntimeException(

                                    itemNo
                                            + " not picked up"
                            );
                        }

                        loadedItems.add(item);

                        orderItemRefs.add(orderItemRef);
                    }

                    // =========================
                    // CHECK IF ALL ITEMS RETURNED
                    // =========================

                    for(FirebaseOrderItemModel item
                            : loadedItems){

                        if(!item.status.equalsIgnoreCase(
                                "PickedUp"
                        )){

                            allReturned = false;

                            break;
                        }
                    }

                    // =========================
                    // START WRITES
                    // =========================

                    for(int i=0;i<loadedItems.size();i++){

                        FirebaseOrderItemModel item =
                                loadedItems.get(i);

                        DocumentReference orderItemRef =
                                orderItemRefs.get(i);

                        // =====================
                        // UPDATE ORDER ITEM
                        // =====================

                        transaction.update(

                                orderItemRef,

                                "status",
                                "Returned"
                        );

                        // =====================
                        // INVENTORY STATUS
                        // =====================

                        String inventoryStatus;

                        long nextAvailableMs;

                        if(item.requiresWash){

                            inventoryStatus = "Washing";

                            nextAvailableMs = item.washMs;

                        }else{

                            inventoryStatus = "Available";

                            nextAvailableMs =
                                    System.currentTimeMillis();
                        }

                        // =====================
                        // UPDATE INVENTORY
                        // =====================

                        DocumentReference inventoryRef =

                                db.collection("items")

                                        .document(item.itemNo);

                        transaction.update(

                                inventoryRef,

                                "currentStatus",
                                inventoryStatus,

                                "currentOrderId",
                                "",

                                "nextAvailableMs",
                                nextAvailableMs,

                                "updatedAt",
                                System.currentTimeMillis()
                        );
                    }

                    // =========================
                    // UPDATE ORDER
                    // =========================

                    transaction.update(

                            orderRef,

                            "refundAmount",
                            refundAmount,

                            "updatedAt",
                            System.currentTimeMillis()
                    );

                    if(allReturned){

                        transaction.update(

                                orderRef,

                                "status",
                                "Returned"
                        );
                    }

                    return null;

                }).addOnSuccessListener(unused -> {

            callback.onSuccess();

        }).addOnFailureListener(e -> {

            callback.onError(
                    e.getMessage()
            );
        });
    }

    public interface OnItemsLoaded{

        void onLoaded(
                List<FirebaseOrderItemModel> items
        );

        void onError(String error);
    }

    public void loadOrderItems(

            String orderId,

            OnItemsLoaded callback
    ){

        db.collection("orders")

                .document(orderId)

                .collection("items")

                .get()

                .addOnSuccessListener(query -> {

                    List<FirebaseOrderItemModel>
                            itemList =
                            new ArrayList<>();

                    for(DocumentSnapshot doc
                            : query){

                        FirebaseOrderItemModel item =

                                doc.toObject(
                                        FirebaseOrderItemModel.class
                                );

                        if(item != null){

                            itemList.add(item);
                        }
                    }

                    callback.onLoaded(
                            itemList
                    );

                }).addOnFailureListener(e -> {

                    callback.onError(
                            e.getMessage()
                    );
                });

    }

        public void cancelBooking(

                String orderId,

                List<String> itemNos,

        double refundAmount,

        ActionCallback callback
        ){

            db.runTransaction(

                    (Transaction.Function<Void>) transaction -> {

                        DocumentReference orderRef =

                                db.collection("orders")
                                        .document(orderId);

                        // =========================
                        // READ ALL ITEMS FIRST
                        // =========================

                        List<FirebaseOrderItemModel>
                                loadedItems =
                                new ArrayList<>();

                        List<DocumentReference>
                                orderItemRefs =
                                new ArrayList<>();

                        boolean allCancelled = true;

                        for(String itemNo : itemNos){

                            DocumentReference orderItemRef =

                                    orderRef

                                            .collection("items")

                                            .document(itemNo);

                            FirebaseOrderItemModel item =

                                    transaction

                                            .get(orderItemRef)

                                            .toObject(
                                                    FirebaseOrderItemModel.class
                                            );

                            if(item == null){

                                throw new RuntimeException(

                                        "Item not found : " + itemNo
                                );
                            }

                            // =====================
                            // VALIDATION
                            // =====================

                            if(!item.status.equalsIgnoreCase(
                                    "Booked"
                            )){

                                throw new RuntimeException(

                                        itemNo
                                                + " cannot cancel"
                                );
                            }

                            loadedItems.add(item);

                            orderItemRefs.add(orderItemRef);
                        }

                        // =========================
                        // START WRITES
                        // =========================

                        for(int i=0;i<loadedItems.size();i++){

                            FirebaseOrderItemModel item =
                                    loadedItems.get(i);

                            DocumentReference orderItemRef =
                                    orderItemRefs.get(i);

                            // =====================
                            // ORDER ITEM
                            // =====================

                            transaction.update(

                                    orderItemRef,

                                    "status",
                                    "Cancelled"
                            );

                            // =====================
                            // INVENTORY
                            // =====================

                            DocumentReference inventoryRef =

                                    db.collection("items")

                                            .document(item.itemNo);

                            transaction.update(

                                    inventoryRef,

                                    "currentStatus",
                                    "Available",

                                    "currentOrderId",
                                    "",

                                    "nextAvailableMs",
                                    System.currentTimeMillis(),

                                    "updatedAt",
                                    System.currentTimeMillis()
                            );
                        }

                        // =========================
                        // UPDATE ORDER
                        // =========================

                        transaction.update(

                                orderRef,

                                "refundAmount",
                                refundAmount,

                                "updatedAt",
                                System.currentTimeMillis()
                        );

                        if(allCancelled){

                            transaction.update(

                                    orderRef,

                                    "status",
                                    "Cancelled"
                            );
                        }

                        return null;

                    }).addOnSuccessListener(unused -> {

                callback.onSuccess();

            }).addOnFailureListener(e -> {

                callback.onError(
                        e.getMessage()
                );
            });
        }
    }
