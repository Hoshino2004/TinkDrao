package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PromotionListActivity extends AppCompatActivity {
    private ListView lvPromotions;
    private Button btnCreatePromotion;
    private DatabaseReference promotionsRef;
    private List<Promotion> promotionList;
    private ArrayAdapter<Promotion> promotionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_list);

        // Khởi tạo Firebase
        promotionsRef = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Promotions");

        // Khởi tạo views
        lvPromotions = findViewById(R.id.lv_promotions);
        btnCreatePromotion = findViewById(R.id.btn_create_promotion);

        // Khởi tạo danh sách khuyến mãi
        promotionList = new ArrayList<>();
        promotionAdapter = new ArrayAdapter<Promotion>(this, android.R.layout.simple_list_item_1, promotionList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                Promotion promotion = getItem(position);
                textView.setText(String.format("Giảm giá: %.2f", promotion.getDiscount()));
                return view;
            }
        };
        lvPromotions.setAdapter(promotionAdapter);

        // Load danh sách khuyến mãi
        loadPromotions();

        // Sự kiện nhấn tạo khuyến mãi mới
        btnCreatePromotion.setOnClickListener(v -> {
            Intent intent = new Intent(PromotionListActivity.this, CreatePromotionActivity.class);
            startActivity(intent);
        });

        // Sự kiện nhấn vào một chương trình khuyến mãi để xem chi tiết
        lvPromotions.setOnItemClickListener((parent, view, position, id) -> {
            Promotion selectedPromotion = promotionList.get(position);
            Intent intent = new Intent(PromotionListActivity.this, PromotionDetailActivity.class);
            intent.putExtra("promotion", selectedPromotion);
            startActivity(intent);
        });

        // Sự kiện nhấn giữ để xóa chương trình khuyến mãi
        lvPromotions.setOnItemLongClickListener((parent, view, position, id) -> {
            Promotion selectedPromotion = promotionList.get(position);
            new androidx.appcompat.app.AlertDialog.Builder(PromotionListActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa chương trình khuyến mãi này? Discount của tất cả sản phẩm liên quan sẽ được đặt về 0.")
                    .setPositiveButton("Xóa", (dialog, which) -> deletePromotion(selectedPromotion))
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });
    }

    private void loadPromotions() {
        promotionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                promotionList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotion promotion = snapshot.getValue(Promotion.class);
                    if (promotion != null) {
                        promotion.setPromotionId(snapshot.getKey());
                        promotionList.add(promotion);
                    }
                }
                promotionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PromotionListActivity.this, "Lỗi tải danh sách khuyến mãi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePromotion(Promotion promotion) {
        DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Drink");
        for (String drinkId : promotion.getDrinkIds()) {
            drinksRef.child(drinkId).child("discount").setValue(0.0);
        }

        promotionsRef.child(promotion.getPromotionId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PromotionListActivity.this, "Xóa khuyến mãi thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PromotionListActivity.this, "Lỗi xóa khuyến mãi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromotions();
    }
}