package com.example.tinkdrao;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tinkdrao.model.Drink;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PromotionDetailActivity extends AppCompatActivity {
    private TextView tvDiscount;
    private ListView lvDrinks;
    private Button btnSave;
    private DatabaseReference drinksRef, promotionsRef;
    private Promotion promotion;
    private List<Drink> drinkList;
    private List<Drink> selectedDrinks;
    private DrinkCheckboxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_detail);

        // Khởi tạo Firebase
        drinksRef = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Drink");
        promotionsRef = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Promotions");

        // Khởi tạo views
        tvDiscount = findViewById(R.id.tv_discount);
        lvDrinks = findViewById(R.id.lv_drinks);
        btnSave = findViewById(R.id.btn_save);

        // Lấy dữ liệu từ Intent
        promotion = (Promotion) getIntent().getSerializableExtra("promotion");
        if (promotion == null) {
            Toast.makeText(this, "Không tìm thấy chương trình khuyến mãi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị thông tin khuyến mãi
        tvDiscount.setText(String.format("Giảm giá: %.2f", promotion.getDiscount()));

        // Khởi tạo danh sách sản phẩm
        drinkList = new ArrayList<>();
        selectedDrinks = new ArrayList<>();
        adapter = new DrinkCheckboxAdapter(this, drinkList, selectedDrinks);
        lvDrinks.setAdapter(adapter);

        // Load danh sách sản phẩm được áp dụng khuyến mãi
        loadDrinks();

        // Sự kiện nhấn lưu
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadDrinks() {
        drinksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                drinkList.clear();
                selectedDrinks.clear();
                if (!dataSnapshot.exists()) {
                    Toast.makeText(PromotionDetailActivity.this, "Không có sản phẩm nào trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Drink drink = snapshot.getValue(Drink.class);
                        if (drink != null && promotion.getDrinkIds().contains(snapshot.getKey())) {
                            drink.setId(Long.parseLong(snapshot.getKey()));
                            drinkList.add(drink);
                            selectedDrinks.add(drink); // Ban đầu, tất cả sản phẩm đều được chọn
                        }
                    } catch (Exception e) {
                        Toast.makeText(PromotionDetailActivity.this, "Lỗi dữ liệu sản phẩm: " + snapshot.getKey(), Toast.LENGTH_SHORT).show();
                    }
                }

                if (drinkList.isEmpty()) {
                    Toast.makeText(PromotionDetailActivity.this, "Không có sản phẩm nào trong chương trình khuyến mãi này", Toast.LENGTH_SHORT).show();
                    finish();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PromotionDetailActivity.this, "Lỗi tải danh sách sản phẩm: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        List<String> updatedDrinkIds = new ArrayList<>();
        for (Drink drink : selectedDrinks) {
            updatedDrinkIds.add(String.valueOf(drink.getId()));
        }

        for (Drink drink : drinkList) {
            if (!selectedDrinks.contains(drink)) {
                drinksRef.child(String.valueOf(drink.getId())).child("discount").setValue(0.0);
            }
        }

        if (updatedDrinkIds.isEmpty()) {
            promotionsRef.child(promotion.getPromotionId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PromotionDetailActivity.this, "Đã xóa chương trình khuyến mãi", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PromotionDetailActivity.this, "Lỗi xóa khuyến mãi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            promotion.setDrinkIds(updatedDrinkIds);
            promotionsRef.child(promotion.getPromotionId()).setValue(promotion)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PromotionDetailActivity.this, "Cập nhật khuyến mãi thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PromotionDetailActivity.this, "Lỗi cập nhật khuyến mãi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}