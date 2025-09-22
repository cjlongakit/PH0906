package com.example.ph906_spalshscreen.ui.letters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ph906_spalshscreen.R;

import java.util.List;

public class LettersAdapter extends RecyclerView.Adapter<LettersAdapter.LetterViewHolder> {

    private List<Letter> letterList;

    public LettersAdapter(List<Letter> letterList) {
        this.letterList = letterList;
    }

    @NonNull
    @Override
    public LetterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.letter_item, parent, false); // Change from item_letter to letter_item
        return new LetterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LetterViewHolder holder, int position) {
        Letter letter = letterList.get(position);
        holder.tvPh906.setText(letter.getPh906());
        holder.tvFullName.setText(letter.getFullName());
        holder.tvAddress.setText(letter.getAddress());
        holder.tvType.setText(letter.getType());
        holder.tvStatus.setText(letter.getStatus()); // Remove deadline for now since table doesn't match
    }

    @Override
    public int getItemCount() {
        return letterList.size();
    }

    static class LetterViewHolder extends RecyclerView.ViewHolder {
        TextView tvPh906, tvFullName, tvAddress, tvType, tvStatus; // Remove tvDeadline

        public LetterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPh906 = itemView.findViewById(R.id.tvPh906);
            tvFullName = itemView.findViewById(R.id.tvName); // Change from tvFullName to tvName
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
