package com.example.tinkdrao;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.model.Cart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private Button btnAction, btnFavorite, btnIQ, btnDQ;
    private int quantity;
    private EditText edtQ;
    private DatabaseReference databaseReference, favoritesReference, cartRef;
    private DecimalFormat decimalFormat;
    private ImageButton btnBack;
    private Drink currentDrink;
    private FirebaseUser mUser;
    private ValueEventListener drinkDataListener; // Lưu listener để hủy sau
    private boolean isActivityDestroyed = false;
    private static String phoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_detail);

        // Khởi tạo DecimalFormat
        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);

        // Khởi tạo views
        initializeViews();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // Lấy drinkId từ Intent
        long drinkId = getIntent().getLongExtra("id", 0);

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Drink").child(String.valueOf(drinkId));

        // Nút tăng giảm số lượng
        btnDQ.setOnClickListener(view -> {
            if (edtQ.getText().toString().equals("1")) {
                Toast.makeText(DrinkDetailActivity.this, "Số lượng bắt buộc phải từ 1 trở lên", Toast.LENGTH_SHORT).show();
            } else {
                quantity = Integer.parseInt(edtQ.getText().toString()) - 1;
                edtQ.setText(String.valueOf(quantity));
            }
        });

        btnIQ.setOnClickListener(view -> {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Drink drink = snapshot.getValue(Drink.class);
                        quantity = Integer.parseInt(edtQ.getText().toString()) + 1;
                        if (quantity > drink.getQuantity()) {
                            Toast.makeText(DrinkDetailActivity.this, "Số lượng không phù hợp", Toast.LENGTH_SHORT).show();
                            quantity = Integer.parseInt(edtQ.getText().toString()) - 1;
                        } else {
                            edtQ.setText(String.valueOf(quantity));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        // EditText số lượng
        edtQ.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    edtQ.setText("1");
                    return;
                }
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Drink drink = snapshot.getValue(Drink.class);
                            if (Integer.parseInt(editable.toString()) > drink.getQuantity()) {
                                edtQ.setText(String.valueOf(drink.getQuantity()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        if(mUser!=null)
        {
            // Check hiện diện đồ uống trong danh sách yêu thích
            checkDrinkFav();
        }

        // Lắng nghe thay đổi dữ liệu realtime
        loadDrinkData();

        // Thêm sự kiện click cho nút
        btnAction.setOnClickListener(v -> {
            if(mUser!=null)
            {
                addToCart(mUser.getUid());
            }
            else {
                if(phoneNo!=null)
                {
                    addToCart(phoneNo);
                }
                else {
                    showPhoneInputDialog();
                }
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            if(mUser!=null)
            {
                if (currentDrink != null) {
                    addToFavorites(currentDrink);
                } else {
                    Toast.makeText(this, "Đang tải dữ liệu, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "Vui lòng đăng nhập để sử dụng chức năng này!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPhoneInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập số điện thoại");

        // Tạo EditText để nhập số điện thoại
        final EditText input = new EditText(this);
        input.setBackgroundResource(android.R.drawable.edit_text);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        // Nút Xác nhận
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String phoneNumber = input.getText().toString().trim();
            if (phoneNumber.startsWith("0") && phoneNumber.length() == 10) {
                phoneNo = phoneNumber;
                Intent intent = new Intent(DrinkDetailActivity.this, MainActivity.class);
                intent.putExtra("phoneNo",phoneNo);
                startActivity(intent);
                // handlePhoneNumber(phoneNumber); // Gọi hàm xử lý ở đây nếu cần
            } else {
                Toast.makeText(DrinkDetailActivity.this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        // Hiển thị dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Kiểm tra khi nhập
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phoneNumber = s.toString().trim();
                if (!phoneNumber.startsWith("0")) {
                    input.setError("Số điện thoại phải bắt đầu bằng số 0!");
                } else if (phoneNumber.length() != 10) {
                    input.setError("Số điện thoại phải đủ 10 chữ số!");
                } else {
                    input.setError(null); // Xóa lỗi nếu hợp lệ
                }
            }
        });
    }

    private void addToCart(String uid) {
        cartRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Cart/" + uid);
        String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));
        cartRef.child(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Cart existingCart = snapshot.getValue(Cart.class);
                    if (existingCart != null) {
                        int newQuantity = existingCart.getQuantity() + Integer.parseInt(edtQ.getText().toString());
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Drink drink1 = snapshot.getValue(Drink.class);
                                    if (newQuantity > drink1.getQuantity()) {
                                        Toast.makeText(DrinkDetailActivity.this, "Không thể thêm vào giỏ hàng vượt quá số lượng", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(DrinkDetailActivity.this, "Đã cập nhật số lượng trong giỏ hàng: " + newQuantity, Toast.LENGTH_SHORT).show();
                                        cartRef.child(drinkId).child("quantity").setValue(newQuantity);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                } else {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Drink drinkCrt = snapshot.getValue(Drink.class);
                                Cart newCart = new Cart(Long.valueOf(drinkId), drinkCrt.getImageUrl(), drinkCrt.getName(), drinkCrt.getPrice(), drinkCrt.getDiscount(), drinkCrt.getDrinkType(), Integer.parseInt(edtQ.getText().toString()), drinkCrt.getUnit());
                                cartRef.child(drinkId).setValue(newCart);
                                Toast.makeText(DrinkDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void checkDrinkFav() {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        favoritesReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Favorites/" + mUser.getUid());
        String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));
        favoritesReference.child(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isActivityDestroyed) { // Kiểm tra trước khi cập nhật UI
                    if (dataSnapshot.exists()) {
                        btnFavorite.setText("Đã yêu thích");
                        btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, android.R.color.darker_gray));
                    } else {
                        btnFavorite.setText("Thêm vào yêu thích");
                        btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, R.color.orange));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isActivityDestroyed) {
                    Toast.makeText(DrinkDetailActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
        btnDQ = findViewById(R.id.btnDecrease);
        btnIQ = findViewById(R.id.btnIncrease);
        edtQ = findViewById(R.id.edtQuantity);
        edtQ.setText("1");
    }

    private void loadDrinkData() {
        drinkDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isActivityDestroyed) { // Kiểm tra trạng thái Activity
                    Drink drink = dataSnapshot.getValue(Drink.class);
                    if (drink != null) {
                        currentDrink = drink;
                        updateUI(drink);
                    } else {
                        Toast.makeText(DrinkDetailActivity.this, "Không tìm thấy thông tin đồ uống", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isActivityDestroyed) {
                    Toast.makeText(DrinkDetailActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        databaseReference.addValueEventListener(drinkDataListener);
    }

    private void updateUI(Drink drink) {
        if (!isDestroyed() && !isFinishing()) { // Kiểm tra trước khi dùng Glide
            Glide.with(this)
                    .load(drink.getImageUrl())
                    .placeholder(R.drawable.loading)
                    .into(ivDrinkImage);
        }

        tvName.setText(drink.getName());
        if (drink.getDiscount() > 0) {
            double discountedPrice = drink.getPrice() * (100 - drink.getDiscount()) / 100;
            tvPrice.setText("Giá: " + decimalFormat.format((int) discountedPrice) + "₫");
            tvDiscount.setText("Giảm: " + decimalFormat.format((int) drink.getDiscount()) + "%");
        } else {
            tvPrice.setText("Giá: " + decimalFormat.format((int) drink.getPrice()) + "₫");
        }
        tvDrinkType.setText("Loại: " + drink.getDrinkType());
        tvPurchaseCount.setText("Đã bán: " + drink.getPurchaseCount());
        tvQuantity.setText("Tồn kho: " + drink.getQuantity());
        tvUnit.setText("Đơn vị: " + drink.getUnit());

        if (drink.getQuantity() == 0) {
            btnAction.setVisibility(View.GONE);
        }
    }

    private void addToFavorites(Drink drink) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        favoritesReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Favorites/" + mUser.getUid());
        String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));
        favoritesReference.child(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isActivityDestroyed) { // Kiểm tra trạng thái Activity
                    if (dataSnapshot.exists()) {
                        favoritesReference.child(drinkId).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(DrinkDetailActivity.this, "Đã xóa " + drink.getName() + " khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                                    btnFavorite.setText("Thêm vào yêu thích");
                                    btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, R.color.orange));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DrinkDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        favoritesReference.child(drinkId).setValue(drink)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(DrinkDetailActivity.this, "Đã thêm " + drink.getName() + " vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                                    btnFavorite.setText("Đã yêu thích");
                                    btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, android.R.color.darker_gray));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DrinkDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isActivityDestroyed) {
                    Toast.makeText(DrinkDetailActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        if (drinkDataListener != null) {
            databaseReference.removeEventListener(drinkDataListener); // Hủy listener
        }
    }
}