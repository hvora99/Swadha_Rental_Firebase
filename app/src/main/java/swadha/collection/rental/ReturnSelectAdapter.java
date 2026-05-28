package swadha.collection.rental;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ReturnSelectAdapter
        extends ArrayAdapter<RentalBooking.ItemStatus> {

    public ReturnSelectAdapter(
            Context context,
            ArrayList<RentalBooking.ItemStatus> items
    ) {

        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {

        if(convertView == null){

            convertView = LayoutInflater.from(
                    getContext()
            ).inflate(
                    R.layout.row_return_select_item,
                    parent,
                    false
            );
        }

        RentalBooking.ItemStatus item =
                getItem(position);

        TextView tvTitle =
                convertView.findViewById(
                        R.id.tvItemTitle
                );

        TextView tvSub =
                convertView.findViewById(
                        R.id.tvItemSub
                );

        View indicator =
                convertView.findViewById(
                        R.id.viewIndicator
                );

        boolean checked =

                ((ListView) parent)
                        .isItemChecked(position);

        if(checked){

            indicator.setBackgroundTintList(

                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#2E7D32")
                    )
            );

            convertView.setBackgroundColor(
                    android.graphics.Color.parseColor("#F4F8F4")
            );

        }else{

            indicator.setBackgroundTintList(

                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#D0D0D0")
                    )
            );

            convertView.setBackgroundColor(
                    android.graphics.Color.TRANSPARENT
            );
        }


        tvTitle.setText(

                item.getItemNo()

                        + " - "

                        + item.getItemName()
        );

        tvSub.setText(

                "Rent ₹"

                        + (int)item.getRent()

                        + " • "

                        + item.getStatus()
        );


        return convertView;
    }
}