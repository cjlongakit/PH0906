package com.example.ph906_spalshscreen.ui.letters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ph906_spalshscreen.R;
import java.util.List;

public class LettersAdapter extends RecyclerView.Adapter<LettersAdapter.LetterViewHolder> {

    private List<Letter> letters;

    public LettersAdapter(List<Letter> letters) {
        this.letters = letters;
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
        Letter letter = letters.get(position);

        holder.tvPh906.setText(letter.getPh906());
        holder.tvName.setText(letter.getFullName());
        holder.tvAddress.setText(letter.getAddress());
        holder.tvType.setText(letter.getType());
        holder.tvStatus.setText(letter.getStatus());

        // Set status colors
        if ("ON HAND".equals(letter.getStatus())) {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
            holder.tvStatus.setTextColor(Color.WHITE);
        } else if ("PENDING".equals(letter.getStatus())) {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gray
            holder.tvStatus.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return letters.size();
    }

    static class LetterViewHolder extends RecyclerView.ViewHolder {
        TextView tvPh906, tvName, tvAddress, tvType, tvStatus;

        public LetterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPh906 = itemView.findViewById(R.id.tvPh906);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}