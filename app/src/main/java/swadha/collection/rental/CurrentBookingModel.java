package swadha.collection.rental;


public class CurrentBookingModel {

    private String name;
    private String phone;
    private String pickup;
    private String returnDate;
    private String total;
    private String advance;
    private String balance;
    private String orderId;

    private String status;

    private String itemNo;

    CurrentBookingModel(

            String orderId,
            String itemNo,
            String status,

            String name,
            String phone,

            String pickup,
            String returnDate,

            String total,
            String advance,
            String balance
    ) {

        this.orderId = orderId;
        this.itemNo = itemNo;
        this.status = status;

        this.name = name;
        this.phone = phone;
        this.pickup = pickup;
        this.returnDate = returnDate;
        this.total = total;
        this.advance = advance;
        this.balance = balance;
    }

    public String getItemNo() {
        return itemNo;
    }
    public String getOrderId() {

        return orderId;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPickup() { return pickup; }
    public String getReturn() { return returnDate; }
    public String getTotal() { return total; }
    public String getAdvance() { return advance; }
    public String getBalance() { return balance; }
}