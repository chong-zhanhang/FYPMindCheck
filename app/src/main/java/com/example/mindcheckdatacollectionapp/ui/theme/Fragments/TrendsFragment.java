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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class TrendsFragment extends Fragment {
    private LineChart ht, ft, sp, pfr;
    private TextView avgHT, avgFT, avgSP, avgPFR;
    private TextView noDataViewHT, noDataViewFT, noDataViewSP, noDataViewPFR;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> xLabels = generatePast14days();

//    private float calculateMedian(List<Float> yValues) {
//        List<Float> copy = new ArrayList<>();
//        copy.addAll(yValues);
//        Collections.sort(copy);
//        int size = copy.size();
//        if (size % 2 == 0) {
//            int midIndex1 = size / 2 - 1;
//            int midIndex2 = size / 2;
//            return (copy.get(midIndex1) + copy.get(midIndex2)) / 2.0f;
//        } else {
//            int midIndex = size / 2;
//            return copy.get(midIndex);
//        }
//    }

    public TrendsFragment() {
        // Required empty public constructor
    }

    private List<String> generatePast14days() {
        List<String> past14Days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M", Locale.getDefault());
        for (int i = 0; i < 14; i++) {
            past14Days.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        Collections.reverse(past14Days);
        return past14Days;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trends, container, false);
        ht = view.findViewById(R.id.ht_line);
        ft = view.findViewById(R.id.ft_line);
        sp = view.findViewById(R.id.sp_line);
        pfr = view.findViewById(R.id.pfr_line);

        avgHT = view.findViewById(R.id.medianHT);
        avgFT = view.findViewById(R.id.medianFT);
        avgSP = view.findViewById(R.id.medianSP);
        avgPFR = view.findViewById(R.id.medianPFR);

        noDataViewHT = view.findViewById(R.id.noDataTextViewHT); // Make sure you have this TextView in your XML
        noDataViewFT = view.findViewById(R.id.noDataTextViewFT);
        noDataViewSP = view.findViewById(R.id.noDataTextViewSP);
        noDataViewPFR = view.findViewById(R.id.noDataTextViewPFR);

        loadData();
        return view;
    }

    private void loadData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("DEBUG", "User not logged in.");
        } else {
            String userID = currentUser.getUid();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -13);
            Timestamp startTimestamp = new Timestamp(cal.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/M", Locale.getDefault());

            db.collection("typingSession")
                    .whereEqualTo("userId", userID)
                    .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, List<Float>> dailyValuesHT = new HashMap<>();
                            Map<String, List<Float>> dailyValuesFT = new HashMap<>();
                            Map<String, List<Float>> dailyValuesSP = new HashMap<>();
                            Map<String, List<Float>> dailyValuesPFR = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Timestamp ts = document.getTimestamp("timestamp");
                                String date = sdf.format(ts.toDate());
                                dailyValuesHT.computeIfAbsent(date, k -> new ArrayList<>()).add(document.getDouble("medianHT").floatValue());
                                dailyValuesFT.computeIfAbsent(date, k -> new ArrayList<>()).add(document.getDouble("medianFT").floatValue());
                                dailyValuesSP.computeIfAbsent(date, k -> new ArrayList<>()).add(document.getDouble("medianSP").floatValue());
                                dailyValuesPFR.computeIfAbsent(date, k -> new ArrayList<>()).add(document.getDouble("medianPFR").floatValue());
                            }
                            updateCharts(dailyValuesHT, ht, "Median Hold Time", avgHT, noDataViewHT);
                            updateCharts(dailyValuesFT, ft, "Median Flight Time", avgFT, noDataViewFT);
                            updateCharts(dailyValuesSP, sp, "Median Typing Speed", avgSP, noDataViewSP);
                            updateCharts(dailyValuesPFR, pfr, "Median Press-Flight Rate", avgPFR, noDataViewPFR);
                        } else {
                            Log.e("DEBUG", "Failed to fetch data", task.getException());
                        }
                    });
        }
    }

    private void updateCharts(Map<String, List<Float>> dailyValues, LineChart chart, String label, TextView avgTV, TextView noDataTextView) {
        List<Entry> entries = new ArrayList<>();
        float totalAvg = 0;
        int count = 0;
        for (int i = 0; i < xLabels.size(); i++) {
            String date = xLabels.get(i);
            List<Float> values = dailyValues.get(date);
            float avg = 0;
            if (values != null && !values.isEmpty()) {
                for (Float v : values) {
                    avg += v;
                }
                avg /= values.size();
                totalAvg += avg;
                count++;
            }
            entries.add(new Entry(i, avg));
        }

        if (count > 0) {
            totalAvg /= count;
            avgTV.setText(String.format(Locale.getDefault(), "%.3f", totalAvg));

            LineDataSet dataSet = new LineDataSet(entries, label);
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setCircleHoleColor(Color.RED);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.BLUE);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);

            XAxis xAxis = chart.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setLabelCount(xLabels.size(), true);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
            xAxis.setLabelRotationAngle(-45);
            xAxis.setTextSize(7f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setGranularityEnabled(true);
            leftAxis.setDrawGridLines(false);

            chart.getAxisRight().setEnabled(false);
            chart.invalidate();

            chart.setVisibility(View.VISIBLE);
            noDataTextView.setVisibility(View.GONE);
        } else {
            chart.setVisibility(View.GONE);
            noDataTextView.setVisibility(View.VISIBLE);
            noDataTextView.setText("Start typing using MindCheck keyboard to reveal the chart");
            avgTV.setText("N/A");
        }
    }

    private void setData(LineChart lineChart, List<Float> yValues, String label) {
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < yValues.size(); i++) {
            values.add(new Entry(i, yValues.get(i)));
        }

        LineDataSet set1;
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, label);
            set1.setDrawIcons(false);
            set1.setColor(Color.BLUE);
            set1.setCircleColor(Color.BLUE);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFillColor(Color.BLUE);

            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            LineData data = new LineData(dataSets);

            lineChart.setData(data);
        }

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularityEnabled(true);
        leftAxis.setDrawGridLines(false);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.invalidate();

    }
}