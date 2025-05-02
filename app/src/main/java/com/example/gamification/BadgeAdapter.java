package com.example.gamification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gamification.model.Badge;

import java.util.List;

public class BadgeAdapter extends BaseAdapter {

    private final Context context;
    private List<Badge> badgeList;
    private final LayoutInflater inflater;

    public BadgeAdapter(Context context, List<Badge> badgeList) {
        this.context = context;
        this.badgeList = badgeList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return badgeList != null ? badgeList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return badgeList != null ? badgeList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.badge_item, parent, false);
            holder = new ViewHolder(convertView, context);  // Pass context to ViewHolder
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (badgeList != null && position < badgeList.size()) {
            Badge badge = badgeList.get(position);
            holder.bind(badge);
        }

        return convertView;
    }

    public void updateBadges(List<Badge> newBadges) {
        this.badgeList = newBadges;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        final ImageView badgeIcon;
        final TextView badgeName;
        final TextView badgeDescription;
        final TextView badgeXpReward;
        final Context context;  // Store context reference

        ViewHolder(View view, Context context) {
            this.context = context;
            badgeIcon = view.findViewById(R.id.badgeIcon);
            badgeName = view.findViewById(R.id.badgeName);
            badgeDescription = view.findViewById(R.id.badgeDescription);
            badgeXpReward = view.findViewById(R.id.badgeXpReward);
        }

        void bind(Badge badge) {
            badgeIcon.setImageResource(badge.getImageId());
            badgeName.setText(badge.getTitle());
            badgeDescription.setText(badge.getDescription());
            badgeXpReward.setText(context.getString(R.string.xp_format, badge.getXpReward()));

            float alpha = badge.isUnlocked() ? 1.0f : 0.5f;
            badgeIcon.setAlpha(alpha);
            badgeName.setAlpha(alpha);
            badgeDescription.setAlpha(alpha);
        }
    }
}