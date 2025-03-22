package com.example.tinkdrao.adapter;

import android.content.Context;
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
import com.example.tinkdrao.model.Drink;

import java.text.DecimalFormat;
import java.util.List;

public class OrderAdapter3 extends RecyclerView.Adapter<OrderAdapter3.OrderViewHolder3> {
    private List<Cart> cartList;
    private DecimalFormat decimalFormat;

    public OrderAdapter3(Context context, List<Cart> cartList) {
        this.cartList = cartList;
        this.decimalFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public OrderViewHolder3 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order3, parent, false);
        return new OrderViewHolder3(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder3 holder, int position) {
        Cart cart = cartList.get(position);

        holder.drinkName.setText(cart.getName());

        holder.drinkPriceO.setText(decimalFormat.format((int)cart.getPrice())+"₫");

        holder.drinkQuantityO.setText("x" + cart.getQuantity());

        holder.drinkTypeO.setText(cart.getDrinkType());

        Glide.with(holder.itemView.getContext())
                .load(cart.getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(holder.drinkImage);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class OrderViewHolder3 extends RecyclerView.ViewHolder {
        ImageView drinkImage;
        TextView drinkName, drinkPriceO, drinkTypeO, drinkQuantityO;

        public OrderViewHolder3(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.drinkImageO);
            drinkName = itemView.findViewById(R.id.drinkNameO);
            drinkPriceO = itemView.findViewById(R.id.drinkPriceO);
            drinkTypeO = itemView.findViewById(R.id.drinkTypeO);
            drinkQuantityO = itemView.findViewById(R.id.drinkQuantityO);
        }
    }
}