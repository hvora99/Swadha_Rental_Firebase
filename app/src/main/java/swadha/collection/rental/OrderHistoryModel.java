package swadha.collection.rental;

import java.util.List;

public class OrderHistoryModel {

    public String orderId;

    public String customerName;

    public String phone;
    public String alternatePhone;

    public double totalRent;

    public double totalDeposit;

    public double totalRentPaid;

    public double balanceRent;

    public long pickupMs;

    public long returnMs;

    public long archivedAt;

    public long createdAt;

    public String status;

    public double refundAmount;

    public long actualPickupMs;

    public long actualReturnMs;

    // =========================
    // ITEM SNAPSHOT
    // =========================

    public List<HistoryItem> items;

    public static class HistoryItem {

        public String itemNo;

        public String itemName;

        public String status;

        public double customRent;

        public double customDeposit;

        public double rentPaid;

        public long pickupMs;

        public long returnMs;

        public long washMs;

        public double refundedRent;

        public double refundedDeposit;

        public double totalRefund;

        public HistoryItem(){}
    }

    public OrderHistoryModel(){}
}