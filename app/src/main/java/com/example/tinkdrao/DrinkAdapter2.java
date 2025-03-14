package com.example.tinkdrao;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.model.Drink;

import java.util.ArrayList;
import java.util.List;

public class DrinkAdapter2 extends RecyclerView.Adapter<DrinkAdapter2.DrinkViewHolder> {
    private List<Drink> drinks = new ArrayList<>();
    private List<Drink> originalDrinks = new ArrayList<>(); // Lưu danh sách gốc

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drink2, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink drink = drinks.get(position);
        holder.tvName.setText(drink.getName());
        //holder.tvPrice.setText(String.format("%.2f", drink.getPrice()));
        // Tính toán giá sau khi giảm
        double finalPrice = drink.getPrice() * (1 - (drink.getDiscount() / 100));

        // Hiển thị giá gốc và giá sau khi giảm (nếu có giảm giá)
        if (drink.getDiscount() > 0) {
            holder.tvPrice.setText(String.format("Giá: %.2f VNĐ", finalPrice));
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(String.format("-%.0f%%", drink.getDiscount())); // Hiển thị giảm giá
            //holder.itemView.setBackgroundResource(R.drawable.discount_background); // Làm nổi bật
        } else {
            holder.tvPrice.setText(String.format("Giá: %.2f VNĐ", drink.getPrice()));
            holder.tvDiscount.setVisibility(View.GONE);
            //holder.itemView.setBackgroundResource(R.drawable.normal_background);
        }
        Glide.with(holder.itemView.getContext())
                .load(drink.getImageUrl())
                .into(holder.ivDrink);

        // Kiểm tra discount
        if (drink.getDiscount() > 0) {
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(String.format("-%.0f%%", drink.getDiscount())); // Hiển thị giảm giá
            //holder.itemView.setBackgroundResource(R.drawable.discount_background); // Làm nổi bật
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
            //holder.itemView.setBackgroundResource(R.drawable.normal_background);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DrinkDetailActivity2.class);
            intent.putExtra("drink", drink);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return drinks.size();
    }

    public void setDrinks(List<Drink> drinks) {
        this.drinks = new ArrayList<>(drinks);
        this.originalDrinks = new ArrayList<>(drinks); // Lưu danh sách gốc để tìm kiếm
        notifyDataSetChanged();
    }

    // Lọc danh sách theo từ khóa
    public void filterDrinks(String query) {
        List<Drink> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(originalDrinks);
        } else {
            for (Drink drink : originalDrinks) {
                if (drink.getName().toLowerCase().contains(query.toLowerCase()) ||
                        drink.getDrinkType().toLowerCase().contains(query.toLowerCase()) ||
                        drink.getUnit().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(drink);
                }
            }
        }
        drinks.clear();
        drinks.addAll(filteredList);
        notifyDataSetChanged();
    }

    static class DrinkViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice,tvDiscount;
        ImageView ivDrink;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_drink_name);
            tvPrice = itemView.findViewById(R.id.tv_drink_price);
            tvDiscount = itemView.findViewById(R.id.tv_drink_discount);
            ivDrink = itemView.findViewById(R.id.iv_drink_image);
        }
    }
}
