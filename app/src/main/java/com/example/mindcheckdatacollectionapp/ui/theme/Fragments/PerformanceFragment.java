package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class PerformanceFragment extends Fragment {
    public interface DataFetchCallback {
        void onDataFetched(float data);
    }
    private PieChart chart1, chart2;
    private ImageButton infoBase, infoFT;
    private TextView notEnoughView, fineTunedView;

    public PerformanceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_performance, container, false);

        notEnoughView = view.findViewById(R.id.notEnoughDataView);
        fineTunedView = view.findViewById(R.id.fine_tuned_view);
        chart1 = view.findViewById(R.id.modelF1);
        chart2 = view.findViewById(R.id.fineTunedF1);
        infoBase = view.findViewById(R.id.pretrained_info);
        infoFT = view.findViewById(R.id.finetuned_info);

        infoBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBaseInfoDialog();
            }
        });

        infoFT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFTInfoDialog();
            }
        });

        setupChart(chart1, 0.90f);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            Log.d("DEBUG", "HELLO-1");
            fetchFineTunedAccuracy(userID, new DataFetchCallback() {
                @Override
                public void onDataFetched(float data) {
                    Log.d("DEBUG", "HELLO0");
                    if (data == -1.0f) {
                        chart2.setVisibility(View.GONE);
                        fineTunedView.setVisibility(View.GONE);
                        notEnoughView.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("DEBUG", "HELLO");
                        setupChart(chart2, data);
                        Log.d("DEBUG", "HELLO1");
                        loadChartData(data, chart2, "Fine-tuned Model Accuracy");
                        Log.d("DEBUG", "HELLO2");
                    }
                }
            });

        }else {
            Log.e("DEBUG", "User not logged in");
        }
        loadChartData(0.90f, chart1, "Pre-trained Model Accuracy");
        Log.d("DEBUG", "HELLO3");

        return view;
    }

    private void showFTInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Fine-Tuned Model");
        builder.setMessage("This model is trained on your typing data. After enough typing data is collected from you, only then the model is created and the chart will be visible. The number on the chart indicates the fraction of times the model correctly predicts depression occurrence. The system will choose the better-performing model for depression prediction.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showBaseInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pre-Trained Model");
        builder.setMessage("This model is trained on data from the general population. The number indicates the fraction of times the model correctly predicts depression occurrence. The system will choose the better-performing model for depression prediction.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void fetchFineTunedAccuracy(String userID, DataFetchCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("mobileUser").whereEqualTo("userID", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Log.d("DEBUG", "HELLOA");
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        try {
                            if (document.contains("fine_tuned_accuracy")){
                                Log.d("DEBUG", "HELLOAA");
                                Double fine_tuned_acc = document.getDouble("fine_tuned_accuracy");
                                Log.d("DEBUG", "HELLOAAA");
                                if (fine_tuned_acc != null) {
                                    Log.d("DEBUG", "HELLOB");
                                    callback.onDataFetched(fine_tuned_acc.floatValue());
                                } else {
                                    Log.d("DEBUG", "HELLONO");
                                    callback.onDataFetched(-1.0f);
                                    Log.e("DEBUG", "No fine-tuned accuracy data available.");
                                }
                            } else {
                                callback.onDataFetched(-1.0f);
                                Log.e("DEBUG", "No fine-tuned accuracy data available2.");
                            }
                        } catch (Exception e) {
                            Log.e("DEBUG", "Exception in fetching or parsing fine_tuned_accuracy: " + e.getMessage());
                        }
                    } else {
                        Log.e("DEBUG", "Failed to fetch data.");
                    }
                });
        Log.d("DEBUG", "HELLOC");
    }

    private void setupChart(PieChart chart, float score) {
        chart.setMinimumWidth(600);
        chart.setMinimumHeight(600);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(255);
        chart.setHoleRadius(40f);
        chart.setRotationAngle(270);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setCenterText(String.valueOf(score));
        chart.setCenterTextSize(25f);
        chart.setCenterTextColor(Color.BLACK);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setTextSize(25f);
        l.setTextColor(Color.BLUE);
        l.setDrawInside(false);
        l.setEnabled(false);
    }

    private void loadChartData(float score, PieChart chart, String label) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(score, ""));
        entries.add(new PieEntry(1 - score, ""));

        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextColor(Color.BLUE);
        dataSet.setValueTextSize(60f);
        dataSet.setColors(new int[]{Color.GREEN, Color.LTGRAY});
        dataSet.setDrawValues(false);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }
}