package com.example.bookapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import com.example.bookapp.Constans;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityReadPdfBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ReadPdfActivity extends AppCompatActivity {

    private ActivityReadPdfBinding binding;
    private String bookId;
    private static final String TAG = "PDF_READ_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: BookId: " + bookId);

        loadBookDetails();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf URL from db ... ");
        DatabaseReference refBookDetails = FirebaseDatabase.getInstance().getReference("Books");
        refBookDetails.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book url
                        String pdfUrl = "" + snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: PDF URL: " + pdfUrl);

                        //step2 load pdf using that url from firebase storage
                        loadBookFromUrl(pdfUrl);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Get PDF from storage");
        StorageReference refUrl = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        refUrl.getBytes(Constans.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        binding.pdfView.fromBytes(bytes)
                                .swipeHorizontal(false) //set false to scroll vertical, set true to swip horizonta
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        //set current and total pages in toolbar subtitle
                                        int currentPage = (page + 1); //do +1 because page starts from 0
                                        binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount); //e.g. 3/290
                                        Log.d(TAG, "onPageChanged: " + currentPage + "/" + pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: " + t.getMessage());
                                        Toast.makeText(ReadPdfActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                        Toast.makeText(ReadPdfActivity.this, R.string.error_on_page + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}