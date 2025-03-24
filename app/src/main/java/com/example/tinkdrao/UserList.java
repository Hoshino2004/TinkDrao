package com.example.tinkdrao;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tinkdrao.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredUserList;
    private DatabaseReference databaseReference;
    private static final String TAG = "UserList"; // Tag cho log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        userAdapter = new UserAdapter(this, filteredUserList);
        recyclerView.setAdapter(userAdapter);

        getSupportActionBar().setTitle("Danh sách người dùng");


        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Gán sự kiện click cho UserAdapter
        userAdapter.setOnItemClickListener(user -> {
            if (user.getId() != null) {
                Intent intent = new Intent(UserList.this, ChatActivity.class);
                intent.putExtra("selectedUser", user.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Lỗi: User không có ID!", Toast.LENGTH_SHORT).show();
            }
        });

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("TinkDrao/Users");
        loadUsers();

        // Thiết lập SearchView
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return false;
            }
        });
    }

    private void loadUsers() {
        Log.d(TAG, "Bắt đầu tải danh sách users từ Firebase");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Dữ liệu thô từ Firebase: " + snapshot.toString());

                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Gán id từ key của DataSnapshot
                        user.setId(dataSnapshot.getKey());

                        // Ghi log thông tin từng user
                        Log.d(TAG, "User tìm thấy - ID: " + user.getId() +
                                ", Username: " + user.getUsername() +
                                ", Email: " + user.getEmail() +
                                ", Role: " + user.getRole());

                        // Kiểm tra role và ID
                        if ("Customer".equals(user.getRole()) && user.getId() != null) {
                            userList.add(user);
                            Log.d(TAG, "Đã thêm user vào danh sách: " + user.getUsername());
                        } else {
                            Log.w(TAG, "User không thỏa mãn điều kiện - Role: " + user.getRole() +
                                    ", ID: " + user.getId());
                        }
                    } else {
                        Log.w(TAG, "Không thể parse dữ liệu từ snapshot: " + dataSnapshot.toString());
                    }
                }

                // Log danh sách hoàn chỉnh
                Log.d(TAG, "Danh sách users sau khi tải: " + userList.size() + " users");
                for (int i = 0; i < userList.size(); i++) {
                    User u = userList.get(i);
                    Log.d(TAG, "User " + i + ": " + u.getUsername() + " (ID: " + u.getId() + ")");
                }

                // Cập nhật filteredUserList
                filteredUserList.clear();
                filteredUserList.addAll(userList);
                userAdapter.notifyDataSetChanged();
                Log.d(TAG, "Đã cập nhật adapter với " + filteredUserList.size() + " users");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải dữ liệu từ Firebase: " + error.getMessage());
                Toast.makeText(UserList.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        filteredUserList.clear();
        if (TextUtils.isEmpty(query)) {
            filteredUserList.addAll(userList);
            Log.d(TAG, "Không có query, hiển thị toàn bộ danh sách: " + filteredUserList.size() + " users");
        } else {
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
            Log.d(TAG, "Lọc users với query '" + query + "': " + filteredUserList.size() + " users");
        }
        userAdapter.notifyDataSetChanged();
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