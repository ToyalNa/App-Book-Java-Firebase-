package com.example.bookapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityAddPdfBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AddPdfActivity extends AppCompatActivity {

    private ActivityAddPdfBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private Uri pdfUri = null;
    private static final int PDF_PICK_CODE = 1000;

    //TAG for debugging
    private static final String TAG = "ADD_PDF_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        binding.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
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
        //step 1: Validate data
        Log.d(TAG, "validateData: validating data... ");

        //get data
        title = binding.edtTitle.getText().toString().trim();
        description = binding.edtDescription.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, R.string.enter_title, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, R.string.enter_description, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, R.string.select_category, Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, R.string.select_file_PDF, Toast.LENGTH_SHORT).show();
        } else {
            //all data is validate , can upload now
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        //step 2: upload PDF to firebase
        Log.d(TAG, "uploadPdfToStorage: uploading to storage... ");

        //show progress
        progressDialog.setMessage(getString(R.string.upload_PDF_file));
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //path of pdf in firebase storage
        String filePathAndName = "Books/ " + timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to storage... ");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedPdfUrl = "" + uriTask.getResult();

                        //upload to firebase db
                        uploadedPdfInfoToDb(uploadedPdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Upload PDF failed due to " + e.getMessage());
                        Toast.makeText(AddPdfActivity.this, R.string.upload_PDF_faile + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadedPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        //step 2: upload PDF info to firebase db
        Log.d(TAG, "uploadPdfToStorage: upload PDF info to firebase db... ");

        progressDialog.setMessage(getString(R.string.loading_files));

        String uid = firebaseAuth.getUid();
        //setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("category", "" + selectedCategoryTitle);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded... ");
                        Toast.makeText(AddPdfActivity.this, R.string.upload_success, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db " + e.getMessage());
                        Toast.makeText(AddPdfActivity.this, R.string.upload_failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //db reference to load categories... db > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    //get id and title of category
                    String categoryTitle = "" + dataSnapshot.child("category").getValue();
                    String categoryId = "" + dataSnapshot.child("id").getValue();

                    //add to respective arraylist
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryTitle, selectedCategoryId;

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get String array of categories from arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_category)
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        selectedCategoryTitle = categoryTitleArrayList.get(i);
                        selectedCategoryId = categoryIdArrayList.get(i);

                        binding.tvCategory.setText(selectedCategoryTitle);

                        Log.d(TAG, "onClick: Selected Category: " + selectedCategoryId + " " + selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file_PDF)), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI: " + pdfUri);
            }
        } else {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, R.string.deselect_files, Toast.LENGTH_SHORT).show();
        }
    }
}