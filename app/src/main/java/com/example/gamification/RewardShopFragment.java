package com.example.gamification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gamification.model.Reward;

import java.util.ArrayList;
import java.util.List;

public class RewardShopFragment extends Fragment {

    ListView rewardListView;
    List<Reward> rewardList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rewardshop, container, false);
        rewardListView = view.findViewById(R.id.rewardListView);

        rewardList.add(new Reward("Apprentice's Hat", "A magical hat that boosts focus +5%.", 100, 1, R.drawable.hat));
        rewardList.add(new Reward("Leather Cloak", "Stylish and warm. Increases luck.", 150, 2, R.drawable.cloak));
        rewardList.add(new Reward("Mana Potion", "Restores magical energy.", 120, 2, R.drawable.mana));
        rewardList.add(new Reward("Silver Sword", "Lightweight sword for beginner knights.", 200, 3, R.drawable.sword));
        rewardList.add(new Reward("Crossbow", "Compact and powerful. Deals high piercing damage.", 350, 4, R.drawable.crossbow));
        rewardList.add(new Reward("Longbow", "Great for long-range attacks. Improves accuracy.", 300, 4, R.drawable.bow));
        rewardList.add(new Reward("Battle Axe", "Heavy weapon that delivers massive damage.", 450, 6, R.drawable.axe));
        rewardList.add(new Reward("Knight's Shield", "Provides +15% damage reduction.", 300, 5, R.drawable.shield));
        rewardList.add(new Reward("Dragon Boots", "Increases speed. Roars when you run.", 300, 5, R.drawable.boots));
        rewardList.add(new Reward("Wizard's Staff", "Grants +10 intelligence. Glows in the dark.", 400, 6, R.drawable.staff));
        rewardList.add(new Reward("Golden Armor", "Legendary gear. Only for true heroes.", 600, 8, R.drawable.armor));

        rewardListView.setAdapter(new RewardAdapter(getContext(), rewardList));
        return view;
    }
}
