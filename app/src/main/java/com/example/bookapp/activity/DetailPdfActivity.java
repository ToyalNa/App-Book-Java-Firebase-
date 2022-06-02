package com.example.bookapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterComment;
import com.example.bookapp.databinding.ActivityDetailPdfBinding;
import com.example.bookapp.databinding.DialogCommentAddBinding;
import com.example.bookapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailPdfActivity extends AppCompatActivity {

    private ActivityDetailPdfBinding binding;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    boolean isInMyFavorite = false;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;


    String bookId, bookTitle, bookUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        binding.btnDownloadBook.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadBookDetails();
        loadComments();

        MyApplication.incrementBookViewCount(bookId);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.btnReadBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentReadBook = new Intent(DetailPdfActivity.this, ReadPdfActivity.class);
                intentReadBook.putExtra("bookId", bookId);
                startActivity(intentReadBook);
            }
        });

        binding.btnDownloadBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                if (ContextCompat.checkSelfPermission(DetailPdfActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                    MyApplication.downloadBook(DetailPdfActivity.this, "" + bookId, ""+bookTitle, ""+bookUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request permission ...");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        binding.btnFavoriteBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(DetailPdfActivity.this, R.string.you_are_not_logged_in, Toast.LENGTH_SHORT).show();
                }
                else {
                    if (isInMyFavorite){
                        MyApplication.removeFromFavorite(DetailPdfActivity.this,bookId);
                    }
                    else {
                        MyApplication.addToFavorite(DetailPdfActivity.this,bookId);
                    }
                }
            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(DetailPdfActivity.this, R.string.you_are_not_logged_in, Toast.LENGTH_SHORT).show();
                }
                else {
                    addCommentDialog();
                }
            }
        });
    }

    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelComment modelComment = ds.getValue(ModelComment.class);

                            commentArrayList.add(modelComment);
                        }
                        adapterComment = new AdapterComment(DetailPdfActivity.this,commentArrayList);
                        binding.commentRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String comment = "";
    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        commentAddBinding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment = commentAddBinding.commentEdt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)){
                    Toast.makeText(DetailPdfActivity.this, R.string.enter_comment, Toast.LENGTH_SHORT).show();
                }
                else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

    private void addComment() {
        progressDialog.setMessage(getString(R.string.add_comment));
        progressDialog.show();

        String timestamp =""+System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("bookId", ""+bookId);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("comment", ""+comment);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(DetailPdfActivity.this, R.string.added_a_comment, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(DetailPdfActivity.this, R.string.failed +e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (isGranted){
                    Log.d(TAG_DOWNLOAD, "Permission Granted ");
                    MyApplication.downloadBook(this, ""+bookId, ""+bookTitle,""+bookUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied ... ");
                    Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        bookTitle = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        bookUrl = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();

                        //required data is loaded, show download button
                        binding.btnDownloadBook.setVisibility(View.VISIBLE);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                "" + categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pagesTv
                        );
                        MyApplication.loadPdfSize(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.sizeTv
                        );

                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite  = snapshot.exists(); // true: if exits, false if not exits
                        if (isInMyFavorite){
                            //exits in favorite
                            binding.btnFavoriteBook.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0);
                            binding.btnFavoriteBook.setText(R.string.remove_favorite);
                        }
                        else {
                            binding.btnFavoriteBook.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border,0,0);
                            binding.btnFavoriteBook.setText(R.string.add_favorite);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}