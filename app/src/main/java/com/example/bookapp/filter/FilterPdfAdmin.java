package com.example.bookapp.filter;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterPdfAdmin;
import com.example.bookapp.models.ModelFilePdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    //arraylist in which we want to search
    ArrayList<ModelFilePdf> filterList;
    //adapter in which filter need to be implemented
    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelFilePdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(charSequence != null && charSequence.length() >0 ){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelFilePdf> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++){
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    //add to firebase list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        //apply fiter change
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelFilePdf>) filterResults.values;

        //notify change
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
