package com.example.pixelarcade.main;

import com.example.pixelarcade.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardEntry> entryList;

    public LeaderboardAdapter(List<LeaderboardEntry> entryList) {
        this.entryList = entryList;
    }

    public void updateData(List<LeaderboardEntry> newList) {
        this.entryList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entryList.get(position);
        
        holder.tvName.setText(entry.getName());
        holder.tvScore.setText(entry.getScore());
        
        int rank = entry.getRank();
        holder.tvRank.setText(String.valueOf(rank));
        if (rank == 1) {
            holder.tvRank.setTextColor(0xFFD4AF37); // Gold
        } else if (rank == 2) {
            holder.tvRank.setTextColor(0xFFC0C0C0); // Silver
        } else if (rank == 3) {
            holder.tvRank.setTextColor(0xFFCD7F32); // Bronze
        } else {
            holder.tvRank.setTextColor(0xFF5F564D); // High contrast Brown
        }

        // Highlight current user's row
        if (entry.isCurrentUser()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_rank_bar); // Solid brown bar
            holder.tvName.setTextColor(0xFFFFFFFF); // White text on dark bar
            holder.tvScore.setTextColor(0xFFF6C547); // Gold score
            if (rank > 3) holder.tvRank.setTextColor(0xFFFFFFFF);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_leaderboard_row); // Default row
            holder.tvName.setTextColor(0xFF1B1C22); // Deep Black
            holder.tvScore.setTextColor(0xFF5F564D); // Deep Brown
        }
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
