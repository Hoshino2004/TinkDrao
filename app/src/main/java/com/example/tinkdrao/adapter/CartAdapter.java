package com.example.tinkdrao.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.DrinkDetailActivity;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Cart;
import com.example.tinkdrao.model.Drink;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Cart> cartList;
    private Context context;
    private DecimalFormat decimalFormat; // Định dạng giá tiền

    public CartAdapter(Context context, List<Cart> cartList) {
        this.context = context;
        this.cartList = cartList;
        // Khởi tạo DecimalFormat với dấu chấm ngăn cách hàng nghìn
        decimalFormat = new DecimalFormat("#,###"); // Dấu chấm: 12.000
        decimalFormat.setDecimalSeparatorAlwaysShown(false); // Không hiển thị phần thập phân
        // Định dạng ngày giờ từ chuỗi createdAt
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cart = cartList.get(position);

        holder.drinkName.setText(cart.getName());
        // Hiển thị giá gốc nếu có giảm giá
        if (cart.getDiscount() > 0) {
            double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
            holder.drinkOriginalPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
            holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.drinkOriginalPrice.setVisibility(View.GONE);
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
        }

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(cart.getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(holder.drinkImage);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public ImageView drinkImage, btnDeleteFromCart;
        public TextView drinkName, drinkQuantity;
        public TextView drinkOriginalPrice;
        public TextView drinkDiscountedPrice;
        public EditText edtQC;
        public Button btnDQC, btnIQC;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.drinkImage);
            drinkName = itemView.findViewById(R.id.drinkName);
            btnDeleteFromCart = itemView.findViewById(R.id.btnDeleteFormCart);

            drinkOriginalPrice = itemView.findViewById(R.id.drinkOriginalPrice);
            drinkQuantity = itemView.findViewById(R.id.drinkQuantity);
            drinkOriginalPrice.setPaintFlags(drinkOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            drinkDiscountedPrice = itemView.findViewById(R.id.drinkDiscountedPrice);

            edtQC = itemView.findViewById(R.id.edtQuantityCart);
            btnDQC = itemView.findViewById(R.id.btnDecreaseCart);
            btnIQC = itemView.findViewById(R.id.btnIncreaseCart);
            edtQC.setEnabled(false);
        }
    }
}