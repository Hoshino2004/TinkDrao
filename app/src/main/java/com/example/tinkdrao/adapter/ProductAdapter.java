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
import com.example.tinkdrao.model.Drink;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Drink> productList;

    public ProductAdapter(List<Drink> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_analytic_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Drink product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductQuantity, tvProductPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
        }

        void bind(Drink product) {
            tvProductName.setText(product.getName());
            tvProductQuantity.setText(String.format("x%d (%s)", product.getQuantity(), product.getUnit()));
            // Ép kiểu về int để đảm bảo tương thích với %d
            int totalPrice = (int) (product.getPrice() * product.getQuantity());
            tvProductPrice.setText(String.format("%,d VNĐ", totalPrice));
            // Nếu cần tải hình ảnh:
             Glide.with(itemView.getContext()).load(product.getImageUrl()).into(ivProductImage);
        }
    }
}