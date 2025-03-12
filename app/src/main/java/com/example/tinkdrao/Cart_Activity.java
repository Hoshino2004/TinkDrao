package com.example.tinkdrao;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.adapter.DrinkAdapter;
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
    FirebaseRecyclerAdapter<Drink, DrinkAdapter.DrinkViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Drink> options;
    long total = 0;
    String drinkId;
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
                if (snapshot.exists()) {
                    // Lấy danh sách ID đồ uống trong giỏ hàng
                    List<String> drinkIds = new ArrayList<>();
                    for (DataSnapshot cartSnapshot : snapshot.getChildren()) {
                        drinkId = cartSnapshot.getKey(); // ID của đồ uống
                        drinkIds.add(drinkId);
                        drinkRef.child(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot drinkSnapshot) {
                                if (drinkSnapshot.exists()) {
                                    Drink drink = drinkSnapshot.getValue(Drink.class);
                                    if (drink != null) {
                                        total += drink.getPrice() - ((drink.getPrice() * drink.getDiscount()) / 100);
                                        totalPrice.setText("Tổng tiền: " + total + " ₫");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                    // Nếu giỏ hàng không có gì thì return
                    if (drinkIds.isEmpty()) {
                        return;
                    }
                    // Truy vấn chỉ lấy những đồ uống có trong danh sách giỏ hàng
                    Query query = drinkRef.orderByKey().startAt(drinkIds.get(0)).endAt(drinkIds.get(drinkIds.size() - 1));
                    options = new FirebaseRecyclerOptions.Builder<Drink>()
                            .setQuery(query, Drink.class)
                            .build();
                    firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Drink, DrinkAdapter.DrinkViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull DrinkAdapter.DrinkViewHolder holder, int position, @NonNull Drink drink) {
                            holder.drinkName.setText(drink.getName());
                            Glide.with(holder.itemView.getContext())
                                    .load(drink.getImageUrl())
                                    .placeholder(R.drawable.loading)
                                    .error(R.drawable.loading)
                                    .into(holder.drinkImage);
                            if (drink.getDiscount() > 0) {
                                double discountedPrice = drink.getPrice() * (100 - drink.getDiscount()) / 100;
                                holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
                                holder.drinkOriginalPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
                                holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
                            } else {
                                holder.drinkOriginalPrice.setVisibility(View.GONE);
                                holder.drinkDiscountedPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
                            }
                        }
                        @NonNull
                        @Override
                        public DrinkAdapter.DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
                            return new DrinkAdapter.DrinkViewHolder(view);
                        }
                    };
                    recyclerViewCart.setLayoutManager(new GridLayoutManager(Cart_Activity.this, 2));
                    recyclerViewCart.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu giỏ hàng: " + error.getMessage());
            }
        });
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