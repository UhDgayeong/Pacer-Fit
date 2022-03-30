package com.example.project.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.project.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.logging.Logger;

public class RecordFragment extends Fragment {

    ImageButton helpBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.record_circle_fragment, container, false);
        helpBtn = view.findViewById(R.id.helpBtn);
        helpBtn.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View map) {
                HelpFragment helpFragment = new HelpFragment(getActivity().getApplicationContext());
                helpFragment.show(getActivity().getSupportFragmentManager(), helpFragment.getTag());
            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}
