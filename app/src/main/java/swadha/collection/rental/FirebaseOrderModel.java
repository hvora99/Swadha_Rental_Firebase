package swadha.collection.rental;

public class FirebaseOrderModel {

    public String orderId;

    public String customerName;

    public String phone;

    public double totalRent;

    public double totalDeposit;

    public double totalRentPaid;

    public double balanceRent;

    public long pickupMs;

    public long returnMs;

    public long washBlockMs;

    public String status;

    public long createdAt;

    public long updatedAt;

    public FirebaseOrderModel() {
    }
}
