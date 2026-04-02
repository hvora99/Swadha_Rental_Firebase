package swadha.collection.rental;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryBookingAdapter
        extends RecyclerView.Adapter<HistoryBookingAdapter.ViewHolder> {

    private List<HistoryBooking> list;

    public HistoryBookingAdapter(List<HistoryBooking> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        HistoryBooking booking = list.get(position);

        holder.tvItemNo.setText(booking.getItemNo());
        holder.tvCustomer.setText(booking.getName());
        holder.tvStatus.setText(booking.getStatus());

        double balance = booking.getBalance();

        if (balance < 0) {
            holder.tvBalance.setText("Refund ₹ " + Math.abs(balance));
            holder.tvBalance.setTextColor(Color.parseColor("#2E7D32"));
        } else if (balance > 0) {
            holder.tvBalance.setText("Customer Paid Extra ₹ " + balance);
            holder.tvBalance.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tvBalance.setText("Settled");
            holder.tvBalance.setTextColor(Color.GRAY);
        }

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(),
                    HistoryDetailActivity.class);

            intent.putExtra("orderId", booking.getOrderId());
            intent.putExtra("name", booking.getName());
            intent.putExtra("phone", booking.getPhone());
            intent.putExtra("pickupDateTime", booking.getPickupDateTime());
            intent.putExtra("returnDateTime", booking.getReturnDateTime());

            intent.putExtra("totalRent", booking.getTotalRent());
            intent.putExtra("deposit", booking.getDeposit());
            intent.putExtra("rentPaid", booking.getRentPaid());
            intent.putExtra("balance", booking.getBalance());
            intent.putExtra("itemsList",
                    new ArrayList<>(booking.getItemsList())); // 👈 YOU NEED THIS FIELD

            intent.putExtra("status", booking.getStatus());
            intent.putExtra("actualPickup", booking.getActualPickup());
            intent.putExtra("actualReceive", booking.getActualReceive());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvItemNo, tvCustomer, tvStatus, tvBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemNo = itemView.findViewById(R.id.tvItemNo);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}