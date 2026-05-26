package swadha.collection.rental;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FirebaseOrderRepository {

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    public interface OrderCallback {

        void onSuccess();

        void onError(String error);
    }

    public void createBooking(

            FirebaseOrderModel order,

            List<SelectedBookingItemModel> items,

            OrderCallback callback
    ){

        db.runTransaction(
                (Transaction.Function<Void>) transaction -> {

                    // =========================
                    // ORDER DOCUMENT
                    // =========================

                    DocumentReference orderRef =

                            db.collection("orders")

                                    .document(order.orderId);

                    // =========================
                    // VALIDATE ITEMS
                    // =========================

                    for(SelectedBookingItemModel selected
                            : items){

                        DocumentReference itemRef =

                                db.collection("items")

                                        .document(
                                                selected.item.getItemNo()
                                        );

                        FirebaseItemModel firebaseItem =

                                transaction

                                        .get(itemRef)

                                        .toObject(
                                                FirebaseItemModel.class
                                        );

                        if(firebaseItem == null){

                            throw new RuntimeException(

                                    "Item not found : " +

                                            selected.item.getItemNo()
                            );
                        }

                        // =====================
                        // LOCK CHECK
                        // =====================

                        if(firebaseItem.isLocked){

                            throw new RuntimeException(

                                    selected.item.getItemNo()

                                            + " is locked"
                            );
                        }

                        // =====================
                        // AVAILABILITY CHECK
                        // =====================

                    }

                    // =====================
                    // SAVE ORDER
                    // =====================

                    transaction.set(orderRef, order);

                    // =====================
                    // SAVE ORDER ITEMS
                    // =====================

                    for(SelectedBookingItemModel selected
                            : items){

                        DocumentReference orderItemRef =

                                orderRef

                                        .collection("items")

                                        .document(
                                                selected.item.getItemNo()
                                        );

                        FirebaseOrderItemModel orderItem =
                                new FirebaseOrderItemModel();

                        orderItem.itemNo =
                                selected.item.getItemNo();

                        orderItem.itemName =
                                selected.item.getItemName();

                        orderItem.originalRent =
                                selected.item.getRent();

                        orderItem.originalDeposit =
                                selected.item.getDeposit();

                        orderItem.customRent =
                                selected.customRent;

                        orderItem.customDeposit =
                                selected.customDeposit;

                        orderItem.rentPaid =
                                selected.rentPaid;

                        orderItem.balanceRent =
                                selected.customRent
                                        - selected.rentPaid;

                        orderItem.pickupMs =
                                selected.pickupMs;

                        orderItem.returnMs =
                                selected.returnMs;

                        orderItem.washMs =
                                selected.washMs;

                        orderItem.requiresWash =
                                selected.item.isRequiresWash();

                        orderItem.status =
                                Constants.STATUS_BOOKED;

                        orderItem.createdAt =
                                System.currentTimeMillis();

                        transaction.set(
                                orderItemRef,
                                orderItem
                        );
                    }

                    // =====================
                    // UPDATE INVENTORY
                    // =====================

                    for(SelectedBookingItemModel selected
                            : items){

                        DocumentReference inventoryRef =

                                db.collection("items")

                                        .document(
                                                selected.item.getItemNo()
                                        );

                        transaction.update(

                                inventoryRef,

                                "currentStatus",
                                Constants.STATUS_BOOKED,

                                "currentOrderId",
                                order.orderId,

                                "nextAvailableMs",
                                selected.washMs,

                                "updatedAt",
                                System.currentTimeMillis()
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


    public void validateFinalAvailability(

            List<SelectedBookingItemModel> items,

            OrderCallback callback
    ){

        long minPickup = Long.MAX_VALUE;

        long maxReturn = 0;

        // =====================
        // FIND DATE RANGE
        // =====================

        for(SelectedBookingItemModel item
                : items){

            if(item.pickupMs < minPickup){

                minPickup = item.pickupMs;
            }

            if(item.returnMs > maxReturn){

                maxReturn = item.returnMs;
            }
        }

        db.collection("orders")

                .whereGreaterThan(
                        "returnMs",
                        minPickup
                )

                .whereLessThan(
                        "pickupMs",
                        maxReturn
                )

                .get()

                .addOnSuccessListener(orderQuery -> {

                    for(DocumentSnapshot orderDoc
                            : orderQuery.getDocuments()){

                        for(SelectedBookingItemModel selected
                                : items){

                            orderDoc

                                    .getReference()

                                    .collection("items")

                                    .document(
                                            selected.item.getItemNo()
                                    )

                                    .get()

                                    .addOnSuccessListener(itemDoc -> {

                                        if(!itemDoc.exists())
                                            return;

                                        FirebaseOrderItemModel bookedItem =

                                                itemDoc.toObject(
                                                        FirebaseOrderItemModel.class
                                                );

                                        if(bookedItem == null)
                                            return;

                                        // ignore completed
                                        if("Returned".equalsIgnoreCase(
                                                bookedItem.status
                                        )
                                                ||
                                                "Cancelled".equalsIgnoreCase(
                                                        bookedItem.status
                                                )){

                                            return;
                                        }

                                        boolean overlaps =

                                                selected.pickupMs
                                                        < bookedItem.washMs

                                                        &&

                                                        selected.returnMs
                                                                > bookedItem.pickupMs;

                                        if(overlaps){

                                            callback.onError(

                                                    selected.item.getItemNo()

                                                            + " was just booked"
                                            );
                                        }
                                    });
                        }
                    }

                    callback.onSuccess();

                })

                .addOnFailureListener(e -> {

                    callback.onError(
                            e.getMessage()
                    );
                });
    }

    public interface OrderIdCallback{

        void onGenerated(
                String orderId
        );

        void onError(
                String error
        );
    }
    public void generateOrderId(

            OrderIdCallback callback
    ){

        Calendar calendar =
                Calendar.getInstance();

        String year =

                String.valueOf(
                        calendar.get(Calendar.YEAR)
                ).substring(2);

        String fieldName =
                "lastOrderNo" + year;

        DocumentReference counterRef =

                db.collection("config")
                        .document("orderCounter");

        db.runTransaction(transaction -> {

            Long lastNo =
                    transaction
                            .get(counterRef)
                            .getLong(fieldName);

            if(lastNo == null){
                lastNo = 0L;
            }

            long nextNo =
                    lastNo + 1;

            transaction.update(
                    counterRef,
                    fieldName,
                    nextNo
            );

            return "SVD"
                    + year
                    + String.format(
                    Locale.getDefault(),
                    "%04d",
                    nextNo
            );

        }).addOnSuccessListener(orderId -> {

            callback.onGenerated(
                    orderId
            );

        }).addOnFailureListener(e -> {

            callback.onError(
                    e.getMessage()
            );
        });
    }
}