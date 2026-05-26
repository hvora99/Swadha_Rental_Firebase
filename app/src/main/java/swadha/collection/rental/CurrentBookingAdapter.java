    package swadha.collection.rental;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.recyclerview.widget.RecyclerView;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import java.util.List;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import java.util.List;

    public class CurrentBookingAdapter
            extends RecyclerView.Adapter<CurrentBookingAdapter.ViewHolder> {

        private List<CurrentBookingModel> bookingList;

        public CurrentBookingAdapter(List<CurrentBookingModel> bookingList) {
            this.bookingList = bookingList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_current_booking, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            CurrentBookingModel model = bookingList.get(position);

            holder.tvName.setText(model.getName());
            holder.tvPhone.setText(model.getPhone());
            holder.tvPickup.setText("Pickup: " + model.getPickup());
            holder.tvReturn.setText("Return: " + model.getReturn());
            holder.tvBalance.setText( model.getBalance());
            holder.tvItemCircle.setText(model.getItemNo());
        }

        @Override
        public int getItemCount() {
            return bookingList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvPhone, tvPickup, tvReturn, tvBalance,tvItemCircle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvBookingName);
                tvPhone = itemView.findViewById(R.id.tvBookingPhone);
                tvPickup = itemView.findViewById(R.id.tvBookingPickup);
                tvReturn = itemView.findViewById(R.id.tvBookingReturn);
                tvBalance = itemView.findViewById(R.id.tvBookingBalance);
                tvItemCircle = itemView.findViewById(R.id.tvItemCircle);
            }
        }
    }