package com.example.tinkdrao;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.adapter.OrderAdapter;
import com.example.tinkdrao.adapter.OrderAdapter2;
import com.example.tinkdrao.model.Drink;
import com.example.tinkdrao.model.Order;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThongKeActivity extends AppCompatActivity {
    private TextInputEditText startDate, endDate, startTime, endTime;
    private Button filterButton;
    private RecyclerView ordersRecyclerView;
    private TextView totalOrders, totalRevenue;
    private OrderAdapter2 orderAdapter;
    private List<Order> orderList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);

        getSupportActionBar().setTitle("Thống kê doanh thu");

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        startTime = findViewById(R.id.start_time);
        endTime = findViewById(R.id.end_time);

        filterButton = findViewById(R.id.filter_button);
        ordersRecyclerView = findViewById(R.id.orders_recycler_view);
        totalOrders = findViewById(R.id.total_orders);
        totalRevenue = findViewById(R.id.total_revenue);

        // Khởi tạo RecyclerView
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter2(orderList);
        ordersRecyclerView.setAdapter(orderAdapter);

        // Khởi tạo Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Order");

        // Thiết lập sự kiện nhấn cho các trường ngày
        startDate.setOnClickListener(v -> showDatePickerDialog(startDate));
        endDate.setOnClickListener(v -> showDatePickerDialog(endDate));

        // Thiết lập sự kiện nhấn cho các trường giờ
        startTime.setOnClickListener(v -> showTimePickerDialog(startTime));
        endTime.setOnClickListener(v -> showTimePickerDialog(endTime));

        // Sự kiện nhấn nút lọc
        filterButton.setOnClickListener(v -> filterOrders());

        // Lấy drawable từ resources
        Drawable calendarIcon = ContextCompat.getDrawable(this, R.drawable.ic_calendar);
        Drawable clockIcon = ContextCompat.getDrawable(this, R.drawable.ic_clock);

        // Đặt kích thước mới (ví dụ: 20dp)
        int size = (int) (20 * getResources().getDisplayMetrics().density); // Chuyển dp sang pixel
        calendarIcon.setBounds(0, 0, size, size);
        clockIcon.setBounds(0, 0, size, size);

        // Áp dụng drawable cho EditText
        startDate.setCompoundDrawables(null, null, calendarIcon, null);
        endDate.setCompoundDrawables(null, null, calendarIcon, null);
        startTime.setCompoundDrawables(null, null, clockIcon, null);
        endTime.setCompoundDrawables(null, null, clockIcon, null);
    }

    private void showDatePickerDialog(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(selectedTime);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void filterOrders() {
        String startDateText = startDate.getText().toString();
        String endDateText = endDate.getText().toString();
        String startTimeText = startTime.getText().toString();
        String endTimeText = endTime.getText().toString();

        if (TextUtils.isEmpty(startDateText) || TextUtils.isEmpty(endDateText)) {
            Toast.makeText(this, "Vui lòng chọn khoảng ngày", Toast.LENGTH_SHORT).show();
            return;
        }

        // Định dạng thời gian để so sánh
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            // Tạo thời gian bắt đầu và kết thúc từ input
            Date start = dateTimeFormat.parse(startDateText + " " + (startTimeText.isEmpty() ? "00:00:00" : startTimeText + ":00"));
            Date end = dateTimeFormat.parse(endDateText + " " + (endTimeText.isEmpty() ? "23:59:59" : endTimeText + ":59"));

            // Lấy dữ liệu từ Firebase
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    orderList.clear();
                    int totalRev = 0;

                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        for (DataSnapshot invoiceSnapshot : orderSnapshot.getChildren()) {
                            String createdAt = invoiceSnapshot.child("createdAt").getValue(String.class);
                            String statusOrder = invoiceSnapshot.child("statusOrder").getValue(String.class);
                            String statusPay = invoiceSnapshot.child("statusPay").getValue(String.class);
                            String address = invoiceSnapshot.child("address").getValue(String.class);
                            String nameUser = invoiceSnapshot.child("nameUser").getValue(String.class); // Lấy nameUser
                            String phoneNo = invoiceSnapshot.child("phoneNo").getValue(String.class);   // Lấy phoneNo
                            // Lấy total dưới dạng Long
                            Long total = invoiceSnapshot.child("total").getValue(Long.class);

                            if (total == null) continue; // Bỏ qua nếu total null

                            // Kiểm tra điều kiện status
                            if ("Giao hàng thành công".equals(statusOrder) && "Đã thanh toán".equals(statusPay)) {
                                try {
                                    Date orderDate = dateTimeFormat.parse(createdAt);
                                    // Kiểm tra nếu đơn hàng nằm trong khoảng thời gian
                                    if (orderDate.compareTo(start) >= 0 && orderDate.compareTo(end) <= 0) {
                                        // Lấy danh sách sản phẩm
                                        List<Drink> products = new ArrayList<>();
                                        DataSnapshot dataSnapshot = invoiceSnapshot.child("Data");
                                        for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                                            String name = productSnapshot.child("name").getValue(String.class);
                                            int price = productSnapshot.child("price").getValue(Integer.class);
                                            int quantity = productSnapshot.child("quantity").getValue(Integer.class);
                                            String unit = productSnapshot.child("unit").getValue(String.class);
                                            String imageUrl = productSnapshot.child("imageUrl").getValue(String.class);

                                            products.add(new Drink(name, price, quantity, unit, imageUrl));
                                        }

                                        // Thêm đơn hàng vào danh sách
                                        // Truyền nameUser và phoneNo vào Order
                                        Order order = new Order(createdAt, address, total, nameUser, phoneNo, products);
                                        orderList.add(order);
                                        totalRev += total;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Cập nhật UI
                    orderAdapter.notifyDataSetChanged();
                    totalOrders.setText(String.valueOf(orderList.size()));
                    totalRevenue.setText(String.format("%,d VNĐ", totalRev));

                    if (orderList.isEmpty()) {
                        Toast.makeText(ThongKeActivity.this, "Không có đơn hàng nào trong khoảng thời gian này", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ThongKeActivity.this, "Lỗi khi lấy dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (ParseException e) {
            Toast.makeText(this, "Lỗi định dạng ngày giờ", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
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