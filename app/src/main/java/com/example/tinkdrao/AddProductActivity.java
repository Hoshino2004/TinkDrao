package com.example.tinkdrao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tinkdrao.model.Drink;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public class AddProductActivity extends AppCompatActivity {
    private EditText etName, etQuantity, etPrice, etDiscount;
    private Spinner spinnerWaterType, spinnerUnit;
    private Button btnAddProduct;
    private ImageView ivProductImage;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        getSupportActionBar().setTitle("Thêm sản phẩm");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Drink");
        storageReference = FirebaseStorage.getInstance().getReference("drink_images");

        // Khởi tạo views
        etName = findViewById(R.id.et_product_name);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_default_option_price);
        etDiscount = findViewById(R.id.et_discount);
        spinnerWaterType = findViewById(R.id.spinner_water_type);
        spinnerUnit = findViewById(R.id.spinner_unit);
        btnAddProduct = findViewById(R.id.btn_add_product);
        ivProductImage = findViewById(R.id.iv_product_image);

        // Load dữ liệu vào Spinner
        loadDrinkOptions();
        loadUnitOptions();

        // Sự kiện nhấn chọn ảnh
        ivProductImage.setOnClickListener(view -> openImageChooser());

        // Sự kiện thêm sản phẩm
        btnAddProduct.setOnClickListener(v -> addProduct());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProductImage.setImageURI(imageUri);
        }
    }

    private void loadDrinkOptions() {
        DatabaseReference drinkOptionsReference = FirebaseDatabase.getInstance().getReference("TinkDrao").child("DrinkType");
        List<String> drinkOptions = new ArrayList<>();
        drinkOptions.add("--Tất cả--"); // Thêm "--Tất cả--" làm mặc định
        drinkOptionsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                drinkOptions.clear();
                drinkOptions.add("--Tất cả--"); // Đảm bảo "--Tất cả--" luôn là lựa chọn đầu tiên
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String option = snapshot.getValue(String.class);
                    if (option != null && !option.equals("--Tất cả--")) {
                        drinkOptions.add(option);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddProductActivity.this,
                        android.R.layout.simple_spinner_item, drinkOptions);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerWaterType.setAdapter(adapter);
                spinnerWaterType.setSelection(0); // Đặt "--Tất cả--" làm mặc định
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddProductActivity.this, "Lỗi tải loại nước: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUnitOptions() {
        DatabaseReference unitOptionsReference = FirebaseDatabase.getInstance().getReference("UnitData");
        List<String> unitOptions = new ArrayList<>();
        unitOptionsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unitOptions.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String unit = snapshot.getValue(String.class);
                    if (unit != null) {
                        unitOptions.add(unit);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddProductActivity.this,
                        android.R.layout.simple_spinner_item, unitOptions);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUnit.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddProductActivity.this, "Lỗi tải đơn vị: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateDrinkId() {
        Random random = new Random();
        int randomNumber = 10000000 + random.nextInt(90000000);
        return String.valueOf(randomNumber);
    }

    private void addProduct() {
        String name = etName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String discountStr = etDiscount.getText().toString().trim();
        String drinkType = spinnerWaterType.getSelectedItem() != null ? spinnerWaterType.getSelectedItem().toString() : "";
        String unit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : "Chai";

        // Kiểm tra dữ liệu đầu vào
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantityStr) || TextUtils.isEmpty(priceStr)  || imageUri == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin và chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        // Không cho lưu nếu loại nước là "--Tất cả--"
        if (drinkType.equals("--Tất cả--")) {
            Toast.makeText(this, "Không thể lưu khi loại nước là '--Tất cả--'", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(drinkType)) {
            Toast.makeText(this, "Vui lòng chọn loại nước", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ràng buộc cho name: độ dài (min 2, max 50), không rỗng
        if (name.length() < 2 || name.length() > 50) {
            Toast.makeText(this, "Tên sản phẩm phải từ 2 đến 50 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ràng buộc cho quantity: số nguyên, ≥ 0, ≤ 999999999, không rỗng, không ký tự chữ
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity < 0 || quantity > 999999999) {
                Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ràng buộc cho price: số thực, ≥ 0, ≤ 999999999, tối đa 2 chữ số thập phân, không rỗng
        try {
            double price = Double.parseDouble(priceStr);
            // Kiểm tra số chữ số thập phân
            String[] priceParts = priceStr.split("\\.");
            if (priceParts.length > 1 && priceParts[1].length() > 2) {
                Toast.makeText(this, "Giá chỉ được tối đa 2 chữ số thập phân", Toast.LENGTH_SHORT).show();
                return;
            }
            if (price < 0 || price > 999999999) {
                Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá phải là số thực hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ràng buộc cho discount: số thực, ≥ 0, ≤ 100, tối đa 2 chữ số thập phân, không rỗng, mặc định 0 nếu không có chỉnh sửa
        double discount = 0.0; // Giá trị mặc định
        if (!TextUtils.isEmpty(discountStr)) {
            try {
                discount = Double.parseDouble(discountStr);
                // Kiểm tra số chữ số thập phân
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
        }

        try {
            final long id = Long.parseLong(generateDrinkId()); // Sửa 1
            final int quantity = Integer.parseInt(quantityStr); // Sửa 2
            final double price = Double.parseDouble(priceStr); // Sửa 3
            final int purchaseCount = 0; // Sửa 5
            final String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // Sửa 6

            // Kiểm tra giá trị hợp lệ (đã được xử lý ở trên)
            final double finalDiscount = discount; // Sửa 4, gán giá trị cuối cùng sau khi kiểm tra

            StorageReference fileReference = storageReference.child(id + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            final String imageUrl = uri.toString(); // Sửa 7
                            Drink drink = new Drink(id, imageUrl, name, price, finalDiscount, drinkType, purchaseCount, quantity, unit, createdAt);
                            databaseReference.child(String.valueOf(id)).setValue(drink)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Thêm đồ uống thành công", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi định dạng số", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi không xác định: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
}

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Khi bấm nút Back, quay về Activity trước đó
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}