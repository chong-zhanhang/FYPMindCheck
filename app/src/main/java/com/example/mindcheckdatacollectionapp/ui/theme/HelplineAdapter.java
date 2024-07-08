package com.example.mindcheckdatacollectionapp.ui.theme;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mindcheckdatacollectionapp.R;

import java.util.List;


public class HelplineAdapter extends RecyclerView.Adapter<HelplineAdapter.HelplineViewHolder> {
    private List<HelplineItem> helplineList;
    public HelplineAdapter(List<HelplineItem> helplineList) {
        this.helplineList = helplineList;
    }

    @Override
    public HelplineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.helpline_list_item, parent, false);
        return new HelplineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HelplineViewHolder holder, int position) {
        HelplineItem item = helplineList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return helplineList.size();
    }

    public static class HelplineViewHolder extends RecyclerView.ViewHolder {
        private TextView helplineName;
        private TextView helplineNumber;
        private TextView helplineWebsite;

        public HelplineViewHolder(View itemView) {
            super(itemView);
            helplineName = itemView.findViewById(R.id.helplineName);
            helplineNumber = itemView.findViewById(R.id.helplineNumber);
            helplineWebsite = itemView.findViewById(R.id.helplineWebsite);

            helplineNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = helplineNumber.getText().toString();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    v.getContext().startActivity(intent);
                }
            });

            helplineWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = helplineWebsite.getText().toString();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    v.getContext().startActivity(intent);
                }
            });
        }

        public void bind(HelplineItem item) {
            helplineName.setText(item.getName());
            helplineNumber.setText(item.getNumber());
            helplineWebsite.setText(item.getWebsite());
        }
    }
}
