package swadha.collection.rental;

import java.util.List;

public class OrderHistory {

    private String orderId;
    private String name;
    private String phone;
    private String date;
    private String status;

    private double totalRent;
    private double rentPaid;
    private double deposit;

    private List<HistoryBooking> items;

    private boolean expanded = false;

    // getters & setters
}