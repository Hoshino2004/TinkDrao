package com.example.tinkdrao;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tinkdrao.adapter.DrinkAdapter;
import com.example.tinkdrao.adapter.ImageSliderAdapter;
import com.example.tinkdrao.model.Drink;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private List<String> imageUrls;
    private DatabaseReference databaseReference;
    private DatabaseReference drinkRef;

    private RecyclerView drinksRecyclerView;
    private RecyclerView drinksRecyclerView2;
    private DrinkAdapter drinkAdapter;
    private List<Drink> drinkList;
    private TextView tvViewAll;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao");
        drinkRef = databaseReference.child("Drink");

        tvViewAll = findViewById(R.id.tvViewAll);
        tvViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchDrinkActivity.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        setUpImageSlider();
        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        // Khởi tạo RecyclerView
        drinksRecyclerView = findViewById(R.id.drinksRecyclerView);
        drinksRecyclerView2 = findViewById(R.id.drinksRecyclerView2);
        drinksRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        drinksRecyclerView2.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        // Khởi tạo list và adapter
        drinkList = new ArrayList<>();
        drinkAdapter = new DrinkAdapter(this, drinkList);
        drinksRecyclerView.setAdapter(drinkAdapter);
        drinksRecyclerView2.setAdapter(drinkAdapter);

        // Lấy dữ liệu từ Firebase
        fetchDrinksFromFirebase();
    }

    private void fetchDrinksFromFirebase() {
        drinkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                drinkList.clear(); // Xóa dữ liệu cũ trước khi cập nhật

                // Duyệt qua tất cả các node con trong "drinks"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Drink drink = snapshot.getValue(Drink.class);
                    if (drink != null) {
                        drinkList.add(drink);
                    }
                }

                // Cập nhật adapter khi có dữ liệu mới
                drinkAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
                // Bạn có thể thêm Toast hoặc Log để debug
            }
        });
    }

    private void setUpImageSlider() {
        viewPager = findViewById(R.id.viewPager);
        imageUrls = new ArrayList<>();
        adapter = new ImageSliderAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);

        drinkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                imageUrls.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Drink drink = dataSnapshot.getValue(Drink.class);
                    if (drink != null) {
                        imageUrls.add(drink.getImageUrl());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mnUser) {
            if (mUser == null) {
                Intent intent = new Intent(this, Login_Activity.class);
                startActivity(intent);
                finish();
            } else {
                if (!mUser.isEmailVerified()) {
                    mAuth.signOut(); // Đăng xuất người dùng nếu họ chưa xác thực
                    startActivity(new Intent(this, Login_Activity.class));
                    finish();
                } else {
                    Intent intent = new Intent(MainActivity.this, User_Activity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}