package com.example.mindcheckdatacollectionapp.ui.theme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {
    private ArrayList<String> journalEntries;
    public JournalAdapter(ArrayList<String> journalEntries) {
        this.journalEntries = journalEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int itemNumber = position + 1;
        String entry = journalEntries.get(position);
        holder.journalTextView.setText(itemNumber + ". " + entry + "\n");
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView journalTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            journalTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
