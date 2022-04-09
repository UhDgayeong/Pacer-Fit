package com.example.project.Ranking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.project.R;
import com.google.android.material.tabs.TabLayout;

public class PedoRankingFragment extends Fragment {


    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pedo_ranking, container, false);

        tabLayout = v.findViewById(R.id.pedo_tabLayout);
        viewPager = v.findViewById(R.id.pedo_ranking_vp);

        tabLayout.setupWithViewPager(viewPager);

        RankingVPAdapter rankingVPAdapter = new RankingVPAdapter(getActivity().getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        rankingVPAdapter.addFragment(new WeekRanking(), "주간랭킹");
        rankingVPAdapter.addFragment(new MonthRanking(), "월간랭킹");
        rankingVPAdapter.addFragment(new TopRanking(), "역대랭킹");
        viewPager.setAdapter(rankingVPAdapter);
        return v;
    }
}
