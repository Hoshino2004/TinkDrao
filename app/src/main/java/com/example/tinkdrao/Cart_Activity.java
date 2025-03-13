package com.example.tinkdrao;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.adapter.CartAdapter;
import com.example.tinkdrao.adapter.DrinkAdapter;
import com.example.tinkdrao.model.Cart;
import com.example.tinkdrao.model.Drink;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Cart_Activity extends AppCompatActivity {

    TextView totalPrice;
    RecyclerView recyclerViewCart;
    FirebaseUser mUser;
    LinearLayoutManager linearLayoutManager;
    DatabaseReference cartRef, drinkRef;
    FirebaseRecyclerAdapter<Cart, CartAdapter.CartViewHolder> firebaseRecyclerAdapter;
    long total = 0;
    int quantity = 0;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);

        getSupportActionBar().setTitle("Giỏ hàng");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        totalPrice = findViewById(R.id.totalPrice);
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        cartRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Cart").child(mUser.getUid());
        drinkRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Drink");
        showData();
    }
    private void showData() {
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    totalPrice.setText("Tổng tiền: 0 ₫");
                    return;
                }

                // Danh sách trung gian để lưu trữ dữ liệu
                List<Cart> cartItems = new ArrayList<>();

                // Lấy dữ liệu từ cartRef
                for (DataSnapshot cartSnapshot : snapshot.getChildren()) {
                    String drinkId = cartSnapshot.getKey();
                    Cart cart = cartSnapshot.getValue(Cart.class);
                    if (cart != null) {
                        cart.setId(Long.valueOf(drinkId)); // Gán ID cho cart
                        cartItems.add(cart);
                    }
                }

                // Lấy thông tin đồ uống cho từng sản phẩm trong giỏ hàng
                for (Cart cart : cartItems) {
                    drinkRef.child(String.valueOf(cart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot drinkSnapshot) {
                            if (drinkSnapshot.exists()) {
                                Drink drink = drinkSnapshot.getValue(Drink.class);
                                if (drink != null) {
                                    cart.setName(drink.getName());
                                    cart.setImageUrl(drink.getImageUrl());
                                    cart.setPrice(drink.getPrice());
                                    cart.setDiscount(drink.getDiscount());

                                    // Tính tổng tiền
                                    double discountedPrice = drink.getPrice() - (drink.getPrice() * drink.getDiscount() / 100);
                                    total += discountedPrice * cart.getQuantity();
                                    totalPrice.setText("Tổng tiền: " + decimalFormat.format((int) total) + " ₫");

                                    // Cập nhật adapter khi tất cả dữ liệu đã sẵn sàng
                                    updateRecyclerView(cartItems);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "Lỗi khi lấy dữ liệu đồ uống: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu giỏ hàng: " + error.getMessage());
            }
        });
    }

    private void updateRecyclerView(List<Cart> cartItems) {
        // Dừng adapter cũ nếu có
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }

        // Tạo adapter mới với danh sách cartItems
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cart, CartAdapter.CartViewHolder>(
                new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartRef, Cart.class) // Giữ query gốc nếu cần
                        .build()) {
            @Override
            protected void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position, @NonNull Cart cart) {
                holder.drinkName.setText(cart.getName());
                Glide.with(holder.itemView.getContext())
                        .load(cart.getImageUrl())
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.loading)
                        .into(holder.drinkImage);

                double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
                if (cart.getDiscount() > 0) {
                    holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
                    holder.drinkOriginalPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
                    holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
                } else {
                    holder.drinkOriginalPrice.setVisibility(View.GONE);
                    holder.drinkDiscountedPrice.setText(decimalFormat.format((int) cart.getPrice()) + "₫");
                }

                drinkRef.child(String.valueOf(cart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            Drink drink = snapshot.getValue(Drink.class);
                            holder.drinkQuantity.setText("Tồn kho: "+ drink.getQuantity());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Setup sẵn số lượng
                holder.edtQC.setText(String.valueOf(cart.getQuantity()));

                // Nút tăng giảm số lượng
                holder.btnDQC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(holder.edtQC.getText().toString().equals("1"))
                        {
                            Toast.makeText(Cart_Activity.this, "Số lượng bắt buộc phải từ 1 trở lên", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            quantity = Integer.valueOf(holder.edtQC.getText().toString())-1;
                            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    cartRef.child(String.valueOf(cart.getId())).child("quantity").setValue(quantity);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            holder.edtQC.setText(String.valueOf(quantity));
                        }
                    }
                });

                holder.btnIQC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        drinkRef.child(String.valueOf(cart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    Drink drink = snapshot.getValue(Drink.class);
                                    quantity = Integer.valueOf(holder.edtQC.getText().toString())+1;
                                    if(quantity>drink.getQuantity())
                                    {
                                        Toast.makeText(Cart_Activity.this, "Số lượng không phù hợp", Toast.LENGTH_SHORT).show();
                                        quantity = Integer.valueOf(holder.edtQC.getText().toString())-1;
                                    }
                                    else {
                                        holder.edtQC.setText(String.valueOf(quantity));
                                        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                cartRef.child(String.valueOf(cart.getId())).child("quantity").setValue(quantity);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });

                // Nút xóa khỏi giỏ hàng
                holder.btnDeleteFromCart.setOnClickListener(v -> {
                    String cartId = String.valueOf(cart.getId());
                    cartRef.child(cartId).removeValue().addOnSuccessListener(aVoid -> {
                        // Xóa sản phẩm khỏi danh sách cartItems
                        cartItems.removeIf(item -> String.valueOf(item.getId()).equals(String.valueOf(cartId)));

                        // Tính lại tổng tiền
                        total = 0;
                        for (Cart cart1 : cartItems) {
                            double discountedPrice1 = cart1.getPrice() - (cart1.getPrice() * cart1.getDiscount() / 100);
                            total += discountedPrice1 * cart1.getQuantity();
                        }
                        totalPrice.setText("Tổng tiền: " + decimalFormat.format((int) total) + " ₫");

                        // Cập nhật RecyclerView (nếu cần)
                        firebaseRecyclerAdapter.notifyDataSetChanged(); // Hoặc gọi updateRecyclerView() nếu bạn dùng hàm riêng
                    }).addOnFailureListener(e -> {
                        Log.e("Firebase", "Lỗi khi xóa sản phẩm: " + e.getMessage());
                        Toast.makeText(Cart_Activity.this, "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            @NonNull
            @Override
            public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
                return new CartAdapter.CartViewHolder(view);
            }
        };

        recyclerViewCart.setLayoutManager(new GridLayoutManager(Cart_Activity.this, 1));
        recyclerViewCart.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Khi bấm nút Back, quay về Activity trước đó
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}