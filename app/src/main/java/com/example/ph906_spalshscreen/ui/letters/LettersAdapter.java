package com.example.ph906_spalshscreen.ui.letters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ph906_spalshscreen.R;

import java.util.List;

public class LettersAdapter extends RecyclerView.Adapter<LettersAdapter.LetterViewHolder> {

    private List<Letter> letterList;

    public LettersAdapter(List<Letter> letterList) {
        this.letterList = letterList;
    }

    // Helper requested by user: map status background drawables to color resources
    // Note: Using only existing drawable resource names to prevent build errors.
    private int getColorFromDrawable(int drawableRes) {
        if (drawableRes == R.drawable.bg_status_onhand) return R.color.statusOnHand;
        if (drawableRes == R.drawable.bg_status_outdated) return R.color.statusOutdated;
        if (drawableRes == R.drawable.bg_status_turnin) return R.color.statusTurnedIn;
        if (drawableRes == R.drawable.bg_status_late) return R.color.statusLate;
        // default
        return R.color.statusPending;
    }

    @NonNull
    @Override
    public LetterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_letter, parent, false); // ✅ matches your file
        return new LetterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LetterViewHolder holder, int position) {
        Letter letter = letterList.get(position);

        // ✅ Safe null-handling with defaults
        String ph906 = safeText(letter.getPh906());
        String fullName = safeText(letter.getFullName());
        String address = safeText(letter.getAddress());
        String type = safeText(letter.getType());
        String deadline = safeText(letter.getDeadline());
        String rawStatus = safeText(letter.getStatus());

        holder.tvPh906.setText(ph906);
        holder.tvFullName.setText(fullName);
        holder.tvAddress.setText(address);
        holder.tvType.setText(type);
        holder.tvDeadline.setText(deadline);
        holder.tvStatus.setText(rawStatus);

        // ✅ Normalize status then decide color
        String status = normalizeStatus(rawStatus);
        int colorRes;
        switch (status) {
            case "ON HAND":
                colorRes = R.color.statusOnHand;
                break;
            case "OUTDATED":
                colorRes = R.color.statusOutdated;
                break;
            case "TURNED IN":       // standard
            case "TURN IN":         // common variant from backend/UI
            case "TURNED IN ":
            case "TURN-IN":
            case "TURNEDIN":
            case "TURNIN":
                colorRes = R.color.statusTurnedIn;
                break;
            case "TURN IN LATE":
            case "TURNED IN LATE":
                colorRes = R.color.statusLate;
                break;
            case "PENDING":
            default:
                colorRes = R.color.statusPending;
                break;
        }

        int bgColor = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        holder.tvStatus.setBackgroundColor(bgColor);

        // ✅ Auto-contrast for readability
        double luminance = (0.299 * Color.red(bgColor) +
                0.587 * Color.green(bgColor) +
                0.114 * Color.blue(bgColor)) / 255.0;
        holder.tvStatus.setTextColor(luminance < 0.5 ? Color.WHITE : Color.BLACK);
    }

    @Override
    public int getItemCount() {
        return letterList != null ? letterList.size() : 0; // ✅ safe
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

    // ✅ Helper method: avoid null text
    private String safeText(String text) {
        return (text == null || text.trim().isEmpty()) ? "N/A" : text;
    }

    // ✅ Normalize status for robust matching
    private String normalizeStatus(String s) {
        if (s == null) return "";
        String x = s.trim().toUpperCase();
        x = x.replace('_', ' ').replace('-', ' ');
        x = x.replaceAll("\\s+", " ");
        if ("TURN IN".equals(x)) x = "TURNED IN";
        return x;
    }
}
