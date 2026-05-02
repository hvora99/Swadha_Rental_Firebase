package swadha.collection.rental;

public class HistoryModel {

    public long timestamp;
    public String itemNo;
    public String name;
    public String phone;
    public String pickupDateTime;
    public String returnDateTime;

    public double totalRent;
    public double deposit;
    public double rentPaid;
    public double balance;

    public String status;
    public String actualPickup;
    public String actualReceive;
}