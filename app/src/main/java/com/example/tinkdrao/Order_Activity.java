package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.adapter.OrderAdapter;
import com.example.tinkdrao.model.Cart;
import com.example.tinkdrao.model.Drink;
import com.example.tinkdrao.model.Order;
import com.example.tinkdrao.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order_Activity extends AppCompatActivity {
    private RecyclerView recOrder;
    private TextView tvTotalO1, tvTotalO2, tvSuccessMessage;
    private ProgressBar progressBar;
    private Button btnOrder;
    private List<Cart> selectedItems;
    private DecimalFormat decimalFormat;
    private EditText edtName, edtPhoneNo, edtAddress;
    FirebaseUser mUser;
    DatabaseReference userRef, orderRef, cartRef, removeRef, drinkRef;
    private long id = 0;
    private long total = 0;
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    String formattedTime = now.format(formatter);
    static String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        phoneNumber = getIntent().getStringExtra("phoneNo");

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        drinkRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Drink");
        if(mUser!=null)
        {
            userRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Users/"+mUser.getUid());
            orderRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Order/"+mUser.getUid());
            cartRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Order/"+mUser.getUid());
            removeRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Cart/"+mUser.getUid());
        } else if (phoneNumber!=null) {
            orderRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Order/"+phoneNumber);
            cartRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Order/"+phoneNumber);
            removeRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Cart/"+phoneNumber);
        }

        decimalFormat = new DecimalFormat("#,###");

        getSupportActionBar().setTitle("Đặt hàng");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recOrder = findViewById(R.id.recOrder);
        tvTotalO1 = findViewById(R.id.tvTotalO1);
        tvTotalO2 = findViewById(R.id.tvTotalO2);
        btnOrder = findViewById(R.id.btnOrder);
        edtName = findViewById(R.id.edtNameOrder);
        edtPhoneNo = findViewById(R.id.edtPhoneNoOrder);
        edtAddress = findViewById(R.id.edtAddressOrder);
        progressBar = findViewById(R.id.progressBar);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

        // Nhận dữ liệu từ Intent
        selectedItems = (ArrayList<Cart>) getIntent().getSerializableExtra("selectedItems");
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }

        // Thiết lập RecyclerView với OrderAdapter
        OrderAdapter orderAdapter = new OrderAdapter(selectedItems);
        recOrder.setLayoutManager(new LinearLayoutManager(this));
        recOrder.setAdapter(orderAdapter);

        // Cập nhật tổng tiền và số lượng
        updateOrderSummary();

        // Check id Firebase
        checkOrder();

        // Up dữ liệu lên Firebase
        if(mUser!=null)
        {
            upOrder();
        } else if (phoneNumber!=null) {
            updateOrder();
        }
    }

    private void updateOrder() {
        edtPhoneNo.setEnabled(false);
        edtPhoneNo.setText(phoneNumber);
        checkOrder();
        // Sự kiện nhấn nút "Đặt hàng"
        btnOrder.setOnClickListener(v -> {
            {
                if(!edtAddress.getText().toString().equals("") && !edtName.getText().toString().equals(""))
                {
                    // Hiển thị ProgressBar và ẩn giao diện khác
                    progressBar.setVisibility(View.VISIBLE);
                    findViewById(R.id.main).setAlpha(0.3f); // Làm mờ giao diện chính
                    btnOrder.setEnabled(false);

                    total = 0;
                    for (Cart cart : selectedItems) {
                        double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
                        total += discountedPrice * cart.getQuantity();
                    }

                    // Thêm logic đặt hàng tại đây (ví dụ: gửi lên Firebase)
                    orderRef.child("HoaDon" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Order order = new Order(edtName.getText().toString(), phoneNumber, edtAddress.getText().toString(), formattedTime, "Chờ vận chuyển", total, "HoaDon" + id, "Chưa thanh toán");
                            orderRef.child("HoaDon" + id).setValue(order).addOnSuccessListener(aVoid -> {
                                        // Sau khi lưu thành công, ẩn ProgressBar và hiển thị thông báo
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            tvSuccessMessage.setVisibility(View.VISIBLE);
                                            findViewById(R.id.main).setAlpha(1.0f); // Khôi phục giao diện
                                            // Quay lại sau 2 giây
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1000);
                                        }, 2000); // Delay 2 giây để mô phỏng loading
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        findViewById(R.id.main).setAlpha(1.0f);
                                        btnOrder.setEnabled(true);
                                        Toast.makeText(Order_Activity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            for(Cart cart : selectedItems)
                            {
                                cartRef.child("HoaDon"+id).child("Data").child(String.valueOf(cart.getId())).setValue(cart);
                                drinkRef.child(String.valueOf(cart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists())
                                        {
                                            Cart itemCart = snapshot.getValue(Cart.class);
                                            drinkRef.child(String.valueOf(itemCart.getId())).child("quantity").setValue(itemCart.getQuantity()-cart.getQuantity());
                                            drinkRef.child(String.valueOf(itemCart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists())
                                                    {
                                                        Drink drink = snapshot.getValue(Drink.class);
                                                        drinkRef.child(String.valueOf(itemCart.getId())).child("purchaseCount").setValue(drink.getPurchaseCount()+ cart.getQuantity());
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                removeRef.child(String.valueOf(cart.getId())).removeValue();
                            }
                            selectedItems.clear();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
                else
                {
                    Toast.makeText(Order_Activity.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void upOrder() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    edtName.setEnabled(false);
                    edtName.setText(user.getUsername());
                    edtPhoneNo.setEnabled(false);
                    edtPhoneNo.setText(user.getPhoneno());
                    orderRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                id = snapshot.getChildrenCount();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    // Sự kiện nhấn nút "Đặt hàng"
                    btnOrder.setOnClickListener(v -> {
                        {
                            if(!edtAddress.getText().toString().equals(""))
                            {
                                // Hiển thị ProgressBar và ẩn giao diện khác
                                progressBar.setVisibility(View.VISIBLE);
                                findViewById(R.id.main).setAlpha(0.3f); // Làm mờ giao diện chính
                                btnOrder.setEnabled(false);

                                total = 0;
                                for (Cart cart : selectedItems) {
                                    double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
                                    total += discountedPrice * cart.getQuantity();
                                }

                                // Thêm logic đặt hàng tại đây (ví dụ: gửi lên Firebase)
                                orderRef.child("HoaDon" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Order order = new Order(user.getUsername(), user.getPhoneno(), edtAddress.getText().toString(), formattedTime, "Chờ vận chuyển", total, "HoaDon" + id, "Chưa thanh toán");
                                        orderRef.child("HoaDon" + id).setValue(order).addOnSuccessListener(aVoid -> {
                                                    // Sau khi lưu thành công, ẩn ProgressBar và hiển thị thông báo
                                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        tvSuccessMessage.setVisibility(View.VISIBLE);
                                                        findViewById(R.id.main).setAlpha(1.0f); // Khôi phục giao diện
                                                        // Quay lại sau 2 giây
                                                        new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1000);
                                                    }, 2000); // Delay 2 giây để mô phỏng loading
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    findViewById(R.id.main).setAlpha(1.0f);
                                                    btnOrder.setEnabled(true);
                                                    Toast.makeText(Order_Activity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                        for(Cart cart : selectedItems)
                                        {
                                            cartRef.child("HoaDon"+id).child("Data").child(String.valueOf(cart.getId())).setValue(cart);
                                            drinkRef.child(String.valueOf(cart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists())
                                                    {
                                                        Cart itemCart = snapshot.getValue(Cart.class);
                                                        drinkRef.child(String.valueOf(itemCart.getId())).child("quantity").setValue(itemCart.getQuantity()-cart.getQuantity());
                                                        drinkRef.child(String.valueOf(itemCart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if(snapshot.exists())
                                                                {
                                                                    Drink drink = snapshot.getValue(Drink.class);
                                                                    drinkRef.child(String.valueOf(itemCart.getId())).child("purchaseCount").setValue(drink.getPurchaseCount()+ cart.getQuantity());
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                            removeRef.child(String.valueOf(cart.getId())).removeValue();
                                        }
                                        selectedItems.clear();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(Order_Activity.this, "Vui lòng nhập địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void checkOrder() {
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    id = snapshot.getChildrenCount();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateOrderSummary() {
        if (selectedItems.isEmpty()) {
            tvTotalO1.setText("0₫");
            tvTotalO2.setText("Tổng (0 mặt hàng)\n0₫");
        } else {
            total = 0;
            for (Cart cart : selectedItems) {
                double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
                total += discountedPrice * cart.getQuantity();
            }
            tvTotalO1.setText(decimalFormat.format(total) + "₫");
            tvTotalO2.setText("Tổng (" + selectedItems.size() + " mặt hàng)\n" + decimalFormat.format(total) + "₫");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(phoneNumber!=null)
        {
            Intent intent = new Intent(Order_Activity.this, Cart_Activity.class);
            intent.putExtra("phoneNo",phoneNumber);
            startActivity(intent);
            finish();
        }
        return true;
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