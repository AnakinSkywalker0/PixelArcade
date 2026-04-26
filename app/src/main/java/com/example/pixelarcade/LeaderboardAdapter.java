package com.example.pixelarcade;

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
        if (rank == 1) {
            holder.tvRank.setText("▽");
            holder.tvRank.setTextColor(0xFFD4AF37); // Gold
        } else if (rank == 2) {
            holder.tvRank.setText("▽");
            holder.tvRank.setTextColor(0xFFC0C0C0); // Silver
        } else if (rank == 3) {
            holder.tvRank.setText("▽");
            holder.tvRank.setTextColor(0xFFCD7F32); // Bronze
        } else {
            holder.tvRank.setText(String.valueOf(rank));
            holder.tvRank.setTextColor(0xFF6B6055); // Default Gray
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
