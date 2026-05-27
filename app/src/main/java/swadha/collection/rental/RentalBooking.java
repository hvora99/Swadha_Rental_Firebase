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
    private String alternatePhone;;
    private long washMs;
    private long actualPickupMs;
     private double refundAmount;
    private long createdAt;
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
                         String alternatePhone,
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
        this.alternatePhone = alternatePhone;
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


    public long getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(
            long createdAt
    ){

        this.createdAt = createdAt;
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

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public String getFirstItem(){
        if(items.size() > 0){
            return items.get(0).getItemNo();
        }
        return "";
    }

    public List<ItemStatus> getItems(){
        return items;
    }

    public void addItem(
            String itemNo,
            String itemName,
            String status,
            double rent,
            double deposit,
            double rentPaid,
            long pickupMs,
            long returnMs,
            long washMs
    ){

        items.add(new ItemStatus(

                itemNo,
                itemName,
                status,
                rent,
                deposit,
                rentPaid,
                pickupMs,
                returnMs,
                washMs,
                0,
                0
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
        private long pickupMs;
        private long returnMs;
        private long washMs;
        private double customRent;

        private double customDeposit;

        private double refundedDeposit;

        private double refundedRent;

        private double totalRefund;

        public ItemStatus(

                String itemNo,

                String itemName,

                String status,

                double customRent,

                double customDeposit,

                double rentPaid,

                long pickupMs,

                long returnMs,

                long washMs,

                double refundedRent,

                double refundedDeposit
        ){

            this.itemNo = itemNo;

            this.itemName = itemName;

            this.status = status;

            this.customRent = customRent;

            this.customDeposit = customDeposit;

            this.rentPaid = rentPaid;

            this.pickupMs = pickupMs;

            this.returnMs = returnMs;

            this.washMs = washMs;

            this.refundedRent = refundedRent;

            this.refundedDeposit = refundedDeposit;

            this.totalRefund =
                    refundedRent
                            + refundedDeposit;
        }

        public double getCustomRent() {
            return customRent;
        }

        public double getCustomDeposit() {
            return customDeposit;
        }

        public double getRefundedDeposit() {
            return refundedDeposit;
        }

        public double getRefundedRent() {
            return refundedRent;
        }

        public double getTotalRefund() {
            return totalRefund;
        }

        public double getRentPaid(){
            return rentPaid;
        }

        public double getBalance(){
            return customRent - rentPaid;
        }
        public String getItemNo(){
            return itemNo;
        }

        public String getStatus(){
            return status;
        }
        public double getRent(){
            return customRent;
        }

        public double getDeposit(){
            return customDeposit;
        }

        public String getItemName(){
            return itemName;
        }
        public long getPickupMs(){
            return pickupMs;
        }

        public long getReturnMs(){
            return returnMs;
        }

        public long getWashMs(){
            return washMs;
        }
    }

}