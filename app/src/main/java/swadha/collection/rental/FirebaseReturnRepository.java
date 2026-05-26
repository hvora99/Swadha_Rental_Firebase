package swadha.collection.rental;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.List;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

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
    ) {

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

                    if (order == null) {

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

                    double remainingPaid =
                            paidNow;

                    for (String itemNo : itemNos) {

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

                        if (item == null) {

                            throw new RuntimeException(
                                    "Item not found : " + itemNo
                            );
                        }

                        if (!item.status.equalsIgnoreCase(
                                "Booked"
                        )) {

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

                    for (int i = 0; i < loadedItems.size(); i++) {

                        FirebaseOrderItemModel item =
                                loadedItems.get(i);

                        DocumentReference orderItemRef =
                                orderItemRefs.get(i);

                        double pendingRent =

                                item.customRent
                                        - item.rentPaid;




                        double additionalPaid = Math.min(

                                remainingPaid,

                                pendingRent
                        );

                        remainingPaid -= additionalPaid;

                        double updatedRentPaid =

                                item.rentPaid
                                        + additionalPaid;
                        transaction.update(

                                orderItemRef,

                                "status",
                                "PickedUp",

                                "rentPaid",
                                updatedRentPaid
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

            double refundedRent,

            double refundedDeposit,

            ActionCallback callback
    ){

        double totalRefund =
                refundedRent
                        + refundedDeposit;

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

                    for (String itemNo : itemNos) {

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

                        if (item == null) {

                            throw new RuntimeException(

                                    "Item not found : " + itemNo
                            );
                        }

                        if (!item.status.equalsIgnoreCase(
                                "PickedUp"
                        )) {

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

                    for (FirebaseOrderItemModel item
                            : loadedItems) {

                        if (!item.status.equalsIgnoreCase(
                                "PickedUp"
                        )) {

                            allReturned = false;

                            break;
                        }
                    }

                    // =========================
                    // START WRITES
                    // =========================
                    double remainingRentRefund =
                            refundedRent;

                    double remainingDepositRefund =
                            refundedDeposit;

                    for (int i = 0; i < loadedItems.size(); i++) {

                        FirebaseOrderItemModel item =
                                loadedItems.get(i);

                        double itemRentRefund = Math.min(

                                remainingRentRefund,

                                item.rentPaid
                        );

                        remainingRentRefund -=
                                itemRentRefund;


                        double itemDepositRefund = Math.min(

                                remainingDepositRefund,

                                item.customDeposit
                        );

                        remainingDepositRefund -=
                                itemDepositRefund;


                        double itemTotalRefund =

                                itemRentRefund
                                        + itemDepositRefund;

                        DocumentReference orderItemRef =
                                orderItemRefs.get(i);

                        // =====================
                        // UPDATE ORDER ITEM
                        // =====================

                        transaction.update(

                                orderItemRef,

                                "status",
                                "Returned",

                                "refundedRent",
                                itemRentRefund,

                                "refundedDeposit",
                                itemDepositRefund,

                                "totalRefund",
                                itemTotalRefund
                        );

                        // =====================
                        // INVENTORY STATUS
                        // =====================

                        String inventoryStatus;

                        long nextAvailableMs;

                        if (item.requiresWash) {

                            inventoryStatus = "Washing";

                            nextAvailableMs = item.washMs;

                        } else {

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
                            totalRefund,

                            "updatedAt",
                            System.currentTimeMillis()
                    );

                    if (allReturned) {

                        transaction.update(

                                orderRef,

                                "status",
                                "Returned"
                        );
                    }

                    return null;

                }).addOnSuccessListener(unused -> {

            callback.onSuccess();

            maybeArchiveOrder(orderId);

        }).addOnFailureListener(e -> {

            callback.onError(
                    e.getMessage()
            );
        });
    }

    public interface OnItemsLoaded {

        void onLoaded(
                List<FirebaseOrderItemModel> items
        );

        void onError(String error);
    }

    public void loadOrderItems(

            String orderId,

            OnItemsLoaded callback
    ) {

        db.collection("orders")

                .document(orderId)

                .collection("items")

                .get()

                .addOnSuccessListener(query -> {

                    List<FirebaseOrderItemModel>
                            itemList =
                            new ArrayList<>();

                    for (DocumentSnapshot doc
                            : query) {

                        FirebaseOrderItemModel item =

                                doc.toObject(
                                        FirebaseOrderItemModel.class
                                );

                        if (item != null) {

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

            double refundedRent,

            double refundedDeposit,

            ActionCallback callback
    ){

        double totalRefund =
                refundedRent
                        + refundedDeposit;

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

                    for (String itemNo : itemNos) {

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

                        if (item == null) {

                            throw new RuntimeException(

                                    "Item not found : " + itemNo
                            );
                        }

                        // =====================
                        // VALIDATION
                        // =====================

                        if (!item.status.equalsIgnoreCase(
                                "Booked"
                        )) {

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

                    double remainingRentRefund =
                            refundedRent;

                    double remainingDepositRefund =
                            refundedDeposit;

                    for (int i = 0; i < loadedItems.size(); i++) {

                        FirebaseOrderItemModel item =
                                loadedItems.get(i);

                        double itemRentRefund = Math.min(

                                remainingRentRefund,

                                item.rentPaid
                        );

                        remainingRentRefund -=
                                itemRentRefund;


                        double itemDepositRefund = Math.min(

                                remainingDepositRefund,

                                item.customDeposit
                        );

                        remainingDepositRefund -=
                                itemDepositRefund;


                        double itemTotalRefund =

                                itemRentRefund
                                        + itemDepositRefund;

                        DocumentReference orderItemRef =
                                orderItemRefs.get(i);

                        // =====================
                        // ORDER ITEM
                        // =====================

                        transaction.update(

                                orderItemRef,

                                "status",
                                "Cancelled",

                                "refundedRent",
                                itemRentRefund,

                                "refundedDeposit",
                                itemDepositRefund,

                                "totalRefund",
                                itemTotalRefund
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
                            totalRefund,

                            "updatedAt",
                            System.currentTimeMillis()
                    );

                    if (allCancelled) {

                        transaction.update(

                                orderRef,

                                "status",
                                "Cancelled"
                        );
                    }

                    return null;

                }).addOnSuccessListener(unused -> {

            callback.onSuccess();

            maybeArchiveOrder(orderId);

        }).addOnFailureListener(e -> {

            callback.onError(
                    e.getMessage()
            );
        });
    }

    public interface ArchiveCallback {

        void onSuccess();

        void onError(String error);
    }

    public void archiveOrder(

            String orderId,

            ArchiveCallback callback
    ) {
        DocumentReference orderRef =

                db.collection("orders")
                        .document(orderId);

        orderRef.get()

                .addOnSuccessListener(orderDoc -> {

                    if (!orderDoc.exists()) {

                        callback.onError(
                                "Order not found"
                        );

                        return;
                    }
                    FirebaseOrderModel activeOrder =

                            orderDoc.toObject(
                                    FirebaseOrderModel.class
                            );

                    if (activeOrder == null) {

                        callback.onError(
                                "Invalid order"
                        );

                        return;
                    }
                    orderRef.collection("items")

                            .get()

                            .addOnSuccessListener(itemQuery -> {
                                OrderHistoryModel history =
                                        new OrderHistoryModel();

                                history.orderId =
                                        activeOrder.orderId;

                                history.customerName =
                                        activeOrder.customerName;

                                history.phone =
                                        activeOrder.phone;

                                history.alternatePhone =
                                        activeOrder.alternatePhone;

                                history.totalRent =
                                        activeOrder.totalRent;

                                history.totalDeposit =
                                        activeOrder.totalDeposit;

                                history.totalRentPaid =
                                        activeOrder.totalRentPaid;

                                history.balanceRent =
                                        activeOrder.balanceRent;

                                history.refundAmount =
                                        activeOrder.refundAmount;

                                history.pickupMs =
                                        activeOrder.pickupMs;

                                history.returnMs =
                                        activeOrder.returnMs;

                                history.actualPickupMs =
                                        activeOrder.actualPickupMs;

                                history.actualReturnMs =
                                        activeOrder.actualReturnMs;

                                history.archivedAt =
                                        System.currentTimeMillis();

                                history.status =
                                        activeOrder.status;
                                List<OrderHistoryModel.HistoryItem>
                                        historyItems =
                                        new ArrayList<>();

                                for (DocumentSnapshot itemDoc
                                        : itemQuery) {

                                    FirebaseOrderItemModel activeItem =

                                            itemDoc.toObject(
                                                    FirebaseOrderItemModel.class
                                            );

                                    if (activeItem == null)
                                        continue;

                                    OrderHistoryModel.HistoryItem item =
                                            new OrderHistoryModel.HistoryItem();

                                    item.itemNo =
                                            activeItem.itemNo;

                                    item.itemName =
                                            activeItem.itemName;

                                    item.status =
                                            activeItem.status;

                                    item.customRent =
                                            activeItem.customRent;

                                    item.customDeposit =
                                            activeItem.customDeposit;
                                    item.refundedRent =
                                            activeItem.refundedRent;

                                    item.refundedDeposit =
                                            activeItem.refundedDeposit;

                                    item.totalRefund =
                                            activeItem.totalRefund;

                                    item.rentPaid =
                                            activeItem.rentPaid;

                                    item.pickupMs =
                                            activeItem.pickupMs;

                                    item.returnMs =
                                            activeItem.returnMs;

                                    item.washMs =
                                            activeItem.washMs;

                                    historyItems.add(item);
                                }

                                history.items = historyItems;
                                db.collection("order_history").document(orderId).set(history)
                                        .addOnSuccessListener(unused -> {
                                            WriteBatch batch = db.batch();
                                            for (DocumentSnapshot itemDoc : itemQuery) {
                                                batch.delete(itemDoc.getReference());
                                            }
                                            batch.delete(orderRef);

                                            // 4. Execute Deletion Batch
                                            batch.commit().addOnSuccessListener(unused2 -> {
                                                callback.onSuccess();
                                            }).addOnFailureListener(e -> {
                                                callback.onError(e.getMessage());
                                            });

                                        }).addOnFailureListener(e -> {
                                            callback.onError(e.getMessage());
                                        });

                            }).addOnFailureListener(e -> {
                                callback.onError(e.getMessage());
                            });

                }).addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void maybeArchiveOrder(

            String orderId
    ){    db.collection("orders")

            .document(orderId)

            .collection("items")

            .get()

            .addOnSuccessListener(query -> {        boolean shouldArchive = true;

                for(DocumentSnapshot doc
                        : query){

                    FirebaseOrderItemModel item =

                            doc.toObject(
                                    FirebaseOrderItemModel.class
                            );

                    if(item == null)
                        continue;

                    String status = item.status;

                    boolean terminal =

                            status.equalsIgnoreCase(
                                    "Returned"
                            )

                                    ||

                                    status.equalsIgnoreCase(
                                            "Cancelled"
                                    );

                    if(!terminal){

                        shouldArchive = false;

                        break;
                    }
                }        if(shouldArchive){

                    archiveOrder(

                            orderId,

                            new ArchiveCallback() {

                                @Override
                                public void onSuccess() {

                                    android.util.Log.d(

                                            "ARCHIVE",

                                            orderId
                                                    + " archived"
                                    );
                                }

                                @Override
                                public void onError(
                                        String error
                                ) {

                                    android.util.Log.e(

                                            "ARCHIVE",

                                            error
                                    );
                                }
                            }
                    );
                }

            });
    }
}