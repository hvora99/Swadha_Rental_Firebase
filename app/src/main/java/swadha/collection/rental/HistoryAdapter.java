package swadha.collection.rental;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private List<OrderHistoryModel> list;
    private Context context;

    public HistoryAdapter(List<OrderHistoryModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_booking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(

            @NonNull VH h,

            int position
    ) {

        OrderHistoryModel m =
                list.get(position);

        h.tvItemNo.setText(m.orderId);

        h.tvCustomer.setText(
                m.customerName
        );

        h.tvStatus.setText(
                m.status
        );

        int count = 0;

        if(m.items != null){
            count = m.items.size();
        }

        h.tvBalance.setText(

                count
                        + " items | ₹"
                        + m.totalRent
        );

        h.itemView.setOnClickListener(v -> {

            Intent i = new Intent(

                    context,

                    HistoryDetailActivity.class
            );

            i.putExtra(
                    "data",
                    new Gson().toJson(m)
            );

            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvItemNo, tvCustomer, tvStatus, tvBalance;

        public VH(@NonNull View itemView) {
            super(itemView);

            tvItemNo = itemView.findViewById(R.id.tvItemNo);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }


}