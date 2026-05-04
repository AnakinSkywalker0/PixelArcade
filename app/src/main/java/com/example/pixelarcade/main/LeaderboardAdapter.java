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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entryList.get(position);

        int rank = entry.getRank();
        boolean isMe = entry.isCurrentUser();

        // ── Medal vs. number ──────────────────────────────────────────────
        if (rank == 1) {
            holder.tvMedal.setText("🥇");
            holder.tvMedal.setVisibility(View.VISIBLE);
            holder.tvRank.setVisibility(View.GONE);
        } else if (rank == 2) {
            holder.tvMedal.setText("🥈");
            holder.tvMedal.setVisibility(View.VISIBLE);
            holder.tvRank.setVisibility(View.GONE);
        } else if (rank == 3) {
            holder.tvMedal.setText("🥉");
            holder.tvMedal.setVisibility(View.VISIBLE);
            holder.tvRank.setVisibility(View.GONE);
        } else {
            holder.tvMedal.setVisibility(View.GONE);
            holder.tvRank.setVisibility(View.VISIBLE);
            holder.tvRank.setText(String.valueOf(rank));
            holder.tvRank.setTextColor(isMe ? 0xFFF6C547 : 0xFF5F564D);
        }

        // ── Name & YOU badge ──────────────────────────────────────────────
        holder.tvName.setText(entry.getName());
        holder.tvYouBadge.setVisibility(isMe ? View.VISIBLE : View.GONE);

        // ── Score & Label ─────────────────────────────────────────────────
        holder.tvScore.setText(entry.getScore());
        holder.tvScoreLabel.setText(entry.getScoreLabel());

        // ── Row background & text colors by rank ──────────────────────────
        if (rank == 1) {
            // Gold row
            holder.itemView.setBackgroundColor(0xFF2A2000);
            holder.tvName.setTextColor(0xFFF6C547);
            holder.tvScore.setTextColor(0xFFF6C547);
        } else if (rank == 2) {
            // Silver row
            holder.itemView.setBackgroundColor(0xFF1A1A1F);
            holder.tvName.setTextColor(0xFFCCCCCC);
            holder.tvScore.setTextColor(0xFFCCCCCC);
        } else if (rank == 3) {
            // Bronze row
            holder.itemView.setBackgroundColor(0xFF1E1408);
            holder.tvName.setTextColor(0xFFCD7F32);
            holder.tvScore.setTextColor(0xFFCD7F32);
        } else if (isMe) {
            // My row – highlighted in arcade gold
            holder.itemView.setBackgroundResource(R.drawable.bg_pixel_card_dark);
            holder.tvName.setTextColor(0xFFFFFFFF);
            holder.tvScore.setTextColor(0xFFF6C547);
        } else {
            // Normal dark row
            holder.itemView.setBackgroundResource(R.drawable.bg_pixel_inner_dark);
            holder.tvName.setTextColor(0xFFAAAAAA);
            holder.tvScore.setTextColor(0xFFF6C547);
        }
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedal, tvRank, tvName, tvYouBadge, tvScore, tvScoreLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedal      = itemView.findViewById(R.id.tvMedal);
            tvRank       = itemView.findViewById(R.id.tvRank);
            tvName       = itemView.findViewById(R.id.tvName);
            tvYouBadge   = itemView.findViewById(R.id.tvYouBadge);
            tvScore      = itemView.findViewById(R.id.tvScore);
            tvScoreLabel = itemView.findViewById(R.id.tvScoreLabel);
        }
    }
}
