package com.example.tinkdrao;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class CreatePromotionActivity extends AppCompatActivity {
    private ListView lvDrinks;
    private EditText etDiscount;
    private Button btnSavePromotion;
    private TextView tvEmptyMessage;
    private DatabaseReference drinksRef, promotionsRef;
    private List<Drink> drinkList;
    private List<Drink> selectedDrinks;
    private DrinkCheckboxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_promotion);

        // Khởi tạo Firebase
        drinksRef = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Drink");
        promotionsRef = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Promotions");

        // Khởi tạo views
        lvDrinks = findViewById(R.id.lv_drinks);
        etDiscount = findViewById(R.id.et_discount);
        btnSavePromotion = findViewById(R.id.btn_save_promotion);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        // Khởi tạo danh sách sản phẩm
        drinkList = new ArrayList<>();
        selectedDrinks = new ArrayList<>();
        adapter = new DrinkCheckboxAdapter(this, drinkList, selectedDrinks);
        lvDrinks.setAdapter(adapter);

        // Load danh sách sản phẩm có discount = 0
        loadDrinks();

        // Sự kiện nhấn lưu khuyến mãi
        btnSavePromotion.setOnClickListener(v -> savePromotion());
    }

    private void loadDrinks() {
        drinksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                drinkList.clear();
                if (!dataSnapshot.exists()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    tvEmptyMessage.setText("Không có sản phẩm nào trong cơ sở dữ liệu");
                    lvDrinks.setVisibility(View.GONE);
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Drink drink = snapshot.getValue(Drink.class);
                        if (drink != null) {
                            drink.setId(Long.parseLong(snapshot.getKey()));
                            if (drink.getDiscount() == 0.0) {
                                drinkList.add(drink);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(CreatePromotionActivity.this, "Lỗi dữ liệu sản phẩm: " + snapshot.getKey(), Toast.LENGTH_SHORT).show();
                    }
                }

                if (drinkList.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    tvEmptyMessage.setText("Không có sản phẩm nào có discount = 0 để áp dụng khuyến mãi");
                    lvDrinks.setVisibility(View.GONE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                    lvDrinks.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CreatePromotionActivity.this, "Lỗi tải danh sách sản phẩm: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                tvEmptyMessage.setVisibility(View.VISIBLE);
                tvEmptyMessage.setText("Lỗi tải dữ liệu: " + databaseError.getMessage());
                lvDrinks.setVisibility(View.GONE);
            }
        });
    }

    private void savePromotion() {
        String discountStr = etDiscount.getText().toString().trim();

        if (TextUtils.isEmpty(discountStr)) {
            Toast.makeText(this, "Vui lòng nhập giá trị giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDrinks.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        double discount;
        try {
            discount = Double.parseDouble(discountStr);
            String[] discountParts = discountStr.split("\\.");
            if (discountParts.length > 1 && discountParts[1].length() > 2) {
                Toast.makeText(this, "Giảm giá chỉ được tối đa 2 chữ số thập phân", Toast.LENGTH_SHORT).show();
                return;
            }
            if (discount < 0 || discount > 100) {
                Toast.makeText(this, "Giảm giá phải từ 0 đến 100", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giảm giá phải là số thực hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String promotionId = promotionsRef.push().getKey();
        List<String> drinkIds = new ArrayList<>();
        for (Drink drink : selectedDrinks) {
            drinkIds.add(String.valueOf(drink.getId()));
        }

        Promotion promotion = new Promotion(promotionId, discount, drinkIds);
        final double finalDiscount = discount;
        promotionsRef.child(promotionId).setValue(promotion)
                .addOnSuccessListener(aVoid -> {
                    for (Drink drink : selectedDrinks) {
                        drinksRef.child(String.valueOf(drink.getId())).child("discount").setValue(finalDiscount);
                    }
                    Toast.makeText(CreatePromotionActivity.this, "Tạo khuyến mãi thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePromotionActivity.this, "Lỗi tạo khuyến mãi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}