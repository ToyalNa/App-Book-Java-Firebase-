package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activity.DetailPdfActivity;
import com.example.bookapp.databinding.RowPdfUserBinding;
import com.example.bookapp.filter.FilterPdfUser;
import com.example.bookapp.models.ModelFilePdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.ViewHolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelFilePdf> arrayListPdf, filterList;
    private FilterPdfUser filterPdfUser;

    private RowPdfUserBinding binding;
    private static final String TAG = "ADAPTER_PDF_USER_TAG";

    public AdapterPdfUser(Context context, ArrayList<ModelFilePdf> arrayListPdf) {
        this.context = context;
        this.arrayListPdf = arrayListPdf;
        this.filterList = arrayListPdf;
    }

    @NonNull
    @Override
    public ViewHolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);

        return new ViewHolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPdfUser holder, int position) {

        ModelFilePdf modelFilePdf = arrayListPdf.get(position);
        String bookId= modelFilePdf.getId();
        String title = modelFilePdf.getTitle();
        String description= modelFilePdf.getDescription();
        String pdfUrl= modelFilePdf.getUrl();
        String categoryId= modelFilePdf.getCategoryId();
        long timestamp = modelFilePdf.getTimestamp();

        String date = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        MyApplication.loadPdfFromUrlSinglePage(
                "" + pdfUrl,
                "" + title,
                holder.pdfView,
                holder.progressBar,
                null);
        MyApplication.loadCategory(
                "" + categoryId,
                holder.categoryTv
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                "" + title,
                holder.sizeTv
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailPdfActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayListPdf.size(); // return list size || number of records
    }

    @Override
    public Filter getFilter() {
        if (filterPdfUser == null){
            filterPdfUser = new FilterPdfUser(filterList,this);
        }
        return filterPdfUser;
    }

    class ViewHolderPdfUser extends RecyclerView.ViewHolder{
        TextView titleTv,descriptionTv, categoryTv, sizeTv, dateTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public ViewHolderPdfUser(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv =binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
