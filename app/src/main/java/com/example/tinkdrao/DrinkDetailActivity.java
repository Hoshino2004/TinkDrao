package com.example.tinkdrao;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private DatabaseReference databaseReference,favoritesReference, cartRef;
    private DecimalFormat decimalFormat;
    private ImageButton btnBack;
    private Drink currentDrink;
    private FirebaseUser mUser;

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

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Drink").child(String.valueOf(drinkId));

        // Nút tăng giảm số lượng
        btnDQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtQ.getText().toString().equals("1"))
                {
                    Toast.makeText(DrinkDetailActivity.this, "Số lượng bắt buộc phải từ 1 trở lên", Toast.LENGTH_SHORT).show();
                }
                else {
                    quantity = Integer.valueOf(edtQ.getText().toString())-1;
                    edtQ.setText(String.valueOf(quantity));
                }
            }
        });

        btnIQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            Drink drink = snapshot.getValue(Drink.class);
                            quantity = Integer.valueOf(edtQ.getText().toString())+1;
                            if(quantity>drink.getQuantity())
                            {
                                Toast.makeText(DrinkDetailActivity.this, "Số lượng không phù hợp", Toast.LENGTH_SHORT).show();
                                quantity = Integer.valueOf(edtQ.getText().toString())-1;
                            }
                            else {
                                edtQ.setText(String.valueOf(quantity));
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
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
                    return; // Nếu trống thì không kiểm tra
                }
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            Drink drink = snapshot.getValue(Drink.class);
                            if(Integer.parseInt(editable.toString()) > drink.getQuantity())
                            {
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

        // Check hiện diện đồ uống
        checkDrinkFav();

        // Lắng nghe thay đổi dữ liệu realtime
        loadDrinkData();

        // Thêm sự kiện click cho nút
        btnAction.setOnClickListener(v -> {
            addToCart(currentDrink);
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

    private void addToCart(Drink drink) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        cartRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Cart/"+mUser.getUid());
        String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));
        cartRef.child(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Nếu sản phẩm đã có trong giỏ hàng, cập nhật số lượng
                    Cart existingCart = snapshot.getValue(Cart.class);
                    if (existingCart != null) {
                        int newQuantity = existingCart.getQuantity() + Integer.valueOf(edtQ.getText().toString());
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    Drink drink1 = snapshot.getValue(Drink.class);
                                    if(newQuantity > drink1.getQuantity())
                                    {
                                        Toast.makeText(DrinkDetailActivity.this, "Không thể thêm vào giỏ hàng vượt quá số lượng", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(DrinkDetailActivity.this, "Đã cập nhật số lượng trong giỏ hàng: "+newQuantity, Toast.LENGTH_SHORT).show();
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
                            if(snapshot.exists())
                            {
                                Drink drinkCrt = snapshot.getValue(Drink.class);
                                // Nếu sản phẩm chưa có, thêm mới vào giỏ hàng
                                Cart newCart = new Cart(Long.valueOf(drinkId), drinkCrt.getImageUrl(), drinkCrt.getName(), drinkCrt.getPrice(), drinkCrt.getDiscount(), drinkCrt.getDrinkType(),Integer.valueOf(edtQ.getText().toString()), drinkCrt.getUnit());
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

        favoritesReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Favorites/"+mUser.getUid());

        String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));

        favoritesReference.child(drinkId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    btnFavorite.setText("Đã yêu thích");
                    btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, android.R.color.darker_gray));
                } else {
                    btnFavorite.setText("Thêm vào yêu thích");
                    btnFavorite.setBackgroundTintList(ContextCompat.getColorStateList(DrinkDetailActivity.this, R.color.orange));
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

        //Quantity
        btnDQ = findViewById(R.id.btnDecrease);
        btnIQ = findViewById(R.id.btnIncrease);
        edtQ = findViewById(R.id.edtQuantity);
        edtQ.setText("1");
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
        tvQuantity.setText("Tồn kho: " + drink.getQuantity());
        tvUnit.setText("Đơn vị: " + drink.getUnit());
    }
        private void addToFavorites(Drink drink) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();

            favoritesReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Favorites/"+mUser.getUid());

            String drinkId = String.valueOf(getIntent().getLongExtra("id", 0));

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