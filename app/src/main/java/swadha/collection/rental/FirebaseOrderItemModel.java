package swadha.collection.rental;

public class FirebaseOrderItemModel {

    public String itemNo;

    public String itemName;
    public double refundedDeposit;

    public double refundedRent;

    public double totalRefund;

    public double originalRent;

    public double originalDeposit;

    public double customRent;

    public double customDeposit;

    public double rentPaid;

    public double balanceRent;

    public long pickupMs;

    public long returnMs;

    public long washMs;

    public boolean requiresWash;

    public String status;

    public long createdAt;

    public FirebaseOrderItemModel() {
    }
}