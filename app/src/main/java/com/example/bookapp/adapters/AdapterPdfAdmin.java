package com.example.bookapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.activity.DetailPdfActivity;
import com.example.bookapp.activity.PdfEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filter.FilterPdfAdmin;
import com.example.bookapp.models.ModelFilePdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.ViewHolderPdfAdmin> implements Filterable {

    private Context context;
    private static final String TAG = "PDF_ADAPTER";
    public ArrayList<ModelFilePdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelFilePdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public ViewHolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPdfAdmin holder, int position) {

        ModelFilePdf modelFilePdf = pdfArrayList.get(position);
        String pdfId = modelFilePdf.getId();
        String categoryId = modelFilePdf.getCategoryId();
        String title = modelFilePdf.getTitle();
        String description = modelFilePdf.getDescription();
        String pdfUrl = modelFilePdf.getUrl();
        long timestamp = modelFilePdf.getTimestamp();

        String formattedDate = MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        MyApplication.loadCategory(
                "" + categoryId,
                holder.categoryTv
        );


        MyApplication.loadPdfFromUrlSinglePage(
                "" + pdfUrl,
                "" + title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.loadPdfSize(
                "" + pdfUrl,
                "" + title,
                holder.sizeTv);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(modelFilePdf, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailPdfActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelFilePdf modelFilePdf, ViewHolderPdfAdmin holder) {
        String bookId = modelFilePdf.getId();
        String bookUrl = modelFilePdf.getUrl();
        String bookTitle = modelFilePdf.getTitle();

        String[] options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.choose_options)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle dialog options click
                        if (i == 0) {
                            //Edit clicked, Open new activity to edit the book info
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);
                        } else if (i == 1) {
                            //Delete clicked
                            MyApplication.deleteBook(context,
                                    "" + bookId,
                                    "" + bookUrl,
                                    "" + bookTitle
                            );
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    class ViewHolderPdfAdmin extends RecyclerView.ViewHolder {
        PDFView pdfView;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;
        ProgressBar progressBar;

        public ViewHolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;

        }
    }
}
