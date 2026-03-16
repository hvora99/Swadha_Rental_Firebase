package swadha.collection.rental;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RentalBooking {

    private String timestamp;
    private String itemName;
    private String name;
    private String phone;
    private long washMs;
    private long actualPickupMs;
     private double refundAmount;

    private double totalRent;
    private double deposit;
    private double rentPaid;
    private String status;
    private double balance;
    private long pickupMs;
    private long returnMs;
    private String orderId;
    private List<ItemStatus> items = new ArrayList<>();

    private double rent;

    public RentalBooking(String timestamp,
                         String orderId,
                         String name,
                         String phone,
                         long pickupMs,
                         long returnMs,
                         long washMs,
                         long actualPickupMs,
                         double totalRent,
                         double deposit,
                         double rentPaid,
                         double balance,
                         String status) {

        this.timestamp = timestamp;
        this.orderId = orderId;
        this.name = name;
        this.phone = phone;

        this.pickupMs = pickupMs;
        this.returnMs = returnMs;
        this.washMs = washMs;
        this.actualPickupMs = actualPickupMs;

        this.totalRent = totalRent;
        this.deposit = deposit;
        this.rentPaid = rentPaid;
        this.balance = balance;

        this.status = status;
    }



            public String getItemsString(){

                List<String> codes = new ArrayList<>();

                for(ItemStatus item : items){
                    codes.add(item.getItemNo());
                }

                return TextUtils.join(", ", codes);
            }

            public String getOrderId(){
                return orderId;
            }

        public long getPickupMs() {
            return pickupMs;
        }

        public long getReturnMs() {
            return returnMs;
        }

    public double getNetIncome() {
        return (deposit + rentPaid) - refundAmount;
    }
    public double getBalance() { return balance; }
    public RentalBooking() {}

    public double getSettlementAmount() {
        return rentPaid - totalRent;
    }

    public double getRentDue() {
        return totalRent - rentPaid;
    }

    public List<String> getItemCodes(){

        List<String> codes = new ArrayList<>();

        for(ItemStatus item : items){
            codes.add(item.getItemNo());
        }

        return codes;
    }


    // --- GETTERS ---


    public String getTimestamp() { return timestamp; }

    public String getName() { return name; }

    public String getPhone() { return phone; }

    public String getFirstItem(){
        if(items.size() > 0){
            return items.get(0).getItemNo();
        }
        return "";
    }

    public List<ItemStatus> getItems(){
        return items;
    }

    public void addItem(String itemNo,
                        String itemName,
                        String status,
                        double rent,
                        double deposit,
                        double rentPaid){

        items.add(new ItemStatus(
                itemNo,
                itemName,
                status,
                rent,
                deposit,
                rentPaid
        ));
    }

    public long getWashMs() { return washMs; }

    public long getActualPickupMs() { return actualPickupMs; }

    public double getTotalRent() { return totalRent; }

    public double getDeposit() { return deposit; }

    public double getRentPaid() { return rentPaid; }


    public String getStatus() { return status; }

    public static class ItemStatus implements Serializable {

        private String itemNo;
        private String status;
        private double rent;
        private double deposit;
        String itemName;
        private double rentPaid;

        public ItemStatus(
                String itemNo,
                String itemName,
                String status,
                double rent,
                double deposit,
                double rentPaid){

            this.itemNo = itemNo;
            this.itemName = itemName;
            this.status = status;
            this.rent = rent;
            this.deposit = deposit;
            this.rentPaid = rentPaid;
        }

        public double getRentPaid(){
            return rentPaid;
        }

        public double getBalance(){
            return rent - rentPaid;
        }

        public String getItemNo(){
            return itemNo;
        }

        public String getStatus(){
            return status;
        }

        public double getRent(){
            return rent;
        }

        public double getDeposit(){
            return deposit;
        }

        public String getItemName(){
            return itemName;
        }
    }

}