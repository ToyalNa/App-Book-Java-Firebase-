package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    private ActivityPdfEditBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private static final String TAG = "BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        binding.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String title = "", description = "";
    private void validateData() {
        title = binding.edtTitle.getText().toString().trim();
        description = binding.edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, R.string.enter_title, Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, R.string.enter_description, Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, R.string.select_category, Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }
    }

    private void updatePdf(){
        Log.d(TAG, "updatePdf: Starting updating pdf info to db ... ");

        progressDialog.setMessage(getString(R.string.updating_book_info));
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book updated ...");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, R.string.book_info_updated, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update due to" +e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String title = "" + snapshot.child("title").getValue();

                        binding.edtTitle.setText(title);
                        binding.edtDescription.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = "" + snapshot.child("category").getValue();
                                        binding.tvCategory.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryId = "", selectedCategoryTitle = "";

    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_category)
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategoryId = categoryIdArrayList.get(i);
                        selectedCategoryTitle = categoryTitleArrayList.get(i);

                        binding.tvCategory.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories ... ");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String id = "" + dataSnapshot.child("id").getValue();
                    String category = "" + dataSnapshot.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID: " + id);
                    Log.d(TAG, "onDataChange: Category: " + category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}