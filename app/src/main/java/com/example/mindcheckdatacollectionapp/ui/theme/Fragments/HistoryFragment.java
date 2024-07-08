package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mindcheckdatacollectionapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class HistoryFragment extends Fragment {
    private TextView depressedDays;
    private TextView noDataTextView;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("DEBUG", "CHARTTTT");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        depressedDays = view.findViewById(R.id.depressed_days);
        noDataTextView = view.findViewById(R.id.noDataTextViewHistory);
        BarChart chart = view.findViewById(R.id.history_chart);

        Log.d("DEBUG", "CHART");
        Bundle args = getArguments();
        if (args != null) {
            String userID = args.getString("userID");
            loadHistory(userID, chart);
        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserID = currentUser.getUid();
                loadHistory(currentUserID, chart);
            }
        }
        return view;
    }

//    private List<String> getDays() {
//        List<String> labels = new ArrayList<>();
//        for (int i = 1; i <= 14; i++) {
//            labels.add("Day " + i);
//        }
//
//        return labels;
//    }

    private List<String> generateDateList() {
        Log.d("DEBUG", "DATE1");
        List<String> dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Log.d("DEBUG", "DATE2");
        for (int i = 0; i < 14; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        Collections.reverse(dates);
        Log.d("DEBUG", String.valueOf(dates));
        return dates;
    }


    private void loadHistory(String userID, BarChart chart) {
        List<String> dates = generateDateList();
        Log.d("DEBUG", "CHART0");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("mobileUser").whereEqualTo("userID", userID);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                if (document.exists()) {
                    noDataTextView.setVisibility(View.GONE);
                    Log.d("DEBUG", "CHART1");
                    int depressedDaysCount = 0;
                    Map<Date, Boolean> sortedMap = new TreeMap<>();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M", Locale.getDefault());
                    if (document.contains("depressionHistory")) {
                        Map<String, Boolean> dateMap = (Map<String, Boolean>) document.getData().get("depressionHistory");
                        Log.d("DEBUG", "CHART2");
                        for (Map.Entry<String, Boolean> entry : dateMap.entrySet()) {
                            try {
                                Date date = dateFormat.parse(entry.getKey());
                                sortedMap.put(date, entry.getValue());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d("DEBUG", "CHART3");
                        List<BarEntry> entries = new ArrayList<>();
                        //List<String> dates = new ArrayList<>();
                        List<Integer> colors = new ArrayList<>();

                        if (sortedMap == null) {
                            Log.d("DEBUG", "sortedMap is null");
                            return; // Or handle appropriately
                        }

                        if (dates == null) {
                            Log.d("DEBUG", "dates is null");
                            return; // Or handle appropriately
                        }

                        int index = 0;

                        for (String dateStr : dates) {
                            try {
                                Date date = dateFormat.parse(dateStr);
                                Log.d("DEBUG", "Processing date: " + date); // Check the date being processed
                                float barHeight = 0.0f;
                                if (sortedMap.containsKey(date)) {
                                    Log.d("DEBUG", "Entering loop");
                                    Boolean value = sortedMap.get(date);
                                    Log.d("DEBUG", "Date found in map, value: " + sortedMap.get(date));
                                    if (value != null) {
                                        barHeight = 1.0f;
                                        if(value) {
                                            depressedDaysCount++;
                                        }
                                        colors.add(value ? Color.RED : Color.GREEN);
                                    } else {
                                        colors.add(Color.TRANSPARENT); // Handle null case
                                        Log.d("DEBUG", "Null value for date: " + date);
                                    }
                                } else {
                                    colors.add(Color.TRANSPARENT);
                                    Log.d("DEBUG", "Date not found in map: " + date);

                                }
                                entries.add(new BarEntry(index, barHeight));

                                index++;
                            } catch (ParseException e) {
                                Log.e("DEBUG", "Date parsing error", e);
                            }

                        }
                        Log.d("DEBUG", "CHART4");

                        String days = String.valueOf(depressedDaysCount);
                        depressedDays.setText(days + " day(s)");

                        BarDataSet dataSet = new BarDataSet(entries, "Depression Status");
                        dataSet.setColors(colors);
                        dataSet.setDrawValues(false);

                        BarData barData = new BarData(dataSet);
                        chart.setData(barData);
                        chart.getDescription().setEnabled(false);

                        XAxis xAxis = chart.getXAxis();
                        xAxis.setGranularity(1f);
                        xAxis.setGranularityEnabled(true);
                        xAxis.setLabelCount(dates.size(), false);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setDrawGridLines(false);
                        xAxis.setLabelRotationAngle(-45);
                        xAxis.setTextSize(10f);
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));

                        YAxis leftAxis = chart.getAxisLeft();
                        leftAxis.setDrawLabels(false);  // Disable left y-axis labels
                        leftAxis.setDrawGridLines(false);

                        YAxis rightAxis = chart.getAxisRight();
                        rightAxis.setDrawLabels(false);  // Disable right y-axis labels
                        rightAxis.setDrawGridLines(false);


                        chart.getLegend().setEnabled(false);
                        chart.animateY(1500);
                        chart.invalidate();
                        Log.d("DEBUG", "CHART5");
                    } else {
                        chart.setVisibility(View.GONE);
                        noDataTextView.setVisibility(View.VISIBLE);
                        noDataTextView.setText("Start typing using MindCheck keyboard to reveal the chart");
                        depressedDays.setText("N/A");
                    }
                } else {
                    Log.e("DEBUG", "No such document");
                }
            } else {
                Log.e("DEBUG", "No such document or error fetching document");
            }
        });
    }

}