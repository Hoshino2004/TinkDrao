package com.example.tinkdrao;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.adapter.CartAdapter;
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
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Cart_Activity extends AppCompatActivity {
    TextView totalPrice;
    RecyclerView recyclerViewCart;
    CheckBox checkSelectAll;
    Button buttonPay;
    FirebaseUser mUser;
    LinearLayoutManager linearLayoutManager;
    DatabaseReference cartRef, drinkRef;
    FirebaseRecyclerAdapter<Cart, CartAdapter.CartViewHolder> firebaseRecyclerAdapter;
    long total = 0;
    int quantity = 0;
    private DecimalFormat decimalFormat;
    private boolean isMultiSelectMode = false;
    private List<Cart> selectedItems = new ArrayList<>();
    private List<Cart> cartItems = new ArrayList<>();
    static String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        phoneNumber = getIntent().getStringExtra("phoneNo");

        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);

        getSupportActionBar().setTitle("Giỏ hàng");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        totalPrice = findViewById(R.id.totalPrice);
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        checkSelectAll = findViewById(R.id.checkSelectAll);
        buttonPay = findViewById(R.id.buttonPay);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        totalPrice.setText("0₫");
        buttonPay.setText("Kiểm tra");

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        if(mUser!=null)
        {
            cartRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Cart").child(mUser.getUid());
        }
        else {
            cartRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Cart").child(phoneNumber);
        }
        drinkRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Drink");

        // Sự kiện khi nhấn CheckBox "Tất cả"
        checkSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                isMultiSelectMode = true;
                selectedItems.clear();
                selectedItems.addAll(cartItems);
                updateSelectedTotalPrice();
                updateActionBarTitle();
                updateButtonPayText();
                if (firebaseRecyclerAdapter != null) {
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                }
            } else {
                isMultiSelectMode = false;
                selectedItems.clear();
                updateSelectedTotalPrice();
                updateActionBarTitle();
                updateButtonPayText();
                if (firebaseRecyclerAdapter != null) {
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });

        // Sự kiện khi nhấn buttonPay
        buttonPay.setOnClickListener(v -> {
            if (isMultiSelectMode && !selectedItems.isEmpty()) {
                Intent intent = new Intent(Cart_Activity.this, Order_Activity.class);
                intent.putExtra("selectedItems", new ArrayList<>(selectedItems));
                intent.putExtra("phoneNo",phoneNumber);
                startActivity(intent);
            } else {
                Toast.makeText(Cart_Activity.this, "Vui lòng chọn ít nhất một sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            }
        });

        showData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Làm mới dữ liệu khi quay lại từ Order_Activity
        selectedItems.clear();
        isMultiSelectMode = false;
        checkSelectAll.setChecked(false);
        updateActionBarTitle();
        updateSelectedTotalPrice();
        updateButtonPayText();
        showData(); // Cập nhật lại danh sách giỏ hàng
    }

    private void showData() {
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    totalPrice.setText("0₫");
                    updateRecyclerView(new ArrayList<>());
                    return;
                }

                cartItems.clear();
                for (DataSnapshot cartSnapshot : snapshot.getChildren()) {
                    String drinkId = cartSnapshot.getKey();
                    Cart cart = cartSnapshot.getValue(Cart.class);
                    if (cart != null) {
                        cart.setId(Long.valueOf(drinkId));
                        cartItems.add(cart);
                    }
                }

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
        this.cartItems = cartItems;

        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cart, CartAdapter.CartViewHolder>(
                new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartRef, Cart.class)
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
                        if (snapshot.exists()) {
                            Drink drink = snapshot.getValue(Drink.class);
                            holder.drinkQuantity.setText("Tồn kho: " + drink.getQuantity());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                holder.edtQC.setText(String.valueOf(cart.getQuantity()));

                if (selectedItems.contains(cart)) {
                    holder.itemView.setBackgroundColor(Color.LTGRAY);
                } else {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                }

                holder.itemView.setOnLongClickListener(v -> {
                    if (!isMultiSelectMode) {
                        isMultiSelectMode = true;
                        selectedItems.add(cart);
                        holder.itemView.setBackgroundColor(Color.LTGRAY);
                        notifyDataSetChanged();
                        updateSelectedTotalPrice();
                        updateActionBarTitle();
                        updateButtonPayText();
                    }
                    return true;
                });

                holder.itemView.setOnClickListener(v -> {
                    if (isMultiSelectMode) {
                        if (selectedItems.contains(cart)) {
                            selectedItems.remove(cart);
                            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                            if (checkSelectAll.isChecked()) {
                                checkSelectAll.setChecked(false);
                            }
                        } else if (!checkSelectAll.isChecked()) {
                            selectedItems.add(cart);
                            holder.itemView.setBackgroundColor(Color.LTGRAY);
                            if (selectedItems.size() == cartItems.size()) {
                                checkSelectAll.setChecked(true);
                            }
                        }
                        if (selectedItems.isEmpty()) {
                            isMultiSelectMode = false;
                        }
                        notifyDataSetChanged();
                        updateSelectedTotalPrice();
                        updateActionBarTitle();
                        updateButtonPayText();
                    }
                });

                holder.btnDQC.setOnClickListener(view -> {
                    if (isMultiSelectMode && selectedItems.contains(cart)) {
                        Toast.makeText(Cart_Activity.this, "Không thể chỉnh sửa khi sản phẩm đã được chọn", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (holder.edtQC.getText().toString().equals("1")) {
                        String cartId = String.valueOf(cart.getId());
                        cartRef.child(cartId).removeValue().addOnSuccessListener(aVoid -> {
                            cartItems.removeIf(item -> String.valueOf(item.getId()).equals(cartId));
                            selectedItems.removeIf(item -> String.valueOf(item.getId()).equals(cartId));
                            if (selectedItems.isEmpty()) {
                                isMultiSelectMode = false;
                                checkSelectAll.setChecked(false);
                            }
                            notifyDataSetChanged();
                            updateSelectedTotalPrice();
                            updateActionBarTitle();
                            updateButtonPayText();
                        }).addOnFailureListener(e -> {
                            Log.e("Firebase", "Lỗi khi xóa sản phẩm: " + e.getMessage());
                            Toast.makeText(Cart_Activity.this, "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        quantity = Integer.parseInt(holder.edtQC.getText().toString()) - 1;
                        cartRef.child(String.valueOf(cart.getId())).child("quantity").setValue(quantity)
                                .addOnSuccessListener(aVoid -> {
                                    cart.setQuantity(quantity);
                                    holder.edtQC.setText(String.valueOf(quantity));
                                    updateSelectedTotalPrice();
                                });
                    }
                });

                holder.btnIQC.setOnClickListener(view -> {
                    if (isMultiSelectMode && selectedItems.contains(cart)) {
                        Toast.makeText(Cart_Activity.this, "Không thể chỉnh sửa khi sản phẩm đã được chọn", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    drinkRef.child(String.valueOf(cart.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Drink drink = snapshot.getValue(Drink.class);
                                quantity = Integer.parseInt(holder.edtQC.getText().toString()) + 1;
                                if (quantity > drink.getQuantity()) {
                                    Toast.makeText(Cart_Activity.this, "Số lượng không phù hợp", Toast.LENGTH_SHORT).show();
                                } else {
                                    cartRef.child(String.valueOf(cart.getId())).child("quantity").setValue(quantity)
                                            .addOnSuccessListener(aVoid -> {
                                                cart.setQuantity(quantity);
                                                holder.edtQC.setText(String.valueOf(quantity));
                                                updateSelectedTotalPrice();
                                            });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                });

                holder.btnDeleteFromCart.setOnClickListener(v -> {
                    if (isMultiSelectMode && selectedItems.contains(cart)) {
                        Toast.makeText(Cart_Activity.this, "Không thể xóa khi sản phẩm đã được chọn", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String cartId = String.valueOf(cart.getId());
                    cartRef.child(cartId).removeValue().addOnSuccessListener(aVoid -> {
                        cartItems.removeIf(item -> String.valueOf(item.getId()).equals(cartId));
                        selectedItems.removeIf(item -> String.valueOf(item.getId()).equals(cartId));
                        if (selectedItems.isEmpty()) {
                            isMultiSelectMode = false;
                            checkSelectAll.setChecked(false);
                        }
                        notifyDataSetChanged();
                        updateSelectedTotalPrice();
                        updateActionBarTitle();
                        updateButtonPayText();
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

    private void updateSelectedTotalPrice() {
        if (isMultiSelectMode && !selectedItems.isEmpty()) {
            total = 0;
            for (Cart cart : selectedItems) {
                double discountedPrice = cart.getPrice() * (100 - cart.getDiscount()) / 100;
                total += discountedPrice * cart.getQuantity();
            }
            totalPrice.setText(decimalFormat.format((int) total) + "₫");
            totalPrice.setTextColor(Color.parseColor("#FF3300"));
        } else {
            totalPrice.setText("0₫");
            totalPrice.setTextColor(Color.parseColor("#000000"));
        }
    }

    private void updateActionBarTitle() {
        if (isMultiSelectMode) {
            getSupportActionBar().setTitle("Đã chọn: " + selectedItems.size());
        } else {
            getSupportActionBar().setTitle("Giỏ hàng");
        }
    }

    private void updateButtonPayText() {
        if (isMultiSelectMode && !selectedItems.isEmpty()) {
            buttonPay.setText("Thanh toán (" + selectedItems.size() + ")");
        } else {
            buttonPay.setText("Kiểm tra");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isMultiSelectMode) {
                isMultiSelectMode = false;
                selectedItems.clear();
                checkSelectAll.setChecked(false);
                updateActionBarTitle();
                updateSelectedTotalPrice();
                updateButtonPayText();
                firebaseRecyclerAdapter.notifyDataSetChanged();
                return true;
            }
            // Chuyển về UserActivity
            Intent intent = new Intent(Cart_Activity.this, User_Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Đóng Cart_Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}