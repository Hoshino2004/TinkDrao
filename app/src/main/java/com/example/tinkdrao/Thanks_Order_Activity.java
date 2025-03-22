package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.adapter.OrderAdapter;
import com.example.tinkdrao.adapter.OrderAdapter3;
import com.example.tinkdrao.model.Cart;
import com.example.tinkdrao.model.Drink;
import com.example.tinkdrao.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

public class Thanks_Order_Activity extends AppCompatActivity {

    private TextView idOrder, timeOrder, priceOrder, discountOrder, totalOrder, nameOrder, phoneOrder, addressOrder;
    private Button btnBackHome;
    private RecyclerView recDetailOrder;
    private DatabaseReference drinkRef;
    private FirebaseUser mUser;
    private String getIdOrder;
    private DecimalFormat format;
    private double total = 0;
    private List<Cart> cartList;
    private OrderAdapter3 orderAdapter3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanks_order);

        //Khai báo
        idOrder = findViewById(R.id.tvIdOrder);
        timeOrder = findViewById(R.id.tvTimeOrder);
        priceOrder = findViewById(R.id.tvPriceOrder);
        discountOrder = findViewById(R.id.tvDiscountOrder);
        totalOrder = findViewById(R.id.tvTotalPrice);
        nameOrder = findViewById(R.id.tvNameOrder);
        phoneOrder = findViewById(R.id.tvPhoneOrder);
        addressOrder = findViewById(R.id.tvAddressOrder);
        btnBackHome = findViewById(R.id.btnBackHome);
        recDetailOrder = findViewById(R.id.recyclerOrder);
        drinkRef = FirebaseDatabase.getInstance().getReference("TinkDrao");
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        getIdOrder = getIntent().getStringExtra("idOrder");
        format = new DecimalFormat("#,###");

        //Thay đổi TextView
        addInformation();

        //Về trang home
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Thanks_Order_Activity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void addInformation() {
        drinkRef.child("Order").child(mUser.getUid()).child(getIdOrder).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null) {
                        idOrder.setText(getIdOrder != null ? getIdOrder : "N/A");
                        timeOrder.setText(order.getCreatedAt() != null ? order.getCreatedAt() : "N/A");
                        totalOrder.setText(format.format((double) order.getTotal()) + " VNĐ");
                        nameOrder.setText(order.getNameUser() != null ? order.getNameUser() : "N/A");
                        phoneOrder.setText(order.getPhoneNo() != null ? order.getPhoneNo() : "N/A");
                        addressOrder.setText(order.getAddress() != null ? order.getAddress() : "N/A");

                        // Khởi tạo RecyclerView và Adapter
                        cartList = new ArrayList<>();
                        orderAdapter3 = new OrderAdapter3(Thanks_Order_Activity.this, cartList);
                        recDetailOrder.setLayoutManager(new LinearLayoutManager(Thanks_Order_Activity.this, LinearLayoutManager.VERTICAL, false));
                        recDetailOrder.setAdapter(orderAdapter3);

                        // Truy xuất danh sách các mục trong node "Data"
                        drinkRef.child("Order").child(mUser.getUid()).child(getIdOrder).child("Data").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                total = 0;
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                                        Cart cart = itemSnapshot.getValue(Cart.class); // Sử dụng Cart thay vì Drink
                                        if (cart != null) {
                                            // Thêm vào cartList để hiển thị trong RecyclerView
                                            cartList.add(cart);

                                            // Tính giá sau khi giảm giá (nếu có)
                                            total += cart.getPrice() * cart.getQuantity();
                                        }
                                    }
                                    // Cập nhật RecyclerView sau khi thêm dữ liệu
                                    orderAdapter3.notifyDataSetChanged();

                                    // Cập nhật priceOrder và discountOrder
                                    priceOrder.setText(format.format((int) total) + " VNĐ");
                                    discountOrder.setText("-" + format.format((int) total - order.getTotal()) + " VNĐ");
                                } else {
                                    priceOrder.setText("0 VNĐ");
                                    discountOrder.setText("0 VNĐ");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Thanks_Order_Activity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Thanks_Order_Activity.this, "Không thể tải dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Thanks_Order_Activity.this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Thanks_Order_Activity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}