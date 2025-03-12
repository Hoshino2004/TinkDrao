package com.example.tinkdrao;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tinkdrao.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Register_Activity extends AppCompatActivity {
    LinearLayout ln1, ln2, ln3;
    ScrollView scrollView;
    EditText emailReg, passReg, repassReg, username, phoneno, otpsms;
    CheckBox checkBoxReg;
    TextView DKDV1, DKDV2, CSVQRT1, CSVQRT2, haveACC, btnSendOTP;
    ImageButton btnReg, btnReg1, btnReg2;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        FirebaseAuth.getInstance().getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);


        //Khai báo
        mAuth=FirebaseAuth.getInstance();
        emailReg=findViewById(R.id.edtEmailReg);
        passReg=findViewById(R.id.edtPassReg);
        repassReg=findViewById(R.id.edtReReg);
        username=findViewById(R.id.edtUsername);
        phoneno=findViewById(R.id.edtPhoneNo);
        haveACC=findViewById(R.id.tvHaveAcc);
        DKDV1=findViewById(R.id.tvDKDV1);
        DKDV2=findViewById(R.id.tvDKDV2);
        CSVQRT1=findViewById(R.id.tvCSVQRT1);
        CSVQRT2=findViewById(R.id.tvCSVQRT2);
        btnReg=findViewById(R.id.btnReg);
        btnReg1=findViewById(R.id.btnReg1);
        btnReg2=findViewById(R.id.btnReg2);
        checkBoxReg=findViewById(R.id.checkboxReg);
        scrollView = findViewById(R.id.scrollView);
        ln1 = findViewById(R.id.lnStep1);
        ln2 = findViewById(R.id.lnStep2);
        ln3 = findViewById(R.id.lnStep3);

        //
        userRef= FirebaseDatabase.getInstance().getReference("TinkDrao");
        btnReg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        haveACC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register_Activity.this, Login_Activity.class));
            }
        });
        // Ban đầu không cho tick
        checkBoxReg.setEnabled(false);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
            if (lastChild != null) {
                int diff = (lastChild.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                if (diff <= 0) {
                    checkBoxReg.setEnabled(true); // Cho phép tick khi cuộn đến cuối
                }
            }
        });
        phoneno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Kiểm tra xem số điện thoại có bắt đầu bằng số 0 hay không
                if (!s.toString().startsWith("0")) {
                    phoneno.setError("Số điện thoại phải bắt đầu bằng số 0!");
                    return;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void register() {
        String email=emailReg.getText().toString();
        String password=passReg.getText().toString();
        String rePassword =repassReg.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Email không được trống",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Mật khẩu không được trống",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(rePassword)){
            Toast.makeText(this,"Xác nhận mật khẩu không được trống",Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length()<6){
            Toast.makeText(this, "Mật khẩu không được dưới 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(rePassword)){
            Toast.makeText(this, "Mật khẩu và Xác nhận mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        userRef.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Toast.makeText(Register_Activity.this, "Email đã tồn tại, vui lòng chọn email khác", Toast.LENGTH_SHORT).show();
                    recreate();
                }
                else {
                    ln1.setVisibility(View.GONE);
                    ln2.setVisibility(View.VISIBLE);
                    btnReg2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RadioGroup radioGroup = findViewById(R.id.radioGroup);
                            int selectedId = radioGroup.getCheckedRadioButtonId();
                            if(selectedId==-1)
                            {
                                Toast.makeText(Register_Activity.this, "Vui lòng chọn giới tính của bạn", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                RadioButton radioButton = findViewById(selectedId);
                                register2(email, password, radioButton.getText().toString());
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

    private void register2(String email, String password, String gioitinh) {

        if(username.getText().toString().equals(""))
        {
            Toast.makeText(this, "Vui lòng nhập username của bạn vào đây", Toast.LENGTH_SHORT).show();
        }
        else {
            ln2.setVisibility(View.GONE);
            ln3.setVisibility(View.VISIBLE);
            btnReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    register3(email, password, username.getText().toString(), phoneno.getText().toString(), gioitinh);
                }
            });
        }
    }

    private void register3(String email, String password, String username, String phoneno, String gioitinh) {
        String role = "Customer";

        if(!checkBoxReg.isChecked())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(Register_Activity.this);
            View dialogView =getLayoutInflater().inflate(R.layout.error_reg,null);
            Button btnErrA = findViewById(R.id.btnErrA);
            Button btnErrD = findViewById(R.id.btnErrD);


            builder.setView(dialogView);
            AlertDialog dialog= builder.create();

            dialogView.findViewById(R.id.btnErrA).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBoxReg.setChecked(true);
                    dialog.dismiss();
                }
            });
            dialogView.findViewById(R.id.btnErrD).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            if(dialog.getWindow() != null){
//                lumbertycoon9524@gmail.com
                dialog.getWindow().setDimAmount(0.5f); // Thay đổi độ mờ của nền
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            dialog.show();
        }
        else {
            if(gioitinh.equals("Nam"))
            {
                String avatar = "https://firebasestorage.googleapis.com/v0/b/tousehao.appspot.com/o/Image%20Users%2Favatar_boy.png?alt=media&token=5959eb26-05c7-46c1-b538-03513533c5ae";
                createAccountUser(id, username, email, password, phoneno, avatar, role, gioitinh);
            }
            else {
                String avatar="https://firebasestorage.googleapis.com/v0/b/tousehao.appspot.com/o/Image%20Users%2Favatar_girl.png?alt=media&token=ecf0669d-4186-4b94-8ba3-9c4cca4b0575";
                createAccountUser(id, username, email, password, phoneno, avatar, role, gioitinh);
            }
        }
    }

    private void createAccountUser(String id, String username, String email, String password, String phoneno, String avatar, String role, String gioitinh) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                User user = new User(id,username,email,password,phoneno,avatar,role,gioitinh);
                                userRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(user);
                                DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("TinkDrao").child("Username");
                                userDB.child(mAuth.getCurrentUser().getUid()).setValue(username + " (" + mAuth.getCurrentUser().getEmail() + ")");
                                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username.toString().trim()).build();
                                mAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    }
                                });
                                Toast.makeText(Register_Activity.this, "Đăng ký thành công, vui lòng xác nhận Email", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Register_Activity.this, Login_Activity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"Email này đã tồn tại, vui lòng nhập Email khác", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}