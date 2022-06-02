package com.example.bookapp.filter;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterPdfUser;
import com.example.bookapp.models.ModelFilePdf;

import java.util.ArrayList;
import java.util.Locale;

public class FilterPdfUser extends Filter {

    //arraylist in which we want to search
    ArrayList<ModelFilePdf> filterList;
    //adapter in which filter need to be implement
    AdapterPdfUser adapterPdfUser;

    //constructor

    public FilterPdfUser(ArrayList<ModelFilePdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value to be searche should not be null/empty
        if (charSequence != null || charSequence.length() > 0) {
            //not null nor empty
            //change to uppercase or lower case to avoid case sensitivity
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelFilePdf> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence)) {
                    //search matches, add to list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            //empty or null, make original list/result
            results.count = filterList.size();
            results.values = filterList;
        }
            return results; //dont miss it
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        //apply filter changes
        adapterPdfUser.arrayListPdf = (ArrayList<ModelFilePdf>) filterResults.values;

        //notify changes
        adapterPdfUser.notifyDataSetChanged();
    }
}
