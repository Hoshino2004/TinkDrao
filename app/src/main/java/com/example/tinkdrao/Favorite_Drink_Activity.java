package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class Favorite_Drink_Activity extends AppCompatActivity {

    TextView totalFav;
    RecyclerView recyclerViewFav;
    FirebaseUser mUser;
    LinearLayoutManager linearLayoutManager;
    DatabaseReference favRef;
    FirebaseRecyclerAdapter<Drink, DrinkAdapter.DrinkViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Drink> options;
    long maxid = 0;
    private DecimalFormat decimalFormat; // Định dạng giá tiền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_drink);

        getSupportActionBar().setTitle("Danh sách nước yêu thích");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        decimalFormat = new DecimalFormat("#,###"); // Dấu chấm: 12.000
        decimalFormat.setDecimalSeparatorAlwaysShown(false); // Không hiển thị phần thập phân

        totalFav = findViewById(R.id.totalFav);
        recyclerViewFav = findViewById(R.id.recyclerViewFav);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        favRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Favorites").child(mUser.getUid());
        showData();
    }

    private void showData() {
        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) maxid = snapshot.getChildrenCount();
                totalFav.setText("Tổng số bài thích " + "(" + maxid + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        options = new FirebaseRecyclerOptions.Builder<Drink>()
                .setQuery(favRef, Drink.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Drink, DrinkAdapter.DrinkViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull DrinkAdapter.DrinkViewHolder holder, int position, @NonNull Drink drink) {
                // Gán dữ liệu từ Drink vào ViewHolder
                holder.drinkName.setText(drink.getName());

                Glide.with(holder.itemView.getContext())
                        .load(drink.getImageUrl())
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.loading)
                        .into(holder.drinkImage);

                // Hiển thị giá gốc nếu có giảm giá
                if (drink.getDiscount() > 0) {
                    double discountedPrice = drink.getPrice() * (100 - drink.getDiscount()) / 100;
                    holder.drinkDiscountedPercent.setText("-" + decimalFormat.format(drink.getDiscount()) + "%");
                    holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
                    holder.drinkOriginalPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
                    holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
                    holder.drinkDiscountedPercent.setVisibility(View.VISIBLE);
                } else {
                    holder.drinkOriginalPrice.setVisibility(View.GONE);
                    holder.drinkDiscountedPercent.setVisibility(View.GONE);
                    holder.drinkDiscountedPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
                }

                // Click để chuyển sang DrinkDetailActivity
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), DrinkDetailActivity.class);
                    intent.putExtra("id", drink.getId()); // Truyền ID của sản phẩm
                    v.getContext().startActivity(intent);
                });
            }

            @NonNull
            @Override
            public DrinkAdapter.DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink, parent, false);
                return new DrinkAdapter.DrinkViewHolder(view);
            }
        };
        recyclerViewFav.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewFav.setAdapter(firebaseRecyclerAdapter);
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