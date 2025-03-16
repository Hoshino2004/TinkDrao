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
    private static final String TAG = "DrinkAdapter";

    public DrinkAdapter(Context context, List<Drink> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        orderReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Order");

        calculateBestSellingDrinks();
    }

    private void calculateBestSellingDrinks() {
        orderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Sử dụng Map với key là Long thay vì String
                Map<Long, Long> salesMap = new HashMap<>();

                if (!snapshot.exists()) {
                    return;
                }

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot invoiceSnapshot : orderSnapshot.getChildren()) {
                        DataSnapshot dataSnapshot = invoiceSnapshot.child("Data");

                        for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                            String drinkIdStr = productSnapshot.getKey();
                            Integer quantity = productSnapshot.child("quantity").getValue(Integer.class);

                            // Chuyển drinkId từ String sang Long
                            Long drinkId;
                            try {
                                drinkId = Long.parseLong(drinkIdStr);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Invalid drink ID format: " + drinkIdStr);
                                continue;
                            }

                            if (quantity != null) {
                                salesMap.put(drinkId, salesMap.getOrDefault(drinkId, 0L) + quantity);
                            }
                        }
                    }
                }

                // Cập nhật totalSold
                for (Drink drink : drinkList) {
                    Long drinkId = drink.getId(); // Lấy ID kiểu Long
                    Long totalSold = salesMap.get(drinkId);
                    drink.setTotalSold(totalSold != null ? totalSold : 0L);
                }

                // Sắp xếp theo totalSold giảm dần
                Collections.sort(drinkList, new Comparator<Drink>() {
                    @Override
                    public int compare(Drink d1, Drink d2) {
                        return Long.compare(d2.getTotalSold(), d1.getTotalSold());
                    }
                });

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
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

    public void updateList(List<Drink> newList) {
        drinkList.clear();
        drinkList.addAll(newList);
        calculateBestSellingDrinks();
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