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

                                    List<RentalBooking> bookingList =
                                            new ArrayList<>();

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

                                                        0,

                                                        order.totalRent,

                                                        order.totalDeposit,

                                                        order.totalRentPaid,

                                                        order.balanceRent,

                                                        order.status
                                                );

                                        bookingList.add(booking);
                                    }

                                    callback.onDataChanged(
                                            bookingList
                                    );
                                });
    }

    public void removeListener(){

        if(dashboardListener != null){

            dashboardListener.remove();

            dashboardListener = null;
        }
    }

}