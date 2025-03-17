package com.example.tinkdrao;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

import javax.annotation.Nullable;

public class DrinkDetailActivity2 extends AppCompatActivity {
    private TextView tvId, tvName, tvPrice, tvDiscount, tvDrinkType, tvPurchaseCount, tvQuantity, tvUnit, tvCreatedAt;
    private ImageView ivDrink;
    private EditText etName, etPrice, etDiscount, etQuantity;
    private Spinner spinnerDrinkType, spinnerUnit;
    private Button btnEdit, btnDelete, btnSave, btnCancel;
    private LinearLayout layoutEditButtons;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Drink drink;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_detail2);

        getSupportActionBar().setTitle("Chi tiết sản phẩm");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Drink");
        storageReference = FirebaseStorage.getInstance().getReference("drink_images");

        // Khởi tạo views
        tvId = findViewById(R.id.tv_detail_id);
        tvName = findViewById(R.id.tv_detail_name);
        tvPrice = findViewById(R.id.tv_price_options);
        tvDiscount = findViewById(R.id.tv_discount);
        tvDrinkType = findViewById(R.id.tv_drink_type);
        tvPurchaseCount = findViewById(R.id.tv_purchase_count);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvUnit = findViewById(R.id.tv_unit);
        tvCreatedAt = findViewById(R.id.tv_created_at);
        ivDrink = findViewById(R.id.iv_detail_drink);
        etName = findViewById(R.id.et_detail_name);
        etPrice = findViewById(R.id.et_price);
        etDiscount = findViewById(R.id.et_discount);
        etQuantity = findViewById(R.id.et_quantity);
        spinnerDrinkType = findViewById(R.id.spinner_drink_type);
        spinnerUnit = findViewById(R.id.spinner_unit);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        layoutEditButtons = findViewById(R.id.layout_edit_buttons);

        // Lấy dữ liệu từ Intent
        drink = (Drink) getIntent().getSerializableExtra("drink");
        if (drink != null) {
            displayDrinkDetails();
        }

        // Load dữ liệu cho Spinner (dùng khi chỉnh sửa)
        loadDrinkTypeOptions();
        loadUnitOptions();

        // Sự kiện nhấn nút Sửa
        btnEdit.setOnClickListener(v -> switchToEditMode());

        // Sự kiện nhấn nút Xóa
        btnDelete.setOnClickListener(v -> confirmDelete());

        // Sự kiện nhấn nút Lưu
        btnSave.setOnClickListener(v -> confirmSave());

        // Sự kiện nhấn nút Hủy
        btnCancel.setOnClickListener(v -> switchToViewMode());

        // Sự kiện nhấn chọn ảnh
        ivDrink.setOnClickListener(v -> {
            if (isEditMode) {
                openImageChooser();
            }
        });
    }

    private void displayDrinkDetails() {
        tvId.setText("ID: " + drink.getId());
        tvName.setText(drink.getName());
        tvPrice.setText(String.format("Giá: %.2f", drink.getPrice()));
        tvDiscount.setText(String.format("Giảm giá: %.2f", drink.getDiscount()));
        tvDrinkType.setText("Loại: " + drink.getDrinkType());
        tvPurchaseCount.setText("Số lượng đã bán: " + drink.getPurchaseCount());
        tvQuantity.setText("Số lượng: " + drink.getQuantity());
        tvUnit.setText("Đơn vị: " + drink.getUnit());
        tvCreatedAt.setText("Ngày tạo: " + drink.getCreatedAt());

        com.bumptech.glide.Glide.with(this)
                .load(drink.getImageUrl())
                .into(ivDrink);
    }

    private void switchToEditMode() {
        isEditMode = true;

        // Ẩn các TextView và hiện các EditText/Spinner
        tvName.setVisibility(View.GONE);
        tvPrice.setVisibility(View.GONE);
        tvDiscount.setVisibility(View.GONE);
        tvDrinkType.setVisibility(View.GONE);
        tvQuantity.setVisibility(View.GONE);
        tvUnit.setVisibility(View.GONE);

        etName.setVisibility(View.VISIBLE);
        etPrice.setVisibility(View.VISIBLE);
        etDiscount.setVisibility(View.VISIBLE);
        spinnerDrinkType.setVisibility(View.VISIBLE);
        etQuantity.setVisibility(View.VISIBLE);
        spinnerUnit.setVisibility(View.VISIBLE);

        // Điền dữ liệu hiện tại vào các trường chỉnh sửa
        etName.setText(drink.getName());
        etPrice.setText(String.valueOf(drink.getPrice()));
        etDiscount.setText(String.valueOf(drink.getDiscount()));
        etQuantity.setText(String.valueOf(drink.getQuantity()));

        // Chọn giá trị hiện tại cho Spinner
        ArrayAdapter<String> drinkTypeAdapter = (ArrayAdapter<String>) spinnerDrinkType.getAdapter();
        if (drinkTypeAdapter != null && drinkTypeAdapter.getPosition(drink.getDrinkType()) != -1) {
            spinnerDrinkType.setSelection(drinkTypeAdapter.getPosition(drink.getDrinkType()));
        }
        ArrayAdapter<String> unitAdapter = (ArrayAdapter<String>) spinnerUnit.getAdapter();
        if (unitAdapter != null && unitAdapter.getPosition(drink.getUnit()) != -1) {
            spinnerUnit.setSelection(unitAdapter.getPosition(drink.getUnit()));
        }

        // Ẩn nút Sửa/Xóa, hiện LinearLayout chứa Lưu/Hủy
        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        layoutEditButtons.setVisibility(View.VISIBLE);

        ivDrink.setClickable(true);
        ivDrink.setFocusable(true);
    }

    private void switchToViewMode() {
        isEditMode = false;

        // Hiện các TextView và ẩn các EditText/Spinner
        tvName.setVisibility(View.VISIBLE);
        tvPrice.setVisibility(View.VISIBLE);
        tvDiscount.setVisibility(View.VISIBLE);
        tvDrinkType.setVisibility(View.VISIBLE);
        tvQuantity.setVisibility(View.VISIBLE);
        tvUnit.setVisibility(View.VISIBLE);

        etName.setVisibility(View.GONE);
        etPrice.setVisibility(View.GONE);
        etDiscount.setVisibility(View.GONE);
        spinnerDrinkType.setVisibility(View.GONE);
        etQuantity.setVisibility(View.GONE);
        spinnerUnit.setVisibility(View.GONE);

        // Hiện nút Sửa/Xóa, ẩn LinearLayout chứa Lưu/Hủy
        btnEdit.setVisibility(View.VISIBLE);
        btnDelete.setVisibility(View.VISIBLE);
        layoutEditButtons.setVisibility(View.GONE);

        ivDrink.setClickable(false);
        ivDrink.setFocusable(false);

        // Cập nhật lại giao diện với dữ liệu ban đầu
        displayDrinkDetails();
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
            ivDrink.setImageURI(imageUri);
        }
    }

    private void loadDrinkTypeOptions() {
        DatabaseReference drinkTypeReference = FirebaseDatabase.getInstance().getReference("TinkDrao").child("DrinkType");
        List<String> drinkTypes = new ArrayList<>();
        drinkTypeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                drinkTypes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String type = snapshot.getValue(String.class);
                    if (type != null) {
                        drinkTypes.add(type);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(DrinkDetailActivity2.this,
                        android.R.layout.simple_spinner_item, drinkTypes);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDrinkType.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DrinkDetailActivity2.this, "Lỗi tải loại nước: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUnitOptions() {
        DatabaseReference unitReference = FirebaseDatabase.getInstance().getReference("UnitData");
        List<String> units = new ArrayList<>();
        unitReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                units.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String unit = snapshot.getValue(String.class);
                    if (unit != null) {
                        units.add(unit);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(DrinkDetailActivity2.this,
                        android.R.layout.simple_spinner_item, units);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUnit.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DrinkDetailActivity2.this, "Lỗi tải đơn vị: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmSave() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận lưu")
                .setMessage("Bạn có chắc chắn muốn lưu thay đổi không?")
                .setPositiveButton("Lưu", (dialog, which) -> saveChanges())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String discountStr = etDiscount.getText().toString().trim();
        String drinkType = spinnerDrinkType.getSelectedItem() != null ? spinnerDrinkType.getSelectedItem().toString() : "";
        String unit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : drink.getUnit();

        // Kiểm tra dữ liệu đầu vào cơ bản
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantityStr) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(discountStr)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
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
        final int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0 || quantity > 999999999) {
                Toast.makeText(this, "Số lượng phải từ 0 đến 999999999", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ràng buộc cho price: số thực, ≥ 0, ≤ 999999999, tối đa 2 chữ số thập phân, không rỗng
        final double price;
        try {
            price = Double.parseDouble(priceStr);
            // Kiểm tra số chữ số thập phân
            String[] priceParts = priceStr.split("\\.");
            if (priceParts.length > 1 && priceParts[1].length() > 2) {
                Toast.makeText(this, "Giá chỉ được tối đa 2 chữ số thập phân", Toast.LENGTH_SHORT).show();
                return;
            }
            if (price < 0 || price > 999999999) {
                Toast.makeText(this, "Giá phải từ 0 đến 999999999", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá phải là số thực hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ràng buộc cho discount: số thực, ≥ 0, ≤ 100, tối đa 2 chữ số thập phân, không rỗng, mặc định 0 nếu không có chỉnh sửa
        final double discount;
        if (TextUtils.isEmpty(discountStr)) {
            discount = 0.0; // Giá trị mặc định
        } else {
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

        // Cập nhật dữ liệu
        drink.setName(name);
        drink.setPrice(price); // Sửa: Sử dụng biến price (double) thay vì priceStr (String)
        drink.setDiscount(discount); // Sửa: Sử dụng biến discount (double) thay vì discountStr (String)
        drink.setDrinkType(drinkType);
        drink.setQuantity(quantity); // Sửa: Sử dụng biến quantity (int) thay vì quantityStr (String)
        drink.setUnit(unit);
        final String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        drink.setCreatedAt(createdAt);

        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(String.valueOf(drink.getId()) + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            final String imageUrl = uri.toString();
                            drink.setImageUrl(imageUrl);
                            updateDrinkInFirebase();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateDrinkInFirebase();
        }
    }

    private void updateDrinkInFirebase() {
        databaseReference.child(String.valueOf(drink.getId())).setValue(drink)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật đồ uống thành công", Toast.LENGTH_SHORT).show();
                    switchToViewMode();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đồ uống này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteDrink())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteDrink() {
        databaseReference.child(String.valueOf(drink.getId())).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xóa đồ uống thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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