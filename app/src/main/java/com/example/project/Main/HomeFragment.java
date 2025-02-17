package com.example.project.Main;

import static com.example.project.Weather.TransLocalPoint.TO_GRID;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.project.Dist.DistDetailRecordFragment;
import com.example.project.Dist.KmLineRecordRequest;
import com.example.project.Formatter.OneMonthXAxisValueFormatter;
import com.example.project.Map.RecordMapActivity;
import com.example.project.Pedo.DetailRecordFragment;
import com.example.project.Pedo.PedoBarRecordRequest;
import com.example.project.Pedo.PedoRecordRequest;
import com.example.project.Pedo.StepCounterActivity;
import com.example.project.R;
import com.example.project.Weather.GpsTrackerService;
import com.example.project.Weather.RequestHttpConnection;
import com.example.project.Weather.TransLocalPoint;
import com.example.project.Weather.Weather;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {
    private TextView weatherInfo;
    private TextView temperature;
    private ImageView weatherInfo_Image;
    private TransLocalPoint transLocalPoint;
    private GpsTrackerService gpsTracker;
    private String address = ""; // x,y는 격자x,y좌표
    String weather = ""; // 날씨 결과
    String tmperature = ""; // 온도 결과

    private BarChart barChart; // 막대 그래프
    private LineChart lineChart; // 꺾은선 그래프

    private Button moreBarChartBtn;
    private Button moreLineChartBtn;

    String userID;
    String userWeight;
    String today_userPedoRecord;
    String today_userPedoTimeRecord;
    String today_userPedoCalorieRecord;
    String date_concat;
    String[] PedoRecord31 = new String[31];
    String[] KmRecord31 = new String[31];
    String[] beforeMonth31 = new String[31];
    String pedo_max, km_max;
    int green,gray;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_home_fragment, container, false);
        Context ct = container.getContext();
        TextView location = v.findViewById(R.id.location);
        location.setSelected(true);
        weatherInfo = v.findViewById(R.id.weather);
        weatherInfo.setSelected(true);
        weatherInfo_Image = v.findViewById(R.id.weather_image);
        temperature = v.findViewById(R.id.temperature);
        //Button Km_button = v.findViewById(R.id.Km_button);
        LinearLayout Km_button = v.findViewById(R.id.km_layout_btn);
        //null포인터 오류로 빼놈

        green = ContextCompat.getColor(getContext(), R.color.green_project);
        gray = ContextCompat.getColor(getContext(), R.color.gray_project);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate now = null;
            now = LocalDate.now();

            int monthValue = now.getMonthValue();
            int dayOfMonth = now.getDayOfMonth();
            date_concat = monthValue + "m" + dayOfMonth + "d";
        }
        //한달 전 데이터 배열
        for (int i = 30, j = 0; i >= 0; i--, j++) {
            Calendar month = Calendar.getInstance();
            month.add(Calendar.DATE, -i);
            beforeMonth31[j] = new java.text.SimpleDateFormat("M'm'dd'd'").format(month.getTime());
            if (beforeMonth31[j].substring(2, 3).equals("0")) {
                beforeMonth31[j] = beforeMonth31[j].replaceFirst("0", "");
            }
//            System.out.println(beforeMonth31[j]);
        }

        Intent receiveIntent = getActivity().getIntent();
        userID = receiveIntent.getStringExtra("userID");
        userWeight = receiveIntent.getStringExtra("userWeight");
        pedo_max = receiveIntent.getStringExtra("pedo_max");
        km_max = receiveIntent.getStringExtra("km_max");

        Km_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View map) {
                Intent intent = new Intent(getActivity(), RecordMapActivity.class); //Fragment -> Activity로 이동 (Map_add.java)
                //Intent intent = new Intent(getActivity(), Map_add.class);
                intent.putExtra("userWeight",userWeight);
                Log.d("weight", String.valueOf(userWeight));
                startActivity(intent);
            }
        });

        //Button pedo_button = v.findViewById(R.id.pedo_button);
        LinearLayout pedo_button = v.findViewById(R.id.pedo_layout_btn);
        pedo_button.setOnClickListener(map -> {
            // DB추가
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        System.out.println("========================" + response);
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) { // 만보기 클릭시
                            String today_PedoStepsRecord = jsonObject.getString(date_concat + ".step");
                            String today_PedoTimeRecord = jsonObject.getString(date_concat + ".time");
                            String today_PedoCalorieRecord = jsonObject.getString(date_concat + ".cal");
                            Intent intent = new Intent(getActivity(), StepCounterActivity.class); //Fragment -> Activity로 이동 (StepCounterActivity.java)
                            intent.putExtra("userID", userID);
                            intent.putExtra("userWeight", userWeight);
                            intent.putExtra("today_stepsRecord", today_PedoStepsRecord);
                            intent.putExtra("today_stepsTimeRecord", today_PedoTimeRecord);
                            intent.putExtra("today_stepsCalorieRecord", today_PedoCalorieRecord);
                            startActivity(intent);
                        } else { // 로그인에 실패한 경우
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            PedoRecordRequest pedoRecordRequest = new PedoRecordRequest(userID, responseListener);
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            queue.add(pedoRecordRequest);
            //DB추가 주석 없앨때 아래 3줄 지우기


//            intent.putExtra("userWeight",userWeight);
//            intent.putExtra("userStepsRecord",today_userPedoRecord);
//            intent.putExtra("userStepsTimeRecord",today_userPedoTimeRecord);
//            intent.putExtra("userStepsCalorieRecord",today_userPedoCalorieRecord);
//            System.out.println("userKg??????????????at HomeFrag"+userWeight);
//            System.out.println("userStepsRecord??????????????at HomeFrag"+today_userPedoRecord);
//            System.out.println("userStepsRecord??????????????at HomeFrag"+today_userPedoTimeRecord);
//            System.out.println("userStepsRecord??????????????at HomeFrag"+today_userPedoCalorieRecord);
//            //BottomNavigation액티비티에서 StepCounterActivity에 userWeight값을 보냄
//            startActivity(intent);
//                Intent intent = new Intent(getActivity(), PopupPedo.class); //Fragment -> Activity로 이동 (만보기팝업)
//                startActivity(intent);
        });
        Weather weatherMethod = new Weather();
        gpsTracker = new GpsTrackerService(ct);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        address = weatherMethod.getCurrentAddress(ct, latitude, longitude);
        String[] local = address.split(" ");  //주소를 대한민국, 서울특별시, xx구 ... 로 나눔
        // String localName = local[2]; //xx구 이름

        location.setText(local[1] + " " + local[2]);

        transLocalPoint = new TransLocalPoint();
        TransLocalPoint.LatXLngY tmp = transLocalPoint.convertGRID_GPS(TO_GRID, latitude, longitude);
        Log.e(">>", "x = " + tmp.x + ", y = " + tmp.y);

        String url = weatherMethod.weather(tmp.x, tmp.y);
        NetworkTask networkTask = new NetworkTask(url, null);
        networkTask.execute();

        Calendar now = Calendar.getInstance();
        int isAMorPM = now.get(Calendar.AM_PM);
        switch (isAMorPM) {
            case Calendar.AM:
                ((TextView) v.findViewById(R.id.ampm)).setText("오전");
                break;
            case Calendar.PM:
                ((TextView) v.findViewById(R.id.ampm)).setText("오후");
                break;
        }


        // <----------- 막대 그래프 ----------->
        barChart = v.findViewById(R.id.barchart);
        ArrayList<Float> barChartValues2 = new ArrayList<>();//초기화
        for (int i = 0; i < 31; i++) {
            barChartValues2.add(0f); //초기화
        }
        barchartConfigureAppearance();//초기화
        BarData barChartData2 = createBarchartData(barChartValues2);//초기화
        barChart.setData(barChartData2); //초기화
        barChart.invalidate(); //초기화

        // <----------- 꺾은선 그래프 ----------->
        lineChart = v.findViewById(R.id.linechart);
        //DB 연결 (Response)

        ArrayList<Float> lineChartValues2 = new ArrayList<>();
        // 최근 1달의 운동량 값 받아오기 -> DB 값으로 추후에 수정
        for (int i = 0; i < 31; i++) {
            //Log.d("RAND", String.valueOf(rand));
            lineChartValues2.add(0f); // 0 ~ 15,000 사이의 랜덤값
        }
        linechartConfigureAppearance();
        LineData lineChartData2 = createLinechartData(lineChartValues2);
        lineChart.setData(lineChartData2); // LineData 전달
        lineChart.invalidate(); // LineChart 갱신해 데이터 표시

        //barChart.setOnChartValueSelectedListener(this);
        Response.Listener<String> responseListener = response -> {
            try {
                System.out.println("========================" + response);
                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    System.out.println("31일치 데이터 가져오기 성공");
                    km_max = jsonObject.getString("km_max_recent");
                    pedo_max = jsonObject.getString("pedo_max_recent");
                    for (int i = 0; i < 31; i++) {
                        PedoRecord31[i] = jsonObject.getString(beforeMonth31[i]);
                    }
                    for (int i = 0; i < 31; i++) {
                        KmRecord31[i] = jsonObject.getString(beforeMonth31[i]+".km");
                    }
                    //막대
                    ArrayList<Float> barChartValues = new ArrayList<>();
                    // 최근 1달의 운동량 값 받아오기 -> DB 값으로 추후에 수정
                    for (int i = 0; i < 31; i++) {
                        float rand2 = Float.parseFloat(PedoRecord31[i]);
                        barChartValues.add(rand2); // DB값
                    }
                    BarData barChartData = createBarchartData(barChartValues);
                    barChart.setData(barChartData); // BarData 전달
                    barChart.invalidate(); // BarChart 갱신해 데이터 표시

                    //꺾은선
                    ArrayList<Float> lineChartValues = new ArrayList<>();
                    // 최근 1달의 운동량 값 받아오기 -> DB 값으로 추후에 수정
                    for (int i = 0; i < 31; i++) {
                        float rand2 = Float.parseFloat(KmRecord31[i]);
                        //Log.d("RAND", String.valueOf(rand));
                        lineChartValues.add(rand2); // 0 ~ 15,000 사이의 랜덤값
                    }
                    LineData lineChartData = createLinechartData(lineChartValues);
                    lineChart.setData(lineChartData); // LineData 전달
                    lineChart.invalidate(); // LineChart 갱신해 데이터 표시
                } else { //실패한 경우
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };
        MainPedoRecentRecordRequest mainPedoRecentRecordRequest = new MainPedoRecentRecordRequest(userID, responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(mainPedoRecentRecordRequest);
        moreBarChartBtn = v.findViewById(R.id.moreBarChartBtn);
        moreBarChartBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Response.Listener<String> responseListener = response -> {
                    try {
                        System.out.println("========================Click->" + response);
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            Bundle bundle = new Bundle(); // 번들을 통해 값 전달
                            bundle.putString("userID",userID);//번들에 넘길 값 저장
                            for(int i=0; i<7; i++) {
                                bundle.putString(i+".day_step",jsonObject.getString(i+".day_step"));
                                bundle.putString(i+".day_time",jsonObject.getString(i+".day_time"));
                            }
                            bundle.putString("pedo_max_day",jsonObject.getString("pedo_max_day"));
                            for(int i=0; i<31; i++) {
                                bundle.putString(i+".month_step",jsonObject.getString(i+".month_step"));
                                bundle.putString(i+".month_time",jsonObject.getString(i+".month_time"));
                            }
                            bundle.putString("pedo_max_month",jsonObject.getString("pedo_max_month"));
                            for(int i=0; i<24; i++){
                                bundle.putString(i+".week_step",jsonObject.getString(i+".week_step"));
                                bundle.putString(i+".week_time",jsonObject.getString(i+".week_time"));
                            }
                            bundle.putString("pedo_max_180",jsonObject.getString("pedo_max_180"));
                            for(int i=0; i<12; i++){
                                bundle.putString(i+".year_step",jsonObject.getString(i+".year_step"));
                                bundle.putString(i+".year_time",jsonObject.getString(i+".year_time"));
                            }
                            bundle.putString("pedo_max_year",jsonObject.getString("pedo_max_year"));
                            // 더보기 탭으로 이동. 이전 프래그먼트로 돌아갈 수 있도록 설정
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            DetailRecordFragment detailRecordFragment = new DetailRecordFragment();//프래그먼트2 선언
                            detailRecordFragment.setArguments(bundle);//번들을 프래그먼트2로 보낼 준비
                            fm.beginTransaction()
                                    .replace(R.id.frame_container, detailRecordFragment)
                                    .addToBackStack(null).commit();
                        } else { //실패한 경우
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                };
                PedoBarRecordRequest pedoBarRecordRequest = new PedoBarRecordRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                queue.add(pedoBarRecordRequest);
                moreBarChartBtn.setEnabled(false);
            }
        });
        moreLineChartBtn = v.findViewById(R.id.moreLineChartBtn);
        moreLineChartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Response.Listener<String> responseListener = response -> {
                    try {
                        System.out.println("========================Click->" + response);
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            Bundle bundle = new Bundle(); // 번들을 통해 값 전달
                            bundle.putString("userID",userID);//번들에 넘길 값 저장
                            for(int i=0; i<7; i++) {
                                bundle.putString(i+".day_km",jsonObject.getString(i+".day_km"));
                                bundle.putString(i+".day_time",jsonObject.getString(i+".day_time"));
                            }
                            bundle.putString("km_max_day",jsonObject.getString("km_max_day"));
                            for(int i=0; i<31; i++) {
                                bundle.putString(i+".month_km",jsonObject.getString(i+".month_km"));
                                bundle.putString(i+".month_time",jsonObject.getString(i+".month_time"));
                            }
                            bundle.putString("km_max_month",jsonObject.getString("km_max_month"));
                            for(int i=0; i<24; i++){
                                bundle.putString(i+".week_km",jsonObject.getString(i+".week_km"));
                                bundle.putString(i+".week_time",jsonObject.getString(i+".week_time"));
                            }
                            bundle.putString("km_max_180",jsonObject.getString("km_max_180"));
                            for(int i=0; i<12; i++){
                                bundle.putString(i+".year_km",jsonObject.getString(i+".year_km"));
                                bundle.putString(i+".year_time",jsonObject.getString(i+".year_time"));
                            }
                            bundle.putString("km_max_year",jsonObject.getString("km_max_year"));
                            // 더보기 탭으로 이동. 이전 프래그먼트로 돌아갈 수 있도록 설정
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            DistDetailRecordFragment distDetailRecordFragment = new DistDetailRecordFragment();//프래그먼트2 선언
                            distDetailRecordFragment.setArguments(bundle);//번들을 프래그먼트2로 보낼 준비
                            fm.beginTransaction()
                                    .replace(R.id.frame_container, distDetailRecordFragment)
                                    .addToBackStack(null).commit();
                        } else { //실패한 경우
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                };
                KmLineRecordRequest kmLineRecordRequest = new KmLineRecordRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                queue.add(kmLineRecordRequest);
                moreLineChartBtn.setEnabled(false);
            }
        });

        return v;

    } // onCreateView

    // 막대그래프 각종 설정
    private void barchartConfigureAppearance() {
        barChart.setTouchEnabled(false); // 터치 유무
        barChart.setPinchZoom(false); // 두 손가락으로 줌인,줌아웃 설정
        barChart.setDrawBarShadow(false); // 그래프의 그림자
        barChart.setDrawGridBackground(false); // 격자무늬 유무
        barChart.getLegend().setEnabled(false); // legend는 차트의 범례
        barChart.getDescription().setEnabled(false); // 우측 하단의 DescriptionLabel 삭제
        barChart.animateY(1500); // 밑에서부터 올라오는 애니메이션 적용
        barChart.animateX(1500); // 왼쪽-오른쪽 방향의 애니메이션 적용
        //barChart.setDescription();
        //barChart.setExtraOffsets(10f, 0f, 40f, 0f);
        // x축 설정(막대그래프 기준 아래쪽)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setAxisMinimum(-0.5f); // 라인그래프만 x축 좌측 여유 공간 필요
        xAxis.setAxisMaximum(30.5f); // x : 0, 1, ... , 30 -> 31개
        xAxis.setDrawAxisLine(false); // 축 그리기 설정
        xAxis.setLabelCount(31); // 이걸 써야 setGranularity가 작동함
        xAxis.setGranularity(1f); // 간격 설정(표시되는 값) -> OneMonthXAxisValueFormatter.java에서 값 번갈아서 나오게 커스텀
        xAxis.setTextSize(13f);
        xAxis.setTextColor(Color.parseColor("#909090"));
        xAxis.setDrawGridLines(false); // 격자
        //xAxis.setGridLineWidth(25f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 데이터 표시 위치
        xAxis.setValueFormatter(new OneMonthXAxisValueFormatter());
        // X축 폰트 설정
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/nanumsquareroundeb.ttf");
        xAxis.setTypeface(tf);

        // y축 설정(막대그래프 기준 왼쪽)
        YAxis axisLeft = barChart.getAxisLeft();
        float pedo_max_float = 0f;
        pedo_max_float = Float.parseFloat(pedo_max);

        axisLeft.setAxisMaximum(pedo_max_float); // y축 최대값 설정
        axisLeft.setAxisMinimum(0f); // y축 최소값 설정
        axisLeft.setDrawLabels(false); // 값 표기 설정
        axisLeft.setDrawGridLines(false); // 격자
        axisLeft.setDrawAxisLine(false); // 축 그리기 설정
        //axisLeft.setAxisLineColor(Color.parseColor("#FFFFFFFF")); // 축 색깔 설정
        //axisLeft.setLabelCo

        // (막대그래프 기준 오른쪽)
        YAxis axisRight = barChart.getAxisRight();
        axisRight.setDrawLabels(false); // 값 표기 설정
        axisRight.setDrawGridLines(false); // 격자
        axisRight.setDrawAxisLine(false); // 축 그리기 설정
        //axisRight.setEnabled(false); // 오른쪽 y축을 안 보이게 해줌
    }

    // 꺾은선그래프 각종 설정
    private void linechartConfigureAppearance() {
        lineChart.setTouchEnabled(false);
        lineChart.setTouchEnabled(false); // 터치 유무
        lineChart.setPinchZoom(false); // 두 손가락으로 줌인,줌아웃 설정
        lineChart.setDrawGridBackground(false); // 격자무늬 유무
        lineChart.getLegend().setEnabled(false); // legend는 차트의 범례
        lineChart.getDescription().setEnabled(false); // 우측 하단의 DescriptionLabel 삭제
        //lineChart.animateY(1500); // 밑에서부터 올라오는 애니메이션 적용
        lineChart.animateX(1500); // 왼쪽-오른쪽 방향의 애니메이션 적용

        // x축 설정(꺾은선그래프 기준 아래쪽)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMaximum(30.5f); // x : 0, 1, ... , 30
        xAxis.setDrawAxisLine(false); // 축 그리기 설정
        xAxis.setLabelCount(31); // 이걸 써야 setGranularity가 작동함
        xAxis.setGranularity(1f); // 간격 설정(표시되는 값) -> OneMonthXAxisValueFormatter.java에서 값 번갈아서 나오게 커스텀
        xAxis.setTextSize(13f);
        xAxis.setTextColor(Color.parseColor("#909090"));
        xAxis.setDrawGridLines(false); // 격자
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축 데이터 표시 위치
        xAxis.setValueFormatter(new OneMonthXAxisValueFormatter());
        // X축 폰트 설정
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/nanumsquareroundeb.ttf");
        xAxis.setTypeface(tf);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        float km_max_float=0f;
        km_max_float = Float.parseFloat(km_max);

        km_max_float += km_max_float/10;
        yAxisLeft.setAxisMaximum(km_max_float); // y축 최대값 설정
        yAxisLeft.setAxisMinimum(0f); // y축 최소값 설정
        yAxisLeft.setDrawLabels(false); // 값 표기 설정
        yAxisLeft.setDrawGridLines(false); // 격자
        yAxisLeft.setDrawAxisLine(false); // 축 그리기 설정

        // (꺾은선그래프 기준 오른쪽)
        YAxis axisRight = lineChart.getAxisRight();
        axisRight.setDrawLabels(false); // 값 표기 설정
        axisRight.setDrawGridLines(false); // 격자
        axisRight.setDrawAxisLine(false); // 축 그리기 설정
    }

    // 이 함수에서 생성된 BarData를 실제 BarData 객체에 전달하고 BarChart를 갱신해 데이터를 표시
    private BarData createBarchartData(ArrayList<Float> chartValues) {
        // 1. [BarEntry] BarChart에 표시될 데이터 값 생성
        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            float x = i;
            float y = chartValues.get(i);
            values.add(new BarEntry(x, y));
        }

        // 2. [BarDataSet] 단순 데이터를 막대 모양으로 표시, BarChart의 막대 커스텀
        BarDataSet set = new BarDataSet(values, "막대그래프");
        set.setDrawIcons(false);
        set.setDrawValues(false); // 막대 위에 값 표시 설정
        set.setColor(Color.parseColor("#EB3314")); // 색상 설정(빨강)

        // 3. [BarData] 보여질 데이터 구성
        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        data.setValueTextSize(15);

        return data;
    }

    // 이 함수에서 생성된 LineData를 실제 LineData 객체에 전달하고 LineChart를 갱신해 데이터를 표시
    private LineData createLinechartData(ArrayList<Float> chartValues) {
        // 1. [Entry] LineChart에 표시될 데이터 값 생성
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            float x = i;
            float y = chartValues.get(i);
            values.add(new Entry(x, y));
        }

        // 2. [LineDataSet] 단순 데이터를 꺾은선 모양으로 표시, LineChart의 막대 커스텀
        LineDataSet set = new LineDataSet(values, "꺾은선그래프");
        set.setDrawIcons(false);
        set.setDrawValues(false);
        //널포인터 오류로 onCreate로 빼놈
//        green = ContextCompat.getColor(getContext(), R.color.green_project);
//        gray = ContextCompat.getColor(getContext(), R.color.gray_project);
        set.setFillColor(green);
        set.setColor(green);
        set.setDrawCircleHole(true);
        set.setCircleColor(green); // 바깥 원 색
        set.setCircleHoleColor(gray); // 안쪽 원 색
        set.setLineWidth(1.5f); // 선 두께
        set.setCircleSize(4f);
        set.setDrawCircles(true); //선 둥글게 만들기
        set.setDrawFilled(false); //그래프 밑부분 색칠X

        // 3. [LineData] 보여질 데이터 구성
        LineData data = new LineData(set);
        data.setValueTextSize(15);

        return data;
    }

    public String weatherJsonParser(String jsonString) throws JSONException {
        long mNow = System.currentTimeMillis();
        Date mReDate = new Date(mNow);
        SimpleDateFormat currentTime = new SimpleDateFormat("HH00");
        String mCurrentTime = String.format("%04d", (Integer.parseInt(currentTime.format(mReDate))));
        Log.i("currentTime", mCurrentTime);
        String rain = "0";

        //response 키를 가지고 데이터를 파싱
        if (jsonString != null) {
            JSONObject jsonObject1 = new JSONObject(jsonString);
            String response = jsonObject1.getString("response");

            // response 로 부터 body 찾기
            if (response != null) {
                JSONObject jsonObject2 = new JSONObject(response);
                String body = jsonObject2.getString("body");

                // body 로 부터 items 찾기
                if (body != null) {
                    JSONObject jsonObject3 = new JSONObject(body);
                    String items = jsonObject3.getString("items");


                    // itmes로 부터 itemlist 를 받기
                    if (items != null) {
                        JSONObject jsonObject4 = new JSONObject(items);
                        JSONArray jsonArray = jsonObject4.getJSONArray("item");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject4 = jsonArray.getJSONObject(i);
                            String fcstTime = jsonObject4.getString("fcstTime");
                            String fcstValue = jsonObject4.getString("fcstValue");
                            String category = jsonObject4.getString("category");

                            if (fcstTime.equals(mCurrentTime)) {
                                Log.d("ITEM", jsonObject4.toString());
                                if (category.equals("PTY")) {
                                    if (fcstValue.equals("1") || fcstValue.equals("2") || fcstValue.equals("5") || fcstValue.equals("6")) {
                                        weather = "비\n";
                                        rain = "1";
                                    } else if (fcstValue.equals("3") || fcstValue.equals("7")) {
                                        weather = "눈\n";
                                        rain = "1";
                                    } else if (fcstValue.equals("0")) {
                                        rain = "0";
                                    }
                                }
                                if (category.equals("SKY") && rain.equals("0")) {
                                    if (fcstValue.equals("1")) {
                                        weather = "맑음\n";
                                    } else if (fcstValue.equals("3")) {
                                        weather = "구름 많음\n";
                                    } else if (fcstValue.equals("4")) {
                                        weather = "흐림\n";
                                    }
                                }

                                if (category.equals("T1H")) {
                                    tmperature = fcstValue + "℃";
                                }
                            }
                        }
                    }
                }
            }
        }
        return weather + tmperature;
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;
        String result;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            RequestHttpConnection requestHttpConnection = new RequestHttpConnection();
            result = requestHttpConnection.requset(url, values); //해당 URL로부터 결과
            try {
                weatherJsonParser(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            weatherInfo.setText(weather);
            temperature.setText(tmperature);
            //doInBackground()로부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력
            if (weather.equals("맑음\n")) {
                weatherInfo_Image.setImageResource(R.color.lightgray_project);
                weatherInfo_Image.setImageResource(R.drawable.weather_sunny);
            } else if (weather.equals("비\n")) {
                weatherInfo_Image.setImageResource(R.color.lightgray_project);
                weatherInfo_Image.setImageResource(R.drawable.weather_rainy);
            } else if (weather.equals("구름 많음\n")) {
                weatherInfo_Image.setImageResource(R.color.lightgray_project);
                weatherInfo_Image.setImageResource(R.drawable.weather_heavy_cloudy);
            } else if (weather.equals("흐림\n")) {
                weatherInfo_Image.setImageResource(R.color.lightgray_project);
                weatherInfo_Image.setImageResource(R.drawable.weather_cloudy);
            } else if (weather.equals("눈\n")) {
                weatherInfo_Image.setImageResource(R.color.lightgray_project);
                weatherInfo_Image.setImageResource(R.drawable.weather_snow);
            }
        }
    }
}