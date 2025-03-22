package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView; // Thêm import này

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.adapter.SearchDrinkAdapter;
import com.example.tinkdrao.model.Drink;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class SearchDrinkActivity extends AppCompatActivity {

    private RecyclerView searchRecyclerView;
    private SearchDrinkAdapter searchAdapter;
    private List<Drink> drinkList;
    private androidx.appcompat.widget.SearchView searchView;
    private DatabaseReference databaseReference;
    private DatabaseReference drinkRef;
    private RangeSlider rangeSliderPrice;
    private TextView btnPriceFilter;
    private EditText etMinPrice, etMaxPrice;
    private Button btnApply;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Cho EditText
    private DecimalFormat priceFormat; // Cho "triệu" với 2 chữ số thập phân
    private boolean isUpdating = false;
    private float minPrice = 0;
    private float maxPrice = 500000;
    private TextView spinnerDrinkType, badge;

    ArrayList<String> drinkTypeList;

    private List<String> selectedDrinkType = new ArrayList<>();
    private DatabaseReference drinkTypeRef;
    private int dem = 0;
    private FloatingActionButton fabCart;
    private FirebaseUser mUser;
    private FrameLayout btnCartSearch;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_drink);

        fabCart = findViewById(R.id.fabCart);
        badge = findViewById(R.id.badge);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        phoneNumber = getIntent().getStringExtra("phoneNo");
        btnCartSearch = findViewById(R.id.btnCartSearch);

        fabCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchDrinkActivity.this, Cart_Activity.class));
            }
        });

        getSupportActionBar().setTitle("Danh sách sản phẩm");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao");
        drinkRef = databaseReference.child("Drink");
        drinkTypeRef = databaseReference.child("DrinkType");

        if(mUser!=null && phoneNumber == null)
        {
            checkCart(mUser.getUid());
        }
        else if(mUser==null && phoneNumber != null)
        {
            checkCart(phoneNumber);
        }
        else {
            btnCartSearch.setVisibility(View.GONE);
        }


        setUpSearchDrink();
        setupSpinners();

        // Lấy dữ liệu từ Realtime Database
        drinkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Drink> tempList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Drink drink = dataSnapshot.getValue(Drink.class);
                    if (drink != null) {
                        tempList.add(drink);
                    }
                }
                // Cập nhật adapter với danh sách mới
                searchAdapter.updateList(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });

        decimalFormat.setDecimalSeparatorAlwaysShown(false);

        btnPriceFilter = findViewById(R.id.btnFilterPrice);

        btnPriceFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPriceFilterDialog();
            }
        });
    }

    private void checkCart(String uid) {
        databaseReference.child("Cart").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    dem = (int)snapshot.getChildrenCount();
                }
                badge.setText(String.valueOf(dem));
            }
            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    private void setupSpinners() {
        spinnerDrinkType = findViewById(R.id.spinnerDrinkType);
        drinkTypeList = new ArrayList<>();

        fetchDrinkTypeForSpinners();

        // Sự kiện click cho TextView quận (multi-select)
        spinnerDrinkType.setOnClickListener(v -> showMultiSelectDialog(drinkTypeList, "Chọn loại", selectedItems -> {
            selectedDrinkType = selectedItems;
            spinnerDrinkType.setText(selectedDrinkType.isEmpty() ? "Chọn loại ▼" : android.text.TextUtils.join(", ", selectedDrinkType));
            searchAdapter.filter(searchView.getQuery().toString(), minPrice, maxPrice, selectedDrinkType);
        }));


    }

    private interface OnMultiSelectListener {
        void onItemsSelected(List<String> selectedItems);
    }

    private void showMultiSelectDialog(List<String> options, String title, OnMultiSelectListener listener) {
        boolean[] checkedItems = new boolean[options.size()];
        String[] itemsArray = options.toArray(new String[0]);

        // Tạo dialog với danh sách checkbox
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setMultiChoiceItems(itemsArray, checkedItems, (dialog, which, isChecked) -> {
            // Cập nhật trạng thái của mục vừa thay đổi
            checkedItems[which] = isChecked;
        });

        // Xử lý khi người dùng bấm "OK"
        builder.setPositiveButton("OK", (dialog, which) -> {
            List<String> selectedItems = new ArrayList<>();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedItems.add(options.get(i));
                }
            }

            // Gọi listener với danh sách các item đã chọn
            listener.onItemsSelected(selectedItems);
        });

        // Hiển thị dialog
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void fetchDrinkTypeForSpinners() {
        drinkTypeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                drinkTypeList.clear();
                for (DataSnapshot mydata : snapshot.getChildren()) {
                    drinkTypeList.add(mydata.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void showPriceFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_price_filter, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        rangeSliderPrice = dialogView.findViewById(R.id.rangeSliderPrice);
        etMinPrice = dialogView.findViewById(R.id.etMinPrice);
        etMaxPrice = dialogView.findViewById(R.id.etMaxPrice);
        btnApply = dialogView.findViewById(R.id.btnApply);

        rangeSliderPrice.setValueFrom(0f);
        rangeSliderPrice.setValueTo(500000f);
        rangeSliderPrice.setStepSize(10000f);
        rangeSliderPrice.setValues(0f, 500000f);

        updateUIFromSlider(0f, 500000f);

        rangeSliderPrice.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(RangeSlider slider, float value, boolean fromUser) {
                if (!isUpdating) {
                    List<Float> values = slider.getValues();
                    float minValue = values.get(0);
                    float maxValue = values.get(1);
                    updateUIFromSlider(minValue, maxValue);
                }
            }
        });

        etMinPrice.addTextChangedListener(new TextWatcher() {
            private String previousText = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousText = s.toString();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    formatEditText(etMinPrice, previousText);
                    updateSliderFromEditText();
                }
            }
        });

        etMaxPrice.addTextChangedListener(new TextWatcher() {
            private String previousText = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousText = s.toString();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    formatEditText(etMaxPrice, previousText);
                    updateSliderFromEditText();
                }
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy giá trị từ EditText và loại bỏ dấu phẩy
                String minPriceStr = etMinPrice.getText().toString().replace(",", "").replace(".", "");
                String maxPriceStr = etMaxPrice.getText().toString().replace(",", "").replace(".", "");

                try {
                    minPrice = minPriceStr.isEmpty() ? 0f : Float.parseFloat(minPriceStr);
                    maxPrice = maxPriceStr.isEmpty() ? 500000f : Float.parseFloat(maxPriceStr);
                } catch (NumberFormatException e) {
                    minPrice = 0f;
                    maxPrice = 500000f;
                }

                // Chuyển sang đơn vị triệu (giữ nguyên số thập phân)
                float minPriceMillion = minPrice / 1000f;
                float maxPriceMillion = maxPrice / 1000f;

                // Định dạng với 2 chữ số thập phân
                String formattedMinPrice = decimalFormat.format(minPriceMillion);
                String formattedMaxPrice = decimalFormat.format(maxPriceMillion);

                // Set text cho tvPriceFilter
                if (btnPriceFilter != null) {
                    String priceRange = formattedMinPrice + " - " + formattedMaxPrice + " nghìn";
                    btnPriceFilter.setText(priceRange);
                }

                if (dialog != null)
                    dialog.dismiss();

                searchAdapter.filter(searchView.getQuery().toString(), minPrice, maxPrice, selectedDrinkType);
            }
        });
    }

    private void updateUIFromSlider(float minValue, float maxValue) {
        isUpdating = true;
        String newMin = decimalFormat.format((long) minValue);
        String newMax = decimalFormat.format((long) maxValue);
        if (!etMinPrice.getText().toString().equals(newMin)) etMinPrice.setText(newMin);
        if (!etMaxPrice.getText().toString().equals(newMax)) etMaxPrice.setText(newMax);
        isUpdating = false;
    }

    private void formatEditText(EditText editText, String previousText) {
        String input = editText.getText().toString().replace(",", "");
        if (input.isEmpty()) {
            editText.setText("0");
            editText.setSelection(1);
            return;
        }

        try {
            long value = Long.parseLong(input);
            String formatted = decimalFormat.format(value);
            if (!formatted.equals(previousText)) {
                int cursorPosition = editText.getSelectionStart();
                editText.setText(formatted);
                int newLength = formatted.length();
                int oldLength = previousText.length();
                int newPosition = cursorPosition + (newLength - oldLength);
                if (newPosition >= 0 && newPosition <= newLength) {
                    editText.setSelection(newPosition);
                } else {
                    editText.setSelection(newLength);
                }
            }
        } catch (NumberFormatException e) {
            editText.setText(previousText);
        }
    }

    private void updateSliderFromEditText() {
        try {
            isUpdating = true;
            String minStr = etMinPrice.getText().toString().replace(",", "");
            String maxStr = etMaxPrice.getText().toString().replace(",", "");

            float minValue = minStr.isEmpty() ? 0f : Float.parseFloat(minStr);
            float maxValue = maxStr.isEmpty() ? 500000f : Float.parseFloat(maxStr);

            minValue = Math.max(0f, Math.min(minValue, 500000f));
            maxValue = Math.max(minValue, Math.min(maxValue, 500000f));
            minValue = Math.round(minValue / 10000f) * 10000f;
            maxValue = Math.round(maxValue / 10000f) * 10000f;

            rangeSliderPrice.setValues(minValue, maxValue);

        } catch (NumberFormatException e) {
            rangeSliderPrice.setValues(0f, 500000f);
            if (!etMinPrice.getText().toString().equals("0")) etMinPrice.setText("0");
            if (!etMaxPrice.getText().toString().equals("500,000")) etMaxPrice.setText("500,000");
        } finally {
            isUpdating = false;
        }
    }

    private void setUpSearchDrink() {
        // Khởi tạo RecyclerView
        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách và adapter
        drinkList = new ArrayList<>();
        searchAdapter = new SearchDrinkAdapter(this, drinkList);
        searchRecyclerView.setAdapter(searchAdapter);

        // Thanh tìm kiếm
        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Tìm kiếm nước...");
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchAdapter.filter(newText, minPrice, maxPrice, selectedDrinkType); // Lọc danh sách khi nhập
                return true;
            }
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