package com.example.bookapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.registerCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDate();
            }
        });

    }
    private String name = "", email = "", password = "", confirm = "";
    private void validateDate() {

        name = binding.edtName.getText().toString().trim();
        email = binding.edtEmail.getText().toString().trim();
        password = binding.passwordEdt.getText().toString().trim();
        confirm = binding.confirmpasswordEdt.getText().toString().trim();

        if (TextUtils.isEmpty(name)){

            Toast.makeText(this, R.string.enter_you_name, Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(this, R.string.invalid_email_pattern, Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){

            Toast.makeText(this, R.string.enter_you_pass, Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirm)){

            Toast.makeText(this, R.string.confirm_pass, Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirm)){

            Toast.makeText(this, R.string.password_does_not_match, Toast.LENGTH_SHORT).show();
        }
        else {

            createUserAccount();
        }
    }

    private void createUserAccount() {

        progressDialog.setMessage(getString(R.string.creating_account));
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage(getString(R.string.saving_user_info));

        long timestamp = System.currentTimeMillis();

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); //add empty, will do later
        hashMap.put("userType", "user"); //possible values are user, admin: will make admin manualy in firebase realtime db by changing this value
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data added to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account created...", Toast.LENGTH_SHORT).show();
                        //since user account is created so start dashboard of user
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //data failed adding to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}