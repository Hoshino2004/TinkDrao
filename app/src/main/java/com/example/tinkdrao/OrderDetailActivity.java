package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.tinkdrao.model.Drink;
import com.example.tinkdrao.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import com.example.tinkdrao.model.Cart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvCreatedAt, tvStatus, tvQuantity, tvTotal;
    private Button btnEditStatus, btnReorderDetail;
    private GridView gridViewItems;
    private Spinner spinnerStatus; // Thêm Spinner
    private Order order;
    private String role;
    private List<Drink> drinkList = new ArrayList<>();
    private List<String> statusList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private DatabaseReference statusReference;
    private boolean isEditing = false; // Trạng thái chỉnh sửa
    static String phoneNumber;
    private FirebaseUser mUser;              // Thêm dòng này
    private long id = 0;                     // Thêm dòng này
    LocalDateTime now = LocalDateTime.now(); // Thêm dòng này
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"); // Thêm dòng này

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        getSupportActionBar().setTitle("Chi tiết đơn hàng");

        phoneNumber = getIntent().getStringExtra("phoneNo");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mUser = FirebaseAuth.getInstance().getCurrentUser(); // Thêm dòng này

        tvCreatedAt = findViewById(R.id.tv_detail_created_at);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvQuantity = findViewById(R.id.tv_detail_quantity);
        tvTotal = findViewById(R.id.tv_detail_total);
        btnEditStatus = findViewById(R.id.btn_edit_status);
        btnReorderDetail = findViewById(R.id.btn_reorder_detail);
        gridViewItems = findViewById(R.id.grid_view_items);
        spinnerStatus = new Spinner(this); // Khởi tạo Spinner động

        order = (Order) getIntent().getSerializableExtra("order");
        role = getIntent().getStringExtra("role");

        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Order");
        statusReference = FirebaseDatabase.getInstance().getReference("Status");

        // Hiển thị thông tin cơ bản
        tvCreatedAt.setText("Ngày tạo: " + (order.getCreatedAt() != null ? order.getCreatedAt() : "Không rõ"));
        tvStatus.setText("Tình trạng: " + (order.getStatusOrder() != null ? order.getStatusOrder() : "Không rõ"));
        tvTotal.setText("Tổng tiền: " + (order.getTotal() != null ? order.getTotal() + " VNĐ" : "0 VNĐ"));

        // Tải dữ liệu từ Firebase và cập nhật giao diện
        loadOrderDetails(order.getId());

        // Thiết lập adapter cho GridView
        ArrayAdapter<Drink> adapter = new ArrayAdapter<Drink>(this, R.layout.item_order_product, R.id.tv_product_name, drinkList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Drink drink = drinkList.get(position);

                ImageView ivProductImage = view.findViewById(R.id.iv_product_image);
                TextView tvProductName = view.findViewById(R.id.tv_product_name);
                TextView tvProductPrice = view.findViewById(R.id.tv_product_price);
                TextView tvProductQuantity = view.findViewById(R.id.tv_product_quantity);

                Glide.with(OrderDetailActivity.this)
                        .load(drink.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivProductImage);

                tvProductName.setText(drink.getName());
                tvProductPrice.setText("Giá: " + drink.getPrice() + " VNĐ");
                tvProductQuantity.setText("Số lượng: " + drink.getQuantity());

                return view;
            }
        };
        gridViewItems.setAdapter(adapter);

        // Tải danh sách trạng thái từ Firebase cho Spinner
        loadStatusList();

        // Hiển thị nút theo vai trò
        if ("Admin".equals(role)) {
            btnEditStatus.setVisibility(View.VISIBLE);
            btnEditStatus.setOnClickListener(v -> toggleEditStatus());
            btnReorderDetail.setVisibility(View.GONE);
        } else {
            if (order.getStatusOrder() != null &&
                    (order.getStatusOrder().equals("Giao hàng thành công") || order.getStatusOrder().equals("Đã hủy"))) {
                btnReorderDetail.setVisibility(View.VISIBLE);
                btnReorderDetail.setOnClickListener(v -> reorder());
            } else {
                btnReorderDetail.setVisibility(View.GONE);
            }
            btnEditStatus.setVisibility(View.GONE);
        }

        // Thêm log để debug
//        Toast.makeText(this, "Role: " + role + ", Status: " + order.getStatusOrder(), Toast.LENGTH_SHORT).show();
    }

    private void loadOrderDetails(String orderId) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drinkList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child(orderId).exists()) {
                        DataSnapshot orderSnapshot = userSnapshot.child(orderId);
                        DataSnapshot dataSnapshot = orderSnapshot.child("Data");

                        // Cập nhật số lượng sản phẩm
                        long itemCount = dataSnapshot.getChildrenCount();
                        tvQuantity.setText("Số lượng sản phẩm: " + itemCount);

                        // Tải danh sách sản phẩm
                        for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                            Drink drink = productSnapshot.getValue(Drink.class);
                            if (drink != null) {
                                drinkList.add(drink);
                            }
                        }
                        break;
                    }
                }
                ((ArrayAdapter<?>) gridViewItems.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadStatusList() {
        statusReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                statusList.clear();
                for (DataSnapshot statusSnapshot : snapshot.getChildren()) {
                    String status = statusSnapshot.getValue(String.class);
                    if (status != null) {
                        statusList.add(status);
                    }
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(OrderDetailActivity.this,
                        android.R.layout.simple_spinner_item, statusList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerStatus.setAdapter(spinnerAdapter);

                // Chọn trạng thái hiện tại trong Spinner
                if (order.getStatusOrder() != null) {
                    int position = statusList.indexOf(order.getStatusOrder());
                    if (position >= 0) {
                        spinnerStatus.setSelection(position);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi khi tải trạng thái: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleEditStatus() {
        if (!isEditing) {
            // Chuyển sang chế độ chỉnh sửa
            tvStatus.setVisibility(View.GONE);

            // Đặt layout params cho Spinner giống TextView
            spinnerStatus.setLayoutParams(tvStatus.getLayoutParams());
            ((ViewGroup) tvStatus.getParent()).addView(spinnerStatus);

            btnEditStatus.setText("Lưu");
            isEditing = true;
        } else {
            // Lưu trạng thái mới
            String newStatus = spinnerStatus.getSelectedItem().toString();
            saveStatus(newStatus);

            // Chuyển về chế độ hiển thị
            ((ViewGroup) spinnerStatus.getParent()).removeView(spinnerStatus);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("Tình trạng: " + newStatus);

            btnEditStatus.setText("Cập nhật tình trạng");
            isEditing = false;
        }
    }

    private void saveStatus(String newStatus) {
        // Tìm userId chứa orderId
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child(order.getId()).exists()) {
                        String userId = userSnapshot.getKey();
                        databaseReference.child(userId).child(order.getId()).child("statusOrder")
                                .setValue(newStatus)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(OrderDetailActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(OrderDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi khi lưu: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void editStatus() {
        Toast.makeText(this, "Sửa trạng thái đơn hàng " + order.getId(), Toast.LENGTH_SHORT).show();
    }

    private void reorder() {
        // Chuẩn bị danh sách Cart từ drinkList
        List<Cart> cartList = new ArrayList<>();
        for (Drink drink : drinkList) {
            Cart cart = new Cart(
                    drink.getId(),           // long id
                    drink.getImageUrl(),     // String imageUrl
                    drink.getName(),         // String name
                    drink.getPrice(),        // double price
                    0.0,                     // double discount (mặc định)
                    "Unknown",               // String drinkType (mặc định)
                    drink.getQuantity(),     // int quantity
                    "Cái"                    // String unit (mặc định)
            );
            cartList.add(cart);
        }

        // Tạo Intent để chuyển sang Order_Activity
        Intent intent = new Intent(OrderDetailActivity.this, Order_Activity.class);
        intent.putExtra("selectedItems", (Serializable) cartList);
        startActivity(intent);

        // Hiển thị thông báo và quay lại (tùy chọn)
        Toast.makeText(this, "Đã chuyển sang trang đặt hàng", Toast.LENGTH_SHORT).show();
        finish(); // Tùy chọn: đóng OrderDetailActivity sau khi chuyển
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