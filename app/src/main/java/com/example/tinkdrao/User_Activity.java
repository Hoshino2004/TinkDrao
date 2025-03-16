package com.example.tinkdrao;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tinkdrao.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class User_Activity extends AppCompatActivity {

    TextView nameUser, emailUser, phonenoUser;
    Button btnChung, btnChangePassword, btnLogout, btnHDTN;
    ImageView avatarUser;
    FirebaseUser mUser;
    DatabaseReference userRef, adminCheckRef;
    StorageReference reference;
    Uri imageUri;
    ImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        getSupportActionBar().setTitle("Trang cá nhân");

        //Truy vấn dữ liệu
        nameUser = findViewById(R.id.nameUser);
        emailUser = findViewById(R.id.emailUser);
        phonenoUser = findViewById(R.id.phonenoUser);

        btnChung = findViewById(R.id.btnChung);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnHDTN = findViewById(R.id.btnHDTN);

        avatarUser = findViewById(R.id.avatarUser);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Users");

        userRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User userDetail = snapshot.getValue(User.class);
                    String nameUserDetail = userDetail.getUsername();
                    String emailUserDetail = userDetail.getEmail();
                    String phonenoUserDetail = userDetail.getPhoneno();
                    if (!User_Activity.this.isDestroyed()) {
                        // Thêm timestamp để tránh cache ảnh cũ
                        String avatarUrl = userDetail.getAvatar() + "?t=" + System.currentTimeMillis();
                        Glide.with(User_Activity.this)
                                .load(avatarUrl)
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // Không lưu cache
                                .skipMemoryCache(true) // Bỏ qua cache RAM
                                .into(avatarUser);
                    }
                    String roleUserDetail = userDetail.getRole();

                    nameUser.setText(nameUserDetail);
                    emailUser.setText(emailUserDetail);
                    phonenoUser.setText(phonenoUserDetail);
                    if (roleUserDetail.equals("Customer")) {
                        btnChung.setText("Chỉnh sửa");
                        btnChung.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(User_Activity.this);
                                View dialogView = getLayoutInflater().inflate(R.layout.update_profile, null);
                                imgAvatar = dialogView.findViewById(R.id.imgAvatarUpdate);
                                EditText edtNameUpdate = dialogView.findViewById(R.id.edtDisplayNameUpdate);
                                EditText edtPhonenoUpdate = dialogView.findViewById(R.id.edtPhoneUpdate);
                                EditText edtEmailUpdate = dialogView.findViewById(R.id.edtEmailUpdate);

                                builder.setView(dialogView);
                                AlertDialog dialog = builder.create();


                                edtEmailUpdate.setText(mUser.getEmail());
                                edtEmailUpdate.setEnabled(false);
                                edtPhonenoUpdate.setText(phonenoUserDetail);
                                edtPhonenoUpdate.setEnabled(false);
                                edtNameUpdate.setText(nameUserDetail);

                                String avatarUrl = userDetail.getAvatar() + "?t=" + System.currentTimeMillis();
                                Glide.with(User_Activity.this)
                                        .load(avatarUrl)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Không lưu cache
                                        .skipMemoryCache(true) // Bỏ qua cache RAM
                                        .into(imgAvatar);

                                imgAvatar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent galleryIntent = new Intent();
                                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                        galleryIntent.setType("image/*");
                                        startActivityForResult(galleryIntent, 2);
                                    }
                                });
                                dialogView.findViewById(R.id.btn_update_profile).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (imageUri != null) {
                                            uploadToFirebase(imageUri);
                                            mUser = FirebaseAuth.getInstance().getCurrentUser();
                                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(edtNameUpdate.getText().toString().trim()).build();
                                            if (edtNameUpdate.getText().toString().trim().equals("")) {
                                                Toast.makeText(User_Activity.this, "Không được để trống", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                String usernameUpdate = edtNameUpdate.getText().toString();
                                                Task<Uri> uriTask = reference.getDownloadUrl();
                                                uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        userRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    String avatar = uri.toString();
                                                                    userRef.child(mUser.getUid()).child("/avatar").setValue(avatar);
                                                                    userRef.child(mUser.getUid()).child("/username").setValue(usernameUpdate);
                                                                    DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("TinkDrao/Username");
                                                                    userDB.child(mUser.getUid()).setValue(usernameUpdate + " (" + mUser.getEmail() + ")");

//                                                                    DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comments");
//                                                                    commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                                        @Override
//                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                                            for (DataSnapshot houseSnapshot : snapshot.getChildren()) {
//                                                                                for (DataSnapshot commentSnapshot : houseSnapshot.getChildren()) {
//                                                                                    String commentUserId = commentSnapshot.child("userId").getValue(String.class);
//
//                                                                                    if (commentUserId != null && commentUserId.equals(mUser.getUid())) {
//                                                                                        // Cập nhật username mới cho bình luận
//                                                                                        commentSnapshot.getRef().child("username").setValue(usernameUpdate);
//                                                                                    }
//                                                                                }
//                                                                            }
//                                                                        }
//
//                                                                        @Override
//                                                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                                                        }
//                                                                    });
                                                                    Toast.makeText(User_Activity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                                                    mUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    });
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        } else {
                                            mUser = FirebaseAuth.getInstance().getCurrentUser();
                                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(edtNameUpdate.getText().toString().trim()).build();
                                            if (edtNameUpdate.getText().toString().trim().equals("")) {
                                                Toast.makeText(User_Activity.this, "Không được để trống", Toast.LENGTH_SHORT).show();
                                            } else {
                                                String usernameUpdate = edtNameUpdate.getText().toString();
                                                userRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            userRef.child(mUser.getUid()).child("/username").setValue(usernameUpdate);
                                                            Toast.makeText(User_Activity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                                            DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("TinkDrao/Username");
                                                            userDB.child(mUser.getUid()).setValue(usernameUpdate + " (" + mUser.getEmail() + ")");
                                                            mUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        dialog.dismiss();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                                if (dialog.getWindow() != null) {
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                                }
                                dialog.show();
                            }
                        });
                        btnChangePassword.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(User_Activity.this);
                                View dialogView = getLayoutInflater().inflate(R.layout.update_password, null);
                                EditText upCrtPass = dialogView.findViewById(R.id.upCrtPass);
                                EditText upNewPass = dialogView.findViewById(R.id.upNewPass);
                                EditText upRePass = dialogView.findViewById(R.id.upRePass);
                                builder.setView(dialogView);
                                AlertDialog dialog = builder.create();
                                dialogView.findViewById(R.id.btnUpdateUP).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mUser = FirebaseAuth.getInstance().getCurrentUser();
                                        String oldPass, newPass, rePass;
                                        oldPass = upCrtPass.getText().toString();
                                        newPass = upNewPass.getText().toString();
                                        rePass = upRePass.getText().toString();
                                        userRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);
                                                if (user.getPassword().equals(oldPass)) {
                                                    if (newPass.length() < 6) {
                                                        Toast.makeText(User_Activity.this, "Mật khẩu không được dưới 6 ký tự", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    if (!newPass.equals(rePass)) {
                                                        Toast.makeText(User_Activity.this, "Mật khẩu mới và Xác nhận mật khẩu không trùng", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    } else {
                                                        mUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(User_Activity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                                                    userRef.child(mUser.getUid()).child("/password").setValue(newPass);
                                                                    dialog.dismiss();
                                                                } else {
                                                                    upCrtPass.setText("");
                                                                    upNewPass.setText("");
                                                                    upRePass.setText("");
                                                                    Toast.makeText(User_Activity.this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    Toast.makeText(User_Activity.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    }
                                });
                                if (dialog.getWindow() != null) {
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                                }
                                dialog.show();
                            }
                        });
                        btnHDTN.setText("Đơn hàng");
                        btnHDTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//                                startActivity(new Intent(User_Activity.this, HDTN_Activity.class));
//                                Intent intent = new Intent(User_Activity.this, HDTN_Activity.class);
//                                intent.putExtra("role", roleUserDetail);
//                                startActivity(intent);
                            }
                        });
                    }
                    if (roleUserDetail.equals("Admin")) {
                        btnChung.setText("Quản lý \n sản phẩm");
                        btnChung.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(User_Activity.this, DrinkListActivity.class );
                                startActivity(intent);
                            }
                        });
//                        btnChung.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(User_Activity.this);
//                                View dialogView = getLayoutInflater().inflate(R.layout.admin_password, null);
//                                EditText edtAdminPassword = dialogView.findViewById(R.id.edtPasswordAdmin);
//
//                                builder.setView(dialogView);
//                                AlertDialog dialog = builder.create();
//                                dialogView.findViewById(R.id.btnLogAdmin).setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        String passwordAdmin = "[" + edtAdminPassword.getText().toString().trim() + "]";
//                                        adminCheckRef.addValueEventListener(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                if (snapshot.exists()) {
//                                                    if (passwordAdmin.equals(snapshot.getValue().toString())) {
//                                                        Toast.makeText(User_Activity.this, "Ngon lành", Toast.LENGTH_SHORT).show();
//                                                        dialog.dismiss();
//                                                        startActivity(new Intent(User_Activity.this, Retrieve_Data_Activity.class));
//                                                    } else {
//                                                        Toast.makeText(User_Activity.this, "Sai rồi bạn ơi", Toast.LENGTH_SHORT).show();
//                                                        dialog.dismiss();
//                                                    }
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onCancelled(@NonNull DatabaseError error) {
//
//                                            }
//                                        });
//                                    }
//                                });
//                                if (dialog.getWindow() != null) {
//                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
//                                }
//                                dialog.show();
//                            }
//                        });
                        btnChangePassword.setText("Quản lý \n Đơn hàng");
//                        btnChangePassword.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                startActivity(new Intent(User_Activity.this, ReportAdminActivity.class));
//                            }
//                        });
                        btnHDTN.setText("Tư vấn");
//                        btnHDTN.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Intent intent = new Intent(User_Activity.this, HDTN_Activity.class);
//                                intent.putExtra("role", roleUserDetail);
//                                startActivity(intent);
//                            }
//                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(User_Activity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void uploadToFirebase(Uri uri) {
        reference = FirebaseStorage.getInstance().getReference("TinkDrao/Image Users/" + mUser.getUid());
        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgAvatar.setImageURI(imageUri);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_user, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mnHome) {
            startActivity(new Intent(User_Activity.this, MainActivity.class));
            finish();
        }
        if (item.getItemId() == R.id.mnFavorite) {
            startActivity(new Intent(User_Activity.this, Favorite_Drink_Activity.class));
        }
        if (item.getItemId() == R.id.mnCart) {
            startActivity(new Intent(User_Activity.this, Cart_Activity.class));
        }
//        if (item.getItemId() == R.id.mnChat) {
//            userRef = FirebaseDatabase.getInstance().getReference("Users");
//            mUser = FirebaseAuth.getInstance().getCurrentUser();
//            userRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        User userDetail = snapshot.getValue(User.class);
//                        String roleUserDetail = userDetail.getRole();
//                        if(roleUserDetail.equals("Customer"))
//                        {
//                            startActivity(new Intent(User_Activity.this, ChatActivity.class));
//                        }
//                        else {
//                            startActivity(new Intent(User_Activity.this, UserList.class));
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
        return super.onOptionsItemSelected(item);
    }
}