package com.example.project.Main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.project.Pedo.PedoRecordRequest;
import com.example.project.Pedo.StepCounterActivity;
import com.example.project.R;
import com.ramotion.foldingcell.FoldingCell;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageFragment extends Fragment {

    String bestCalorie_Steps;
    String bestCalorie_Km;
    String bestTime_Steps;
    String bestTime_Km;
    String bestTime;
    String userID;
    String userName;
    String bestCalorie;
    String bestSteps;
    String bestKm;
    int userProfileNum;
    int hour, minutes;

    FoldingCell foldingCell;
    Float userHeight;
    Float userWeight;


    int[] ProfileDrawable = {
            R.drawable.profile_default, R.drawable.profile_man, R.drawable.profile_man_beard, R.drawable.profile_man_cap,
            R.drawable.profile_man_hat, R.drawable.profile_man_hood, R.drawable.profile_man_horn, R.drawable.profile_man_round,
            R.drawable.profile_man_suit, R.drawable.profile_man_sunglass, R.drawable.profile_woman_glasses, R.drawable.profile_woman_neck,
            R.drawable.profile_woman_old, R.drawable.profile_woman_scarf
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_mypage_fragment, container, false);

        TextView myID = v.findViewById(R.id.mypageId);

        TextView maxStep = v.findViewById(R.id.maxStep);
        TextView maxKm = v.findViewById(R.id.maxKm);
        TextView maxKcal = v.findViewById(R.id.maxKcal);
        TextView maxTime = v.findViewById(R.id.maxTime);
        CircleImageView imageView = v.findViewById(R.id.circleImageView);

        TextView heightTxt = v.findViewById(R.id.heightTxt);
        TextView weightTxt = v.findViewById(R.id.weightTxt);
        TextView bmiTxt = v.findViewById(R.id.bmiTxt);

        Intent intent = getActivity().getIntent();
        userID = intent.getStringExtra("userID");
        userName = intent.getStringExtra("userName");
        userProfileNum = intent.getIntExtra("userProfileNum",0);
        userHeight = Float.valueOf(intent.getStringExtra("userHeight"));
        userWeight = Float.valueOf(intent.getStringExtra("userWeight"));

        imageView.setImageResource(ProfileDrawable[userProfileNum]);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    System.out.println("========================" + response);
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) { // 만보기 클릭시
                        bestSteps = jsonObject.getString("bestSteps");
                        bestKm = jsonObject.getString("bestKm");
                        bestTime_Km = jsonObject.getString("bestTime(km)");
                        bestCalorie_Km = jsonObject.getString("bestCalorie(km)");
                        bestTime_Steps = jsonObject.getString("bestTime(steps)");
                        bestCalorie_Steps = jsonObject.getString("bestCalorie(steps)");

                        myID.setText(userName); // user이름 설정해주기
                        
                        //칼로리
                        if (Float.parseFloat(bestCalorie_Km) > Float.parseFloat(bestCalorie_Steps))
                            bestCalorie = bestCalorie_Km;
                        else
                            bestCalorie = bestCalorie_Steps;

                        //시간
                        if (Integer.parseInt(bestTime_Km) > Integer.parseInt(bestTime_Steps))
                            bestTime = bestTime_Km;
                        else
                            bestTime = bestTime_Steps;
                        minutes = Integer.parseInt(bestTime) / 60;
                        hour = minutes / 60;
                        minutes %= 60;

                        //만보기 기록
                        maxStep.setText(bestSteps); // DB에서 불러온 값으로 바꾸기
                        //거리 기록
                        maxKm.setText(bestKm + "km"); // DB에서 불러온 값으로 바꾸기
                        //칼로리 기록
                        maxKcal.setText(bestCalorie + "kcal"); // DB에서 불러온 값으로 바꾸기
                        //시간 기록
                        maxTime.setText(hour + "시간 " + minutes + "분"); // DB에서 불러온 값으로 바꾸기
                    } else { // 로그인에 실패한 경우
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        MyPageBestRecordRequest myPageBestRecordRequest = new MyPageBestRecordRequest(userID, responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(myPageBestRecordRequest);

        foldingCell = (FoldingCell) v.findViewById(R.id.folding_cell);
        foldingCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foldingCell.toggle(false);
            }
        });

        heightTxt.setText("키 : " + userHeight + "cm");
        weightTxt.setText("몸무게 : " + userWeight + "kg");
        double bmi = userWeight / ((userHeight*0.01)*(userHeight*0.01));
        bmi = Math.round(bmi*100)/100.0; // 소수점 아래 둘째자리가지 반올림
        bmiTxt.setText("bmi : " + bmi);


        return v;
    }

}