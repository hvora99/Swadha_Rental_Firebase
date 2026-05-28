package swadha.collection.rental;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDashboardRepository {

    private ListenerRegistration
            dashboardListener;



    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    public interface DashboardCallback {

        void onDataChanged(
                List<RentalBooking> bookings
        );

        void onError(String error);
    }
    public void listenActiveOrders(

            DashboardCallback callback
    ){

        dashboardListener =

                db.collection("orders")

                        .addSnapshotListener(

                                (value, error) -> {

                                    if(error != null){

                                        callback.onError(
                                                error.getMessage()
                                        );

                                        return;
                                    }

                                    if(value == null
                                            || value.isEmpty()){

                                        callback.onDataChanged(
                                                new ArrayList<>()
                                        );

                                        return;
                                    }

                                    List<RentalBooking> bookingList =
                                            new ArrayList<>();

                                    final int totalOrders =
                                            value.size();

                                    final int[] processed =
                                            {0};

                                    for(QueryDocumentSnapshot doc
                                            : value){

                                        FirebaseOrderModel order =

                                                doc.toObject(
                                                        FirebaseOrderModel.class
                                                );

                                        RentalBooking booking =
                                                new RentalBooking(

                                                        "",

                                                        order.orderId,

                                                        order.customerName,

                                                        order.phone,

                                                        order.alternatePhone,

                                                        order.pickupMs,

                                                        order.returnMs,

                                                        order.washBlockMs,

                                                        order.actualPickupMs,

                                                        order.totalRent,

                                                        order.totalDeposit,

                                                        order.totalRentPaid,

                                                        order.balanceRent,

                                                        order.status
                                                );

                                        booking.setCreatedAt(
                                                order.createdAt
                                        );

                                        // ==========================
                                        // LOAD ITEMS SUBCOLLECTION
                                        // ==========================

                                        db.collection("orders")

                                                .document(order.orderId)

                                                .collection("items")

                                                .get()

                                                .addOnSuccessListener(itemSnapshots -> {

                                                    for(QueryDocumentSnapshot itemDoc
                                                            : itemSnapshots){

                                                        FirebaseOrderItemModel item =

                                                                itemDoc.toObject(
                                                                        FirebaseOrderItemModel.class
                                                                );

                                                        booking.addItem(

                                                                item.itemNo,

                                                                item.itemName,

                                                                item.status,

                                                                item.customRent,

                                                                item.customDeposit,

                                                                item.rentPaid,

                                                                item.pickupMs,

                                                                item.returnMs,

                                                                item.washMs
                                                        );
                                                    }

                                                    bookingList.add(
                                                            booking
                                                    );

                                                    processed[0]++;

                                                    if(processed[0] >= totalOrders){

                                                        callback.onDataChanged(
                                                                bookingList
                                                        );
                                                    }

                                                })

                                                .addOnFailureListener(e -> {

                                                    bookingList.add(
                                                            booking
                                                    );

                                                    processed[0]++;

                                                    if(processed[0] >= totalOrders){

                                                        callback.onDataChanged(
                                                                bookingList
                                                        );
                                                    }
                                                });
                                    }
                                });
    }

    public void removeListener(){

        if(dashboardListener != null){

            dashboardListener.remove();

            dashboardListener = null;
        }
    }

}