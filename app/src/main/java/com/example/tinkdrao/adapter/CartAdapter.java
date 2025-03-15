package com.example.tinkdrao.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Cart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Cart> cartList;
    private Context context;
    private DecimalFormat decimalFormat;
    private boolean isMultiSelectMode = false; // Trạng thái chế độ chọn nhiều
    private List<Cart> selectedItems = new ArrayList<>(); // Danh sách các mục được chọn
    private OnItemSelectionChangedListener selectionListener; // Listener để thông báo thay đổi

    // Interface để thông báo thay đổi số lượng mục được chọn
    public interface OnItemSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public CartAdapter(Context context, List<Cart> cartList, OnItemSelectionChangedListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.selectionListener = listener;
        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
    }

    // Phương thức để bật/tắt chế độ chọn nhiều
    public void setMultiSelectMode(boolean enabled) {
        isMultiSelectMode = enabled;
        selectedItems.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedItems.size());
        }
    }

    // Lấy danh sách các mục được chọn
    public List<Cart> getSelectedItems() {
        return new ArrayList<>(selectedItems);
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
        if (cart.getDiscount() > 0) {
            double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
            holder.drinkOriginalPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
            holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.drinkOriginalPrice.setVisibility(View.GONE);
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
        }

        Glide.with(context)
                .load(cart.getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(holder.drinkImage);

        // Cập nhật giao diện khi mục được chọn
        if (selectedItems.contains(cart)) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Sự kiện nhấn giữ để kích hoạt chế độ chọn
        holder.itemView.setOnLongClickListener(v -> {
            if (!isMultiSelectMode) {
                isMultiSelectMode = true;
                selectedItems.add(cart);
                holder.itemView.setBackgroundColor(Color.LTGRAY);
                Toast.makeText(context, "Đã kích hoạt chế độ chọn", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
                if (selectionListener != null) {
                    selectionListener.onSelectionChanged(selectedItems.size());
                }
            }
            return true;
        });

        // Sự kiện nhấn ngắn để chọn/bỏ chọn
        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                if (selectedItems.contains(cart)) {
                    selectedItems.remove(cart);
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    selectedItems.add(cart);
                    holder.itemView.setBackgroundColor(Color.LTGRAY);
                }
                notifyDataSetChanged();
                if (selectionListener != null) {
                    selectionListener.onSelectionChanged(selectedItems.size());
                }
            }
        });
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