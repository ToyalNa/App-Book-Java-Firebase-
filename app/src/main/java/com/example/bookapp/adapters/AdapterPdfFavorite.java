package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activity.DetailPdfActivity;
import com.example.bookapp.databinding.RowPdfFavoriteBinding;
import com.example.bookapp.models.ModelFilePdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.ViewHolderFavorite>{

    private Context context;
    private ArrayList<ModelFilePdf> pdfArrayList;
    private RowPdfFavoriteBinding binding;

    private static final String TAG = "ADAPTER_FAVORITES_TAG";

    public AdapterPdfFavorite(Context context, ArrayList<ModelFilePdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public ViewHolderFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);


        return new ViewHolderFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFavorite holder, int position) {

        ModelFilePdf modelFilePdf = pdfArrayList.get(position);

        loadPdfDetails(modelFilePdf,holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailPdfActivity.class);
                intent.putExtra("bookId", modelFilePdf.getId());
                context.startActivity(intent);
            }
        });

        holder.removeFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context,modelFilePdf.getId());
            }
        });

    }

    private void loadPdfDetails(ModelFilePdf modelFilePdf, ViewHolderFavorite holder) {
        String bookId = modelFilePdf.getId();
        Log.d(TAG, "loadPdfDetails: Book Details of Book ID: " + bookId);

        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Books");
        dataRef.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bookTitle = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String bookUrl = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();

                        modelFilePdf.setFavorite(true);
                        modelFilePdf.setTitle(bookTitle);
                        modelFilePdf.setDescription(description);
                        modelFilePdf.setTimestamp(Long.parseLong(timestamp));
                        modelFilePdf.setCategoryId(categoryId);
                        modelFilePdf.setUid(uid);
                        modelFilePdf.setUrl(bookUrl);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(categoryId,holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage("" + bookUrl,
                                "" + bookTitle,
                                holder.pdfView, holder.progressBar, null);
                        MyApplication.loadPdfSize("" + bookUrl,
                                "" + bookTitle,
                                holder.sizeTv);

                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class ViewHolderFavorite extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavoriteBtn;

        public ViewHolderFavorite(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            removeFavoriteBtn = binding.removeFavBtn;
        }
    }
}
