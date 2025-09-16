package com.example.ph906_spalshscreen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.models.Master;

import java.util.List;

public class MasterAdapter extends RecyclerView.Adapter<MasterAdapter.MasterViewHolder> {

    private List<Master> masterList;

    public MasterAdapter(List<Master> masterList) {
        this.masterList = masterList;
    }

    @NonNull
    @Override
    public MasterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_master, parent, false);
        return new MasterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MasterViewHolder holder, int position) {
        Master master = masterList.get(position);
        holder.tvName.setText(master.getName());
        holder.tvDetails.setText(master.getDetails());
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

    static class MasterViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;

        MasterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}
