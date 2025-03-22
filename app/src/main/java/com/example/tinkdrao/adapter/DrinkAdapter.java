package com.example.tinkdrao.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.DrinkDetailActivity;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Drink;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {
    private List<Drink> drinkList;
    private Context context;
    private DecimalFormat decimalFormat;
    private DatabaseReference orderReference;

    public DrinkAdapter(Context context, List<Drink> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
    }


    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink drink = drinkList.get(position);

        holder.drinkName.setText(drink.getName());
        if (drink.getDiscount() > 0) {
            double discountedPrice = drink.getPrice() * (100 - drink.getDiscount()) / 100;
            holder.drinkDiscountedPercent.setText("-" + decimalFormat.format(drink.getDiscount()) + "%");
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
            holder.drinkOriginalPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
            holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
            holder.drinkDiscountedPercent.setVisibility(View.VISIBLE);
        } else {
            holder.drinkOriginalPrice.setVisibility(View.GONE);
            holder.drinkDiscountedPercent.setVisibility(View.GONE);
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
        }

        if (drink.getQuantity() == 0) {
            holder.soldOutText.setVisibility(View.VISIBLE);
        } else {
            holder.soldOutText.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(drink.getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(holder.drinkImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DrinkDetailActivity.class);
            intent.putExtra("id", drink.getId()); // Chuyển Long sang String khi truyền qua Intent
            context.startActivity(intent);
        });
    }

    // Thêm phương thức sắp xếp theo purchaseCount
    public void sortByPurchaseCount() {
        // Sắp xếp drinkList theo purchaseCount giảm dần
        Collections.sort(drinkList, new Comparator<Drink>() {
            @Override
            public int compare(Drink d1, Drink d2) {
                // So sánh purchaseCount, sắp xếp giảm dần nên đảo thứ tự d2 - d1
                return Integer.compare(d2.getPurchaseCount(), d1.getPurchaseCount());
            }
        });
        // Thông báo adapter cập nhật lại RecyclerView
        notifyDataSetChanged();
    }

    public void updateList(List<Drink> newList) {
        drinkList.clear();
        drinkList.addAll(newList);
        sortByPurchaseCount(); // Tự động sắp xếp sau khi cập nhật danh sách
    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        public ImageView drinkImage;
        public TextView drinkName;
        public TextView drinkOriginalPrice;
        public TextView drinkDiscountedPrice;
        public TextView drinkDiscountedPercent;
        private TextView soldOutText;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.drinkImage);
            drinkName = itemView.findViewById(R.id.drinkName);
            drinkOriginalPrice = itemView.findViewById(R.id.drinkOriginalPrice);
            drinkOriginalPrice.setPaintFlags(drinkOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            drinkDiscountedPrice = itemView.findViewById(R.id.drinkDiscountedPrice);
            drinkDiscountedPercent = itemView.findViewById(R.id.drinkDiscountPercent);
            soldOutText = itemView.findViewById(R.id.soldOutText);
        }
    }
}