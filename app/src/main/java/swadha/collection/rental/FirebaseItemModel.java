package swadha.collection.rental;

public class FirebaseItemModel {

    public String itemNo;

    public String itemName;

    public double rent;

    public double deposit;

    public boolean requiresWash;

    public boolean isLocked;

    public String currentStatus;

    public String currentOrderId;

    public long nextAvailableMs;

    public long createdAt;

    public long updatedAt;

    public FirebaseItemModel() {

        // Required empty constructor
    }
}