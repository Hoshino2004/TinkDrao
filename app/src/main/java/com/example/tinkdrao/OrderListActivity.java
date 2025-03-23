package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tinkdrao.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections; // Thêm dòng này
import java.util.Comparator;  // Thêm dòng này

public class OrderListActivity extends AppCompatActivity {

    private Spinner spinnerStatus;
    private ListView listViewOrders;
    private List<Order> orderList = new ArrayList<>();
    private List<Order> filteredList = new ArrayList<>();
    private String currentUserRole;
    private String currentUserId;
    private ArrayAdapter<Order> adapter;
    private DatabaseReference databaseReference;
    private DatabaseReference userReference;
    private DatabaseReference statusReference;
    private FirebaseAuth auth;
    static String phoneNumber;
    private DecimalFormat decimalFormat; // Định dạng giá tiền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        phoneNumber = getIntent().getStringExtra("phoneNo");

        getSupportActionBar().setTitle("Danh sách đơn hàng");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        decimalFormat = new DecimalFormat("#,###"); // Dấu chấm: 12.000
        decimalFormat.setDecimalSeparatorAlwaysShown(false); // Không hiển thị phần thập phân

        spinnerStatus = findViewById(R.id.spinner_status);
        listViewOrders = findViewById(R.id.list_view_orders);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Order");
        userReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Users");
        statusReference = FirebaseDatabase.getInstance().getReference("Status");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        loadOrdersFromFirebase();

        final List<String> statusList = new ArrayList<>();
        statusList.add("Tất cả");
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(spinnerAdapter);

        statusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                statusList.clear();
                statusList.add("Tất cả");
                for (DataSnapshot statusSnapshot : snapshot.getChildren()) {
                    String status = statusSnapshot.getValue(String.class);
                    if (status != null) {
                        statusList.add(status);
                    }
                }
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderListActivity.this, "Lỗi khi tải trạng thái: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterOrders(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        adapter = new ArrayAdapter<Order>(this, R.layout.item_order2, filteredList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(OrderListActivity.this).inflate(R.layout.item_order2, parent, false);
                }

                if (position < 0 || position >= filteredList.size()) {
                    return view;
                }

                Order order = filteredList.get(position);
                if (order == null) {
                    return view;
                }

                TextView tvCreatedAt = view.findViewById(R.id.tv_created_at);
                TextView tvStatus = view.findViewById(R.id.tv_status);
                TextView tvQuantity = view.findViewById(R.id.tv_quantity);
                TextView tvTotal = view.findViewById(R.id.tv_total);
                TextView tvNameUser = view.findViewById(R.id.tv_name_user); // Thêm dòng này
                //Button btnReorder = view.findViewById(R.id.btn_reorder);

                if (tvCreatedAt != null) {
                    tvCreatedAt.setText("Ngày tạo: " + (order.getCreatedAt() != null ? order.getCreatedAt() : "Không rõ"));
                }
                if (tvStatus != null) {
                    tvStatus.setText("Tình trạng: " + (order.getStatusOrder() != null ? order.getStatusOrder() : "Không rõ"));
                }
                if (tvTotal != null) {
                    tvTotal.setText("Tổng tiền: " + (order.getTotal() != null ? decimalFormat.format(order.getTotal()) + " VNĐ" : "0 VNĐ"));
                }
                if (tvNameUser != null) {
                    if ("User".equals(currentUserRole)) {
                        tvNameUser.setVisibility(View.GONE); // Ẩn tên người dùng khi role là User
                    } else {
                        tvNameUser.setVisibility(View.VISIBLE); // Hiện tên khi role không phải User (Admin)
                        tvNameUser.setText("Tên người dùng: " + (order.getNameUser() != null ? order.getNameUser() : "Không rõ"));
                    }
                }

                if (tvQuantity != null) {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String foundUserId = null;
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                if (userSnapshot.child(order.getId()).exists()) {
                                    foundUserId = userSnapshot.getKey();
                                    break;
                                }
                            }
                            if (foundUserId != null) {
                                databaseReference.child(foundUserId).child(order.getId()).child("Data")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (tvQuantity != null) {
                                                    tvQuantity.setText("Số lượng sản phẩm: " + dataSnapshot.getChildrenCount());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                if (tvQuantity != null) {
                                                    tvQuantity.setText("Số lượng sản phẩm: Lỗi");
                                                }
                                            }
                                        });
                            } else {
                                tvQuantity.setText("Số lượng sản phẩm: Không tìm thấy");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (tvQuantity != null) {
                                tvQuantity.setText("Số lượng sản phẩm: Lỗi");
                            }
                        }
                    });
                }

                return view;
            }
        };
        listViewOrders.setAdapter(adapter);

        listViewOrders.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < filteredList.size()) {
                Order selectedOrder = filteredList.get(position);
                if (selectedOrder != null) {
                    Intent detailIntent = new Intent(OrderListActivity.this, OrderDetailActivity.class);
                    detailIntent.putExtra("order", selectedOrder);
                    detailIntent.putExtra("role", currentUserRole);
                    detailIntent.putExtra("phoneNo",phoneNumber);
                    startActivity(detailIntent);
                } else {
                    Toast.makeText(OrderListActivity.this, "Đơn hàng không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OrderListActivity.this, "Vị trí không hợp lệ: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        filterOrders("Tất cả");
    }

    private void loadOrdersFromFirebase() {
        if(currentUserId!=null)
        {
            userReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        currentUserRole = (role != null && role.equals("Admin")) ? "Admin" : "User";

                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                orderList.clear();
                                if ("Admin".equals(currentUserRole)) {
                                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                        for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                                            Order order = orderSnapshot.getValue(Order.class);
                                            if (order != null) {
                                                orderList.add(order);
                                            }
                                        }
                                    }
                                } else {
                                    DataSnapshot userSnapshot = snapshot.child(currentUserId);
                                    if (userSnapshot.exists()) {
                                        for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                                            Order order = orderSnapshot.getValue(Order.class);
                                            if (order != null) {
                                                orderList.add(order);
                                            }
                                        }
                                    }
                                }
                                // Sắp xếp orderList theo createdAt giảm dần
                                Collections.sort(orderList, new Comparator<Order>() {
                                    @Override
                                    public int compare(Order o1, Order o2) {
                                        String createdAt1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
                                        String createdAt2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
                                        return createdAt2.compareTo(createdAt1); // Giảm dần (mới nhất trước)
                                    }
                                });
                                filterOrders(spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "Tất cả");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(OrderListActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(OrderListActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
                        currentUserRole = "User";
                        loadUserOrders();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(OrderListActivity.this, "Lỗi khi kiểm tra role: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    currentUserRole = "User";
                    loadUserOrders();
                }
            });
        }
        else {
            userReference.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentUserRole = "User";

                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                orderList.clear();
                                    DataSnapshot userSnapshot = snapshot.child(phoneNumber);
                                    if (userSnapshot.exists()) {
                                        for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                                            Order order = orderSnapshot.getValue(Order.class);
                                            if (order != null) {
                                                orderList.add(order);
                                            }
                                        }
                                }
                                // Sắp xếp orderList theo createdAt giảm dần
                                Collections.sort(orderList, new Comparator<Order>() {
                                    @Override
                                    public int compare(Order o1, Order o2) {
                                        String createdAt1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
                                        String createdAt2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
                                        return createdAt2.compareTo(createdAt1); // Giảm dần (mới nhất trước)
                                    }
                                });
                                filterOrders(spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "Tất cả");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(OrderListActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        currentUserRole = "User";
                        loadUserOrders();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(OrderListActivity.this, "Lỗi khi kiểm tra role: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    currentUserRole = "User";
                    loadUserOrders();
                }
            });
        }
    }

    private void loadUserOrders() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                if(currentUserId !=null)
                {
                    DataSnapshot userSnapshot = snapshot.child(currentUserId);
                    if (userSnapshot.exists()) {
                        for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }
                    }
                }
                else
                {
                    DataSnapshot userSnapshot = snapshot.child(phoneNumber);
                    if (userSnapshot.exists()) {
                        for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }
                    }
                }
                // Sắp xếp orderList theo createdAt giảm dần
                Collections.sort(orderList, new Comparator<Order>() {
                    @Override
                    public int compare(Order o1, Order o2) {
                        String createdAt1 = o1.getCreatedAt() != null ? o1.getCreatedAt() : "";
                        String createdAt2 = o2.getCreatedAt() != null ? o2.getCreatedAt() : "";
                        return createdAt2.compareTo(createdAt1); // Giảm dần (mới nhất trước)
                    }
                });

                filterOrders(spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "Tất cả");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderListActivity.this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterOrders(String status) {
        filteredList.clear();
        if (status.equals("Tất cả")) {
            filteredList.addAll(orderList);
        } else {
            for (Order order : orderList) {
                if (order.getStatusOrder() != null && order.getStatusOrder().equals(status)) {
                    filteredList.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void reorder(Order order) {
        Toast.makeText(this, "Đã thêm đơn hàng " + order.getId() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
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