package swadha.collection.rental;

public class ItemModel {
    private String itemNo;
    private String itemName;
    private double rent;
    private double deposit;
    private boolean requiresWash;
    private boolean isLocked;
    private long nextAvailableMs;
    private boolean booked;
    private String status;
    private boolean isBooked;
    private boolean isWashing;
    private boolean isAvailable;
    // Constructor: This is how we build the item object
    public ItemModel(String itemNo, String itemName, double rent, double deposit,
                     boolean requiresWash, boolean isLocked, long nextAvailableMs,String status) {

        this.itemNo = itemNo;
        this.itemName = itemName;
        this.rent = rent;
        this.deposit = deposit;
        this.requiresWash = requiresWash;
        this.isLocked = isLocked;
        this.nextAvailableMs = nextAvailableMs;
        this.status = status;
    }
    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
    public long getNextAvailableMs() {
        return nextAvailableMs;
    }
    public boolean isRequiresWash() { return requiresWash; }
    public boolean isLocked() { return isLocked; }
    public String getStatus() {
        return status;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    @Override
    public String toString() {
        return itemNo + " - " + itemName;
    }
    // Getters: These allow the Adapter to read the data
    public String getItemNo() { return itemNo; }
    public String getItemName() { return itemName; }
    public double getRent() { return rent; }
    public double getDeposit() { return deposit; }
    public boolean isWashing() {
        return "washing".equals(status);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemModel)) return false;
        ItemModel other = (ItemModel) obj;
        return itemNo.equals(other.itemNo);
    }

    public boolean isAvailable() {
        return nextAvailableMs == 0 || nextAvailableMs < System.currentTimeMillis();
    }
}