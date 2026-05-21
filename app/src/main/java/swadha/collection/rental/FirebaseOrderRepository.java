package swadha.collection.rental;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.List;
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

                        if(firebaseItem.nextAvailableMs

                                > selected.pickupMs){

                            throw new RuntimeException(

                                    selected.item.getItemNo()

                                            + " already booked"
                            );
                        }
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
}