package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    private ActivityCategoryAddBinding binding;

    private EditText edtCategory;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        edtCategory = findViewById(R.id.edtCategory);

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

        //begin upload category
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String category = "";

    private void validateData() {

        edtCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(CategoryAddActivity.this, "A", Toast.LENGTH_SHORT).show();
            }
        });
        category = edtCategory.getText().toString().trim();

        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, R.string.please_enter_category, Toast.LENGTH_SHORT).show();
        }
        else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        progressDialog.setMessage(getString(R.string.add_category));
        progressDialog.show();

        //get timestamp
        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("category", "" + category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        //add to firebase db... Database Root > Categories > categoryId > category info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // category add success
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, R.string.more_success, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
//                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}