package com.example.gamification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gamification.model.Reward;

import java.util.List;

public class RewardAdapter extends BaseAdapter {

    private final Context context;
    private final List<Reward> rewardList;

    public RewardAdapter(Context context, List<Reward> rewardList) {
        this.context = context;
        this.rewardList = rewardList;
    }

    @Override
    public int getCount() {
        return rewardList.size();
    }

    @Override
    public Object getItem(int position) {
        return rewardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rewardList.get(position).rewardId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Reward reward = rewardList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.reward_item, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.rewardIcon);
        TextView name = convertView.findViewById(R.id.rewardName);
        TextView cost = convertView.findViewById(R.id.rewardCost);
        Button redeemBtn = convertView.findViewById(R.id.redeemBtn);

        icon.setImageResource(reward.getImageId());
        name.setText(reward.getName());
        cost.setText(reward.getRequiredCoins() + " coins");

        redeemBtn.setOnClickListener(v -> {
            Toast.makeText(context,
                    "Redeemed: " + reward.getName(),
                    Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}

