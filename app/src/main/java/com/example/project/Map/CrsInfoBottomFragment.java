package com.example.project.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.project.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CrsInfoBottomFragment extends BottomSheetDialogFragment {
    private Context context;
    private String name;
    private String summary;
    private String time;
    private String level;
    private String dist;
//    private GPXBottomDialogAdapter adapter;
//    private RecyclerView recyclerView;

    public CrsInfoBottomFragment(Context context, String name, String summary, String time, String level, String dist) {
        this.context = context;
        this.name = name;
        this.summary = summary;
        this.time = time;
        this.level = level;
        this.dist = dist;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_crsinfo_bottom_fragment, container, false);

//        TextView crsName = view.findViewById(R.id.crs_Name);
//        TextView crsSummary = view.findViewById(R.id.crs_summary);
//        TextView crsTime = view.findViewById(R.id.crs_time);
//        TextView crsLevel = view.findViewById(R.id.crs_level);
//        TextView crsDist = view.findViewById(R.id.crs_dist);
//        crsName.setText(name);
//        crsSummary.setText(summary);
//        crsTime.setText(time);
//        crsLevel.setText(level);
//        crsDist.setText(dist);

        return view;
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        ((RecordMapActivity) getActivity()).dismissCrs();

    }
}