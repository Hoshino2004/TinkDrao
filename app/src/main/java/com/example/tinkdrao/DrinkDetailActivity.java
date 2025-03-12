package com.example.tinkdrao;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.tinkdrao.model.Drink;

import java.text.DecimalFormat;

public class DrinkDetailActivity extends AppCompatActivity {

    private ImageView ivDrinkImage;
    private TextView tvName, tvPrice, tvDiscount, tvDrinkType, tvPurchaseCount, tvQuantity, tvUnit;
    private Button btnAction, btnFavorite;
    private DatabaseReference databaseReference,favoritesReference;
    private DecimalFormat decimalFormat;
    private ImageButton btnBack;
    private Drink currentDrink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_detail);

        // Khởi tạo DecimalFormat
        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);

        // Khởi tạo views
        initializeViews();

        // Lấy drinkId từ Intent
        long drinkId = getIntent().getLongExtra("id",0);
//        if (drinkId == null) {
//            Toast.makeText(this, "Không tìm thấy ID đồ uống", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Drink").child(String.valueOf(drinkId));

        // Lắng nghe thay đổi dữ liệu realtime
        loadDrinkData();

        // Thêm sự kiện click cho nút
        btnAction.setOnClickListener(v -> {
            // Xử lý sự kiện click (ví dụ: thêm vào giỏ hàng)
            Toast.makeText(this, "Đã thêm " + tvName.getText() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            // Bạn có thể thêm logic để lưu vào giỏ hàng ở đây
        });
        // Thêm sự kiện click cho nút Back
        btnBack.setOnClickListener(v -> {
            finish(); // Đóng Activity hiện tại và quay lại trang trước
        });
        // Thêm sự kiện click cho nút Favorite
        btnFavorite.setOnClickListener(v -> {
            if (currentDrink != null) {
                addToFavorites(currentDrink);
            } else {
                Toast.makeText(this, "Đang tải dữ liệu, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        ivDrinkImage = findViewById(R.id.ivDrinkImage);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvDrinkType = findViewById(R.id.tvDrinkType);
        tvPurchaseCount = findViewById(R.id.tvPurchaseCount);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvUnit = findViewById(R.id.tvUnit);
        btnAction = findViewById(R.id.btnAction);
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
    }

    private void loadDrinkData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Drink drink = dataSnapshot.getValue(Drink.class);
                if (drink != null) {
                    currentDrink = drink;
                    updateUI(drink);
                } else {
                    Toast.makeText(DrinkDetailActivity.this,
                            "Không tìm thấy thông tin đồ uống",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DrinkDetailActivity.this,
                        "Lỗi: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Drink drink) {
        // Load ảnh bằng Glide
        Glide.with(this)
                .load(drink.getImageUrl())
                .placeholder(R.drawable.loading) // Thay bằng drawable placeholder của bạn// Thay bằng drawable error của bạn
                .into(ivDrinkImage);

        tvName.setText(drink.getName());

        // Hiển thị giá và giá giảm nếu có
        if (drink.getDiscount() > 0) {
            double discountedPrice = drink.getPrice() * (100 - drink.getDiscount()) / 100;
            tvPrice.setText("Giá: " + decimalFormat.format((int) discountedPrice) + "₫");
            tvDiscount.setText("Giá gốc: " + decimalFormat.format((int) drink.getDiscount()) + "₫");
        } else {
            tvPrice.setText("Giá: " + decimalFormat.format((int) drink.getPrice()) + "₫");
            tvDiscount.setText("Giảm giá: Không có");
        }

        tvDrinkType.setText("Loại: " + drink.getDrinkType());
        tvPurchaseCount.setText("Đã bán: " + drink.getPurchaseCount());
        tvQuantity.setText("Số lượng: " + drink.getQuantity());
        tvUnit.setText("Đơn vị: " + drink.getUnit());
    }
//    private void addToFavorites(Drink drink) {
//        // Sử dụng drinkId làm key trong nhánh Favorites
//        String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));

        // Lưu dữ liệu vào Firebase
//        favoritesReference.child(drinkId).setValue(drink)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(DrinkDetailActivity.this,
//                            "Đã thêm " + drink.getName() + " vào danh sách yêu thích",
//                            Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(DrinkDetailActivity.this,
//                            "Lỗi khi thêm vào yêu thích: " + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                });
//    }
        private void addToFavorites(Drink drink) {
            if (favoritesReference == null) {
                try {
                    favoritesReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Favorites");
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi khởi tạo Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));
            if (drinkId.equals("0")) {
                Toast.makeText(this, "ID đồ uống không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            favoritesReference.child(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        favoritesReference.child(drinkId).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(DrinkDetailActivity.this,
                                            "Đã xóa " + drink.getName() + " khỏi danh sách yêu thích",
                                            Toast.LENGTH_SHORT).show();
                                    btnFavorite.setText("Thêm vào yêu thích");
                                    btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, R.color.orange));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DrinkDetailActivity.this,
                                            "Lỗi: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        favoritesReference.child(drinkId).setValue(drink)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(DrinkDetailActivity.this,
                                            "Đã thêm " + drink.getName() + " vào danh sách yêu thích",
                                            Toast.LENGTH_SHORT).show();
                                    btnFavorite.setText("Đã yêu thích");
                                    btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, android.R.color.darker_gray));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DrinkDetailActivity.this,
                                            "Lỗi: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(DrinkDetailActivity.this,
                            "Lỗi: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
}