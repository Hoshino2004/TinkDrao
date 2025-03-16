package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.model.Drink;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DrinkListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private DrinkAdapter2 adapter;
    private FloatingActionButton fabAddDrink, fabAddDiscount;
    private SearchView searchView;

    private List<Drink> drinkList = new ArrayList<>(); // Lưu danh sách gốc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);

        getSupportActionBar().setTitle("Quản lý sản phẩm");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view);
        fabAddDrink = findViewById(R.id.fab_add_drink);
        fabAddDiscount = findViewById(R.id.fab_add_discount);
        searchView = findViewById(R.id.search_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DrinkAdapter2();
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Drink");
        loadDrinks();

        fabAddDrink.setOnClickListener(v -> {
            Intent intent = new Intent(DrinkListActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        fabAddDiscount.setOnClickListener(v -> {
            Intent intent = new Intent(DrinkListActivity.this, PromotionListActivity.class);
            startActivity(intent);
        });

        // Xử lý tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDrinks(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterDrinks(newText);
                return false;
            }
        });
    }

    private void loadDrinks() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                drinkList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Drink drink = data.getValue(Drink.class);
                    if (drink != null) {
                        drinkList.add(drink);
                    }
                }
                adapter.setDrinks(drinkList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DrinkListActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterDrinks(String query) {
        List<Drink> filteredList = new ArrayList<>();
        for (Drink drink : drinkList) {
            if (drink.getName().toLowerCase().contains(query.toLowerCase()) ||
                    drink.getDrinkType().toLowerCase().contains(query.toLowerCase()) ||
                    drink.getUnit().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(drink);
            }
        }
        adapter.setDrinks(filteredList);
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
