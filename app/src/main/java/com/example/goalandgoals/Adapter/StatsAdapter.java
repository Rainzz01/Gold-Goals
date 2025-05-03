package com.example.goalandgoals.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goalandgoals.Model.StatItem;
import com.example.goalandgoals.R;
import java.util.List;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.StatsViewHolder> {

    private final List<StatItem> statsList;

    public StatsAdapter(List<StatItem> statsList) {
        this.statsList = statsList;
    }

    @NonNull
    @Override
    public StatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat, parent, false);
        return new StatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
        StatItem stat = statsList.get(position);
        holder.titleTextView.setText(stat.getTitle());
        holder.valueTextView.setText(stat.getValue());
    }

    @Override
    public int getItemCount() {
        return statsList.size();
    }

    static class StatsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView valueTextView;

        StatsViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.stat_title);
            valueTextView = itemView.findViewById(R.id.stat_value);
        }
    }
}