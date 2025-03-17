package com.example.tinkdrao;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.tinkdrao.model.Drink;
import com.example.tinkdrao.model.Order;
import com.example.tinkdrao.model.Cart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvCreatedAt, tvStatus, tvQuantity, tvTotal, tvAddress, tvPhoneNumber, tvStatusPay;
    private Button btnEdit, btnReorderDetail, btnCancelOrder;
    private GridView gridViewItems;
    private Spinner spinnerStatus, spinnerStatusPay;
    private Order order;
    private String role;
    private List<Drink> drinkList = new ArrayList<>();
    private List<String> statusList = new ArrayList<>();
    private List<String> statusPayList = new ArrayList<>();
    private DatabaseReference databaseReference, statusReference, statusPayReference;
    private boolean isEditing = false;
    static String phoneNumber;
    private FirebaseUser mUser;
    private long id = 0;
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        getSupportActionBar().setTitle("Chi tiết đơn hàng");
        phoneNumber = getIntent().getStringExtra("phoneNo");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // Khởi tạo các view
        tvCreatedAt = findViewById(R.id.tv_detail_created_at);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvQuantity = findViewById(R.id.tv_detail_quantity);
        tvTotal = findViewById(R.id.tv_detail_total);
        tvAddress = findViewById(R.id.tv_detail_address);
        tvPhoneNumber = findViewById(R.id.tv_detail_phone_number);
        tvStatusPay = findViewById(R.id.tv_detail_status_pay);
        btnEdit = findViewById(R.id.btn_edit_status);
        btnReorderDetail = findViewById(R.id.btn_reorder_detail);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        gridViewItems = findViewById(R.id.grid_view_items);
        spinnerStatus = new Spinner(this);
        spinnerStatusPay = new Spinner(this);

        order = (Order) getIntent().getSerializableExtra("order");
        role = getIntent().getStringExtra("role");

        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Order");
        statusReference = FirebaseDatabase.getInstance().getReference("Status");
        statusPayReference = FirebaseDatabase.getInstance().getReference("StatusPay");

        // Hiển thị thông tin cơ bản
        tvCreatedAt.setText("Ngày tạo: " + (order.getCreatedAt() != null ? order.getCreatedAt() : "Không rõ"));
        tvStatus.setText("Tình trạng: " + (order.getStatusOrder() != null ? order.getStatusOrder() : "Không rõ"));
        tvTotal.setText("Tổng tiền: " + (order.getTotal() != null ? order.getTotal() + " VNĐ" : "0 VNĐ"));
        tvAddress.setText("Địa chỉ: " + (order.getAddress() != null ? order.getAddress() : "Không rõ"));
        tvPhoneNumber.setText("Số điện thoại: " + (order.getPhoneNo() != null ? order.getPhoneNo() : "Không rõ"));
        updateStatusPayBasedOnOrderStatus();

        // Tải dữ liệu từ Firebase
        loadOrderDetails(order.getId());
        loadStatusList();
        loadStatusPayList();

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

        // Hiển thị nút theo vai trò
        if ("Admin".equals(role)) {
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v -> toggleEdit());
            btnReorderDetail.setVisibility(View.GONE);
            btnCancelOrder.setVisibility(View.GONE); // Ẩn nút hủy cho Admin
        } else {
            // Vai trò không phải Admin
            if (order.getStatusOrder() != null) {
                if (order.getStatusOrder().equals("Giao hàng thành công") || order.getStatusOrder().equals("Đã hủy")) {
                    btnReorderDetail.setVisibility(View.VISIBLE);
                    btnReorderDetail.setOnClickListener(v -> reorder());
                    btnCancelOrder.setVisibility(View.GONE);
                } else {
                    btnCancelOrder.setVisibility(View.VISIBLE);
                    btnCancelOrder.setOnClickListener(v -> showCancelConfirmationDialog());
                    btnReorderDetail.setVisibility(View.GONE);
                }
            } else {
                btnCancelOrder.setVisibility(View.VISIBLE); // Nếu trạng thái null, vẫn cho phép hủy
                btnCancelOrder.setOnClickListener(v -> showCancelConfirmationDialog());
                btnReorderDetail.setVisibility(View.GONE);
            }
            btnEdit.setVisibility(View.GONE);
        }
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

                        long itemCount = dataSnapshot.getChildrenCount();
                        tvQuantity.setText("Số lượng sản phẩm: " + itemCount);

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

    private void loadStatusPayList() {
        statusPayReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                statusPayList.clear();
                for (DataSnapshot statusSnapshot : snapshot.getChildren()) {
                    String statusPay = statusSnapshot.getValue(String.class);
                    if (statusPay != null) {
                        statusPayList.add(statusPay);
                    }
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(OrderDetailActivity.this,
                        android.R.layout.simple_spinner_item, statusPayList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerStatusPay.setAdapter(spinnerAdapter);

                if (order.getStatusPay() != null) {
                    int position = statusPayList.indexOf(order.getStatusPay());
                    if (position >= 0) {
                        spinnerStatusPay.setSelection(position);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi khi tải trạng thái thanh toán: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleEdit() {
        if (!isEditing) {
            tvStatus.setVisibility(View.GONE);
            spinnerStatus.setLayoutParams(tvStatus.getLayoutParams());
            ((ViewGroup) tvStatus.getParent()).addView(spinnerStatus);

            tvStatusPay.setVisibility(View.GONE);
            spinnerStatusPay.setLayoutParams(tvStatusPay.getLayoutParams());
            ((ViewGroup) tvStatusPay.getParent()).addView(spinnerStatusPay);

            btnEdit.setText("Lưu");
            isEditing = true;
        } else {
            String newStatus = spinnerStatus.getSelectedItem().toString();
            String newStatusPay = spinnerStatusPay.getSelectedItem().toString();

            saveStatus(newStatus);
            saveStatusPay(newStatusPay);

            ((ViewGroup) spinnerStatus.getParent()).removeView(spinnerStatus);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("Tình trạng: " + newStatus);

            ((ViewGroup) spinnerStatusPay.getParent()).removeView(spinnerStatusPay);
            tvStatusPay.setVisibility(View.VISIBLE);
            tvStatusPay.setText("Trạng thái thanh toán: " + newStatusPay);

            btnEdit.setText("Cập nhật tình trạng");
            isEditing = false;

            updateButtonVisibility(newStatus);
        }
    }

    private void saveStatus(String newStatus) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child(order.getId()).exists()) {
                        String userId = userSnapshot.getKey();
                        databaseReference.child(userId).child(order.getId()).child("statusOrder")
                                .setValue(newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(OrderDetailActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                                    order.setStatusOrder(newStatus);
                                    updateStatusPayBasedOnOrderStatus();
                                })
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

    private void saveStatusPay(String newStatusPay) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child(order.getId()).exists()) {
                        String userId = userSnapshot.getKey();
                        databaseReference.child(userId).child(order.getId()).child("statusPay")
                                .setValue(newStatusPay)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(OrderDetailActivity.this, "Đã cập nhật trạng thái thanh toán", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(OrderDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi khi lưu trạng thái thanh toán: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateStatusPayBasedOnOrderStatus() {
        if (order.getStatusOrder() != null && order.getStatusOrder().equals("Giao hàng thành công")) {
            tvStatusPay.setText("Trạng thái thanh toán: Đã thanh toán");
            order.setStatusPay("Đã thanh toán");
            saveStatusPay("Đã thanh toán");
        } else {
            tvStatusPay.setText("Trạng thái thanh toán: Chưa thanh toán");
            order.setStatusPay("Chưa thanh toán");
            saveStatusPay("Chưa thanh toán");
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    saveStatus("Đã hủy");
                    tvStatus.setText("Tình trạng: Đã hủy");
                    btnCancelOrder.setVisibility(View.GONE);
                    updateButtonVisibility("Đã hủy");
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void updateButtonVisibility(String currentStatus) {
        if ("Admin".equals(role)) {
            btnEdit.setVisibility(View.VISIBLE);
            btnReorderDetail.setVisibility(View.GONE);
            btnCancelOrder.setVisibility(View.GONE);
        } else {
            if (currentStatus.equals("Giao hàng thành công") || currentStatus.equals("Đã hủy")) {
                btnReorderDetail.setVisibility(View.VISIBLE);
                btnCancelOrder.setVisibility(View.GONE);
            } else {
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnReorderDetail.setVisibility(View.GONE);
            }
            btnEdit.setVisibility(View.GONE);
        }
    }

    private void reorder() {
        List<Cart> cartList = new ArrayList<>();
        for (Drink drink : drinkList) {
            Cart cart = new Cart(
                    drink.getId(),
                    drink.getImageUrl(),
                    drink.getName(),
                    drink.getPrice(),
                    0.0,
                    "Unknown",
                    drink.getQuantity(),
                    "Cái"
            );
            cartList.add(cart);
        }

        Intent intent = new Intent(OrderDetailActivity.this, Order_Activity.class);
        intent.putExtra("selectedItems", (Serializable) cartList);
        startActivity(intent);

        Toast.makeText(this, "Đã chuyển sang trang đặt hàng", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}