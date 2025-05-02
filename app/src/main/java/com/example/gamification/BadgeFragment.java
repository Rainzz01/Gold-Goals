package com.example.gamification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gamification.model.Badge;

import java.util.ArrayList;
import java.util.List;

public class BadgeFragment extends Fragment {

    private GridView badgeGrid;
    private final List<Badge> badgeList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_badge, container, false);
        badgeGrid = view.findViewById(R.id.badgeGrid);

        initializeBadges();
        badgeGrid.setAdapter(new BadgeAdapter(requireContext(), badgeList));

        return view;
    }

    private void initializeBadges() {
        int icon = R.drawable.badge_sample; // Replace with your image

        badgeList.clear();
        badgeList.add(new Badge("Novice Adventurer", "Complete your first quest", icon, 50, true));
        badgeList.add(new Badge("5-Day Streak", "Complete 5 days in a row", icon, 75, false));
        badgeList.add(new Badge("Early Bird", "Task before 8 AM", icon, 100, true));
        badgeList.add(new Badge("Weekly Warrior", "Finish all weekly tasks", icon, 125, false));
        badgeList.add(new Badge("Collector", "Earn 500 coins", icon, 150, true));
        badgeList.add(new Badge("Master Quest", "Complete 10 hard quests", icon, 200, false));
    }
}
