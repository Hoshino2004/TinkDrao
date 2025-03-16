package com.example.tinkdrao.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.OrderAnalyticDetailActivity;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Drink;
import com.example.tinkdrao.model.Order;

import java.util.List;

public class OrderAdapter2 extends RecyclerView.Adapter<OrderAdapter2.OrderViewHolder> {
    private List<Order> orderList;
    private static final int MAX_PRODUCTS_DISPLAY = 3; // Giới hạn hiển thị 3 sản phẩm

    public OrderAdapter2(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_analytic, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCreatedAt, tvAddress, tvTotal, tvMoreProducts, tvNameUser, tvPhoneNo;
        RecyclerView rvProducts;
        ProductAdapter productAdapter;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvMoreProducts = itemView.findViewById(R.id.tv_more_products);
            tvNameUser = itemView.findViewById(R.id.tv_name_user); // Ánh xạ nameUser
            tvPhoneNo = itemView.findViewById(R.id.tv_phone_no);   // Ánh xạ phoneNo
            rvProducts = itemView.findViewById(R.id.rv_products);
            rvProducts.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        void bind(Order order) {
            tvCreatedAt.setText(order.getCreatedAt());
            tvAddress.setText(order.getAddress());
            tvTotal.setText(String.format("%,d VNĐ", order.getTotal()));
            tvNameUser.setText(order.getNameUser()); // Hiển thị nameUser
            tvPhoneNo.setText(order.getPhoneNo());   // Hiển thị phoneNo

            // Giới hạn số lượng sản phẩm hiển thị
            List<Drink> products = order.getProducts();
            List<Drink> displayProducts = products.size() > MAX_PRODUCTS_DISPLAY
                    ? products.subList(0, MAX_PRODUCTS_DISPLAY)
                    : products;

            productAdapter = new ProductAdapter(displayProducts);
            rvProducts.setAdapter(productAdapter);

            // Hiển thị "Xem thêm" nếu có nhiều sản phẩm hơn giới hạn
            if (products.size() > MAX_PRODUCTS_DISPLAY) {
                int remainingCount = products.size() - MAX_PRODUCTS_DISPLAY;
                tvMoreProducts.setText(String.format("Xem thêm (+%d sản phẩm)", remainingCount));
                tvMoreProducts.setVisibility(View.VISIBLE);
                tvMoreProducts.setOnClickListener(v -> {
                    // Mở danh sách đầy đủ hoặc mở rộng hiển thị
                    Intent intent = new Intent(itemView.getContext(), OrderAnalyticDetailActivity.class);
                    intent.putExtra("order", order); // Truyền đối tượng Order qua Intent
                    itemView.getContext().startActivity(intent);
                });
            } else {
                tvMoreProducts.setVisibility(View.GONE);
            }
        }
    }
}
