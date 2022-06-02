package com.example.bookapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.activity.PdfListAdminActivity;
import com.example.bookapp.filter.FilterCategory;
import com.example.bookapp.models.ModelCategory;
import com.example.bookapp.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;
    private RowCategoryBinding binding;
    private FilterCategory filterCategory;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public ViewHolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false);

        return new ViewHolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderCategory holder, int position) {
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        holder.categoryTv.setText(category);

        holder.deleteBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.delete)
                        .setMessage(R.string.do_you_want_to_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin delete
                                Toast.makeText(context, R.string.deleting, Toast.LENGTH_SHORT).show();
                                deleteCategory(model,holder);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId",id);
                intent.putExtra("categoryTitle",category);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(ModelCategory model, ViewHolderCategory holder) {
        String id = model.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //delete successfully
                        Toast.makeText(context, R.string.successfully_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //deleted failed
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filterCategory == null){
            filterCategory = new FilterCategory(filterList,this);
        }
        return filterCategory;
    }

    class ViewHolderCategory extends RecyclerView.ViewHolder{
        TextView categoryTv;
        ImageButton deleteBtnImg;

        public ViewHolderCategory(@NonNull View itemView) {
            super(itemView);

            categoryTv = binding.tvCategoryTitle;
            deleteBtnImg = binding.imgBtnDelete;
        }
    }
}
