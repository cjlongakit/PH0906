package com.example.ph906_spalshscreen.ui.letters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ph906_spalshscreen.R;

import java.util.List;

public class    LettersAdapter extends RecyclerView.Adapter<LettersAdapter.LetterViewHolder> {

    private List<Letter> letterList;

    public LettersAdapter(List<Letter> letterList) {
        this.letterList = letterList;
    }

    @NonNull
    @Override
    public LetterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.letter_item, parent, false);
        return new LetterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LetterViewHolder holder, int position) {
        Letter letter = letterList.get(position);
        holder.tvPh906.setText(letter.getPh906());
        holder.tvFullName.setText(letter.getFullName());
        holder.tvAddress.setText(letter.getAddress());
        holder.tvType.setText(letter.getType());
        holder.tvDeadline.setText(letter.getDeadline());
        holder.tvStatus.setText(letter.getStatus());
        // Set status background color based on value
        String status = letter.getStatus();
        if ("ON HAND".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_onhand); // blue
        } else if ("OUTDATED".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_outdated); // red
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending); // gray
        }
    }

    @Override
    public int getItemCount() {
        return letterList.size();
    }

    static class LetterViewHolder extends RecyclerView.ViewHolder {
        TextView tvPh906, tvFullName, tvAddress, tvType, tvDeadline, tvStatus;
        public LetterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPh906 = itemView.findViewById(R.id.tvPh906);
            tvFullName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvType = itemView.findViewById(R.id.tvType);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
