package com.example.tinkdrao;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.tinkdrao.adapter.NewDrinkAdapter;
import com.example.tinkdrao.model.Drink;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;

    private Handler handler = new Handler();
    private Runnable runnable;
    private ImageSliderAdapter adapter;
    private List<String> imageUrls;
    private DatabaseReference databaseReference;
    private DatabaseReference drinkRef;

    private RecyclerView drinksRecyclerView;
    private RecyclerView drinksRecyclerView2;
    private DrinkAdapter drinkAdapter;
    private NewDrinkAdapter newDrinkAdapter;
    private List<Drink> drinkList;
    private List<Drink> newDrinkList;
    private TextView tvViewAll;
    static String phoneNumber;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumber = getIntent().getStringExtra("phoneNo");
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

        setUpRecyclerView();
        setUpImageSlider();

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
        newDrinkList = new ArrayList<>();

        drinkAdapter = new DrinkAdapter(this, drinkList);
        newDrinkAdapter = new NewDrinkAdapter(this, newDrinkList);

        drinksRecyclerView.setAdapter(drinkAdapter);
        drinksRecyclerView2.setAdapter(newDrinkAdapter);

        // Lấy dữ liệu từ Firebase
        fetchDrinksFromFirebase();
    }

    private void fetchDrinksFromFirebase() {
        drinkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Drink> tempList = new ArrayList<>();

                // Duyệt qua tất cả các node con trong "drinks"
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Drink drink = snapshot.getValue(Drink.class);
                    if (drink != null) {
                        tempList.add(drink);
                    }
                }

                // Cập nhật adapter khi có dữ liệu mới
                drinkAdapter.updateList(tempList);
                newDrinkAdapter.updateList(tempList);
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
        adapter = new ImageSliderAdapter(this, drinkList);
        viewPager.setAdapter(adapter);

        // Tạo Runnable để chuyển trang
        runnable = new Runnable() {
            @Override
            public void run() {
                if (viewPager.getAdapter() == null) return;

                int currentItem = viewPager.getCurrentItem();
                int itemCount = viewPager.getAdapter().getItemCount();

                if (itemCount != 0) {
                    // Chuyển sang trang tiếp theo (vòng lại nếu hết)
                    int nextItem = (currentItem + 1) % itemCount;
                    viewPager.setCurrentItem(nextItem, true);
                }

                // Đặt lại handler (chỉ một runnable chạy duy nhất)
                handler.postDelayed(this, 5000);
            }
        };

        // Chạy auto scroll lần đầu
        handler.postDelayed(runnable, 5000);

        // Dừng auto scroll khi người dùng tương tác
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacks(runnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 5000);
                }
            }
        });

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
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
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
//            if (mUser == null) {
//                Intent intent = new Intent(this, Login_Activity.class);
//                startActivity(intent);
//                finish();
//            } else {
//                if (!mUser.isEmailVerified()) {
//                    mAuth.signOut(); // Đăng xuất người dùng nếu họ chưa xác thực
//                    startActivity(new Intent(this, Login_Activity.class));
//                    finish();
//                } else if (phoneNumber!=null) {
//                    Intent intent = new Intent(MainActivity.this, User_Activity.class);
//                    intent.putExtra("phoneNo",phoneNumber);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    Intent intent = new Intent(MainActivity.this, User_Activity.class);
//                    startActivity(intent);
//                    finish();
//                }
//            }
            if(mUser!=null)
            {
                if (!mUser.isEmailVerified()) {
                    mAuth.signOut(); // Đăng xuất người dùng nếu họ chưa xác thực
                    startActivity(new Intent(this, Login_Activity.class));
                    finish();
                }
                else {
                    Intent intent = new Intent(MainActivity.this, User_Activity.class);
                    startActivity(intent);
                    finish();
                }
            } else if (mUser==null && phoneNumber!=null) {
                Intent intent = new Intent(MainActivity.this, User_Activity.class);
                intent.putExtra("phoneNo",phoneNumber);
                startActivity(intent);
                finish();
            }
            else {
                startActivity(new Intent(MainActivity.this,Login_Activity.class));
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}