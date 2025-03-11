package com.example.tinkdrao.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Drink;

import java.util.List;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {
    private List<Drink> drinkList;
    private Context context;

    public DrinkAdapter(Context context, List<Drink> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_drink, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink drink = drinkList.get(position);

        holder.drinkName.setText(drink.getName());
        holder.drinkDiscountedPrice.setText(String.format("$%.2f", drink.getDiscountedPrice()));

        // Hiển thị giá gốc nếu có giảm giá (giá gốc > giá đã giảm)
        if (drink.getOriginalPrice() > drink.getDiscountedPrice()) {
            holder.drinkOriginalPrice.setText(String.format("$%.2f", drink.getOriginalPrice()));
            holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.drinkOriginalPrice.setVisibility(View.GONE);
        }

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(drink.getImageUrl())
                .into(holder.drinkImage);
    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        ImageView drinkImage;
        TextView drinkName;
        TextView drinkOriginalPrice;
        TextView drinkDiscountedPrice;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.drinkImage);
            drinkName = itemView.findViewById(R.id.drinkName);

            drinkOriginalPrice = itemView.findViewById(R.id.drinkOriginalPrice);
            drinkOriginalPrice.setPaintFlags(drinkOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            drinkDiscountedPrice = itemView.findViewById(R.id.drinkDiscountedPrice);
        }
    }
}
