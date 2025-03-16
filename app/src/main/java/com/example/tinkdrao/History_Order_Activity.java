package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.adapter.History_Order_Adapter;
import com.example.tinkdrao.adapter.StatusOrderAdapter;
import com.example.tinkdrao.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class History_Order_Activity extends AppCompatActivity implements StatusOrderAdapter.OnStatusClickListener {
    private ListView lvHC;
    private List<Order> htcList; // Danh sách hiển thị
    private List<Order> fullHtcList; // Danh sách đầy đủ để lọc
    private DatabaseReference htcDBRef;
    private History_Order_Adapter htcAdapter;
    private FirebaseUser mUser;
    private StatusOrderAdapter statusOrderAdapter;
    private RecyclerView recStatusOrder;
    private List<String> statusList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);

        lvHC = findViewById(R.id.lvCartHistory);
        htcList = new ArrayList<>();
        fullHtcList = new ArrayList<>();
        getSupportActionBar().setTitle("Lịch sử đặt hàng");

        recStatusOrder = findViewById(R.id.recStatusOrder);
        statusList = Arrays.asList("Tất cả", "Chờ vận chuyển", "Đang vận chuyển", "Đã vận chuyển", "Đã hủy");
        statusOrderAdapter = new StatusOrderAdapter(this, statusList, this); // Truyền listener
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recStatusOrder.setLayoutManager(layoutManager);
        recStatusOrder.setAdapter(statusOrderAdapter);
        recStatusOrder.setHasFixedSize(true);
        recStatusOrder.setNestedScrollingEnabled(false);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        htcDBRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Order").child(mUser.getUid());
        loadOrderHistory();

        lvHC.setOnItemClickListener((parent, view, position, id) -> {
            Order historyCart = htcList.get(position);
            // Intent intent = new Intent(History_Order_Activity.this, DetailHCActivity.class);
            // intent.putExtra("id", historyCart.getId());
            // startActivity(intent);
        });
    }

    private void loadOrderHistory() {
        htcDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fullHtcList.clear();
                htcList.clear();
                for (DataSnapshot htcDatasnap : dataSnapshot.getChildren()) {
                    Order historyCart = htcDatasnap.getValue(Order.class);
                    if (historyCart != null) {
                        fullHtcList.add(historyCart);
                        htcList.add(historyCart); // Ban đầu hiển thị tất cả
                    }
                }
                htcAdapter = new History_Order_Adapter(History_Order_Activity.this, htcList);
                lvHC.setAdapter(htcAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Xử lý khi nhấn vào trạng thái trong RecyclerView
    @Override
    public void onStatusClick(String status) {
        filterOrdersByStatus(status);
    }

    private void filterOrdersByStatus(String status) {
        htcList.clear();
        if (status.equals("Tất cả")) { // Thêm tùy chọn "Tất cả" nếu muốn
            htcList.addAll(fullHtcList);
        } else {
            for (Order order : fullHtcList) {
                if (order.getStatusOrder() != null && order.getStatusOrder().equals(status)) {
                    htcList.add(order);
                }
            }
        }
        htcAdapter.notifyDataSetChanged(); // Cập nhật ListView
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mnUser) {
            startActivity(new Intent(History_Order_Activity.this, User_Activity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}