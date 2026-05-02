package swadha.collection.rental;

import java.util.List;

public class OrderHistoryModel {

    public long timestamp;
    public String orderId;
    public String name;
    public String phone;

    public String pickupDateTime;
    public String returnDateTime;

    public double totalRent;
    public double deposit;
    public double rentPaid;
    public double balance;

    public String status;

    public List<String> items;
}