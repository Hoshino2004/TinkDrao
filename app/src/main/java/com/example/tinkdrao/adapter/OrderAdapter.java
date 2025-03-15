package com.example.tinkdrao.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Cart;

import java.text.DecimalFormat;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Cart> orderItems;
    private DecimalFormat decimalFormat;

    public OrderAdapter(List<Cart> orderItems) {
        this.orderItems = orderItems;
        this.decimalFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Cart cart = orderItems.get(position);

        holder.drinkName.setText(cart.getName());
        if (cart.getDiscount() > 0) {
            double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
            holder.drinkOriginalPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
            holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.drinkOriginalPrice.setVisibility(View.GONE);
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
        }

        holder.drinkQuantity.setText("Số lượng: " + cart.getQuantity());

        Glide.with(holder.itemView.getContext())
                .load(cart.getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(holder.drinkImage);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView drinkImage;
        TextView drinkName, drinkOriginalPrice, drinkDiscountedPrice, drinkQuantity;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.drinkImage);
            drinkName = itemView.findViewById(R.id.drinkName);
            drinkOriginalPrice = itemView.findViewById(R.id.drinkOriginalPrice);
            drinkDiscountedPrice = itemView.findViewById(R.id.drinkDiscountedPrice);
            drinkQuantity = itemView.findViewById(R.id.drinkQuantity);
        }
    }
}