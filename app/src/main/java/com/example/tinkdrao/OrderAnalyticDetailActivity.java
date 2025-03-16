package com.example.tinkdrao;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.adapter.DrinkAdapter;
import com.example.tinkdrao.adapter.ProductAdapter;
import com.example.tinkdrao.model.Order;

public class OrderAnalyticDetailActivity extends AppCompatActivity {

    private RecyclerView rvFullProducts;
    private TextView tvCreatedAt, tvAddress, tvTotal, tvNameUser, tvPhoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_analytic_detail);

        getSupportActionBar().setTitle("Chi tiết đơn hàng");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvCreatedAt = findViewById(R.id.tv_created_at);
        tvAddress = findViewById(R.id.tv_address);
        tvTotal = findViewById(R.id.tv_total);
        tvNameUser = findViewById(R.id.tv_name_user);
        tvPhoneNo = findViewById(R.id.tv_phone_no);
        rvFullProducts = findViewById(R.id.rv_full_products);
        rvFullProducts.setLayoutManager(new LinearLayoutManager(this));

        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            tvCreatedAt.setText(order.getCreatedAt());
            tvAddress.setText(order.getAddress());
            tvTotal.setText(String.format("%,d VNĐ", order.getTotal()));
            tvNameUser.setText(order.getNameUser());
            tvPhoneNo.setText(order.getPhoneNo());
            ProductAdapter adapter = new ProductAdapter(order.getItems());
            rvFullProducts.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Khi bấm nút Back, quay về Activity trước đó
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}