package swadha.collection.rental;

import java.util.List;

public class OrderHistoryModel {

    public long timestamp;

    public String orderId;

    public String name;
    public String phone;

    public String pickupDateTime;
    public String returnDateTime;

    public String actualPickup;
    public String actualReturn;

    public double totalRent;
    public double deposit;
    public double rentPaid;
    public double balance;

    public String status;

    // ✅ ITEM LIST
    public List<HistoryItem> items;

    // =========================
    // INNER ITEM MODEL
    // =========================

    public static class HistoryItem {

        public String itemNo;

        public String pickupScheduled;
        public String returnScheduled;

        public String actualPickup;
        public String actualReturn;

        public String status;

        public double rent;
        public double deposit;
    }
}