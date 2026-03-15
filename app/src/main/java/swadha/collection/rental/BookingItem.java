package swadha.collection.rental;

    public class BookingItem {

        private String itemNo;
        private long pickupMs;
        private long returnMs;
        private long washMs;
        private boolean requiresWash;

        // ⭐ ADD THESE
        private double rent;
        private double deposit;

        public BookingItem(String itemNo,
                           long pickupMs,
                           long returnMs,
                           long washMs,
                           boolean requiresWash,
                           double rent,
                           double deposit) {

            this.itemNo = itemNo;
            this.pickupMs = pickupMs;
            this.returnMs = returnMs;
            this.washMs = washMs;
            this.requiresWash = requiresWash;

            this.rent = rent;
            this.deposit = deposit;
        }

        public String getItemNo() {
            return itemNo;
        }

        public long getPickupMs() {
            return pickupMs;
        }

        public long getReturnMs() {
            return returnMs;
        }

        public long getWashMs() {
            return washMs;
        }

        public boolean isRequiresWash() {
            return requiresWash;
        }

        // ⭐ NEW METHODS

        public double getRent() {
            return rent;
        }

        public double getDeposit() {
            return deposit;
        }

        // setters

        public void setPickupMs(long pickupMs) {
            this.pickupMs = pickupMs;
        }

        public void setReturnMs(long returnMs) {
            this.returnMs = returnMs;
        }

        public void setWashMs(long washMs) {
            this.washMs = washMs;
        }
    }