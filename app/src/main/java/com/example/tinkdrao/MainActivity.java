package com.example.tinkdrao;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tinkdrao.adapter.DrinkAdapter;
import com.example.tinkdrao.adapter.ImageSliderAdapter;
import com.example.tinkdrao.model.Drink;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao");
        drinkRef = databaseReference.child("Drink");

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


}