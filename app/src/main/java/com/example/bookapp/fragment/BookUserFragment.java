package com.example.bookapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookapp.R;
import com.example.bookapp.adapters.AdapterPdfUser;
import com.example.bookapp.databinding.FragmentBookUserBinding;
import com.example.bookapp.models.ModelFilePdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookUserFragment extends Fragment {

    //that we passed while creating instance of this fragment
    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelFilePdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    //view binding
    private FragmentBookUserBinding binding;

    private static final String TAG = "BOOKS_USER_TAG";


    public BookUserFragment() {
        // Required empty public constructor
    }


    public static BookUserFragment newInstance(String categoryId, String category, String uid) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()),container,false);

        Log.d(TAG, "onCreateView: Category: " +category);
        if (category.equals("All")){
            //load all books
            loadAllBooks();
        }
        else if (category.equals("Most Viewed")){
            //load most viewed books
            loadMostViewedDownloadedBooks("viewsCount");
        }
        else if (category.equals("Most Downloaded")){
            //load most downloaded books
            loadMostViewedDownloadedBooks("downloadsCount");
        }
        else {
            //load selected category books
            loadCategoriesBooks();
        }

        //search
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //called as and when user type any letter
                try {
                    adapterPdfUser.getFilter().filter(charSequence);
                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: " +e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return binding.getRoot();
    }

    private void loadAllBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference refLoadAllBooks = FirebaseDatabase.getInstance().getReference("Books");
        refLoadAllBooks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    //get data
                    ModelFilePdf modelFilePdf = dataSnapshot.getValue(ModelFilePdf.class);
                    //add to list
                    pdfArrayList.add(modelFilePdf);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                //set adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewedDownloadedBooks(String orderBy) {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10) // load 10 most views or downloaded books
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    //get data
                    ModelFilePdf modelFilePdf = dataSnapshot.getValue(ModelFilePdf.class);
                    //add to list
                    pdfArrayList.add(modelFilePdf);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                //set adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCategoriesBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            //get data
                            ModelFilePdf modelFilePdf = dataSnapshot.getValue(ModelFilePdf.class);
                            //add to list
                            pdfArrayList.add(modelFilePdf);
                        }
                        //setup adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                        //set adapter to recyclerview
                        binding.booksRv.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}