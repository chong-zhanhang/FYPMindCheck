package com.example.mindcheckdatacollectionapp.ui.theme;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class DepressionDetectionApiService {
    public interface ApiInterface {
        @POST("/predict")
        Call<PredictionResponse> predictDepression(@Body PredictionRequest request);

        @POST("/fine_tune")
        Call<FineTuneResponse> fineTuneModel(@Body FineTuneRequest request);
    }

    public static class FineTuneRequest {
        private String userId;
        private List<typingSession> data;
        private List<Float> labels;
        public FineTuneRequest(String userId, List<typingSession> data, List<Float> labels) {
            this.userId = userId;
            this.data = data;
            this.labels = labels;
        }
    }

    public static class FineTuneResponse {
        private String status;
        private double fine_tuned_accuracy;
        private double fine_tuned_f1;
        private double base_accuracy;
        private double base_f1;

        public String getModelStatus() {
            return status;
        }
        public double getBase_accuracy() {
            return base_accuracy;
        }
        public double getBase_f1() {
            return base_f1;
        }
        public double getFine_tuned_accuracy() {
            return fine_tuned_accuracy;
        }
        public double getFine_tuned_f1() {
            return fine_tuned_f1;
        }
        public void setModelStatus(String status) {
            this.status = status;
        }
        public void setBase_accuracy(double base_accuracy) {
            this.base_accuracy = base_accuracy;
        }
        public void setBase_f1(double base_f1) {
            this.base_f1 = base_f1;
        }
        public void setFine_tuned_accuracy(double fine_tuned_accuracy) {
            this.fine_tuned_accuracy = fine_tuned_accuracy;
        }
        public void setFine_tuned_f1(double fine_tuned_f1) {
            this.fine_tuned_f1 = fine_tuned_f1;
        }
    }

    public static class PredictionRequest {
        private String userId;
        private List<typingSession> data;
        private String bestModel;

        public PredictionRequest(String userId, List<typingSession> data, String bestModel) {
            this.userId = userId;
            this.data = data;
            this.bestModel = bestModel;
        }
    }

    public static class PredictionResponse {
        private Boolean prediction;
        public Boolean getPrediction() {
            return prediction;
        }
        public void setPrediction(Boolean prediction) {
            this.prediction = prediction;
        }
    }

    public static class typingSession {
        private String docId;
        private String userId;
        private int totalCharTyped;
        private Timestamp timestamp;
        private Float kurtosisFT, kurtosisHT, kurtosisPFR, kurtosisSP;
        private Float medianFT, medianHT, medianPFR, medianSP;
        private Float sdFT, sdHT, sdPFR, sdSP;
        private Float skewnessFT, skewnessHT, skewnessPFR, skewnessSP;
        private String trained;

        public typingSession() {
        }
        public String getDocId() {
            return docId;
        }
        public String getUserId() {
            return userId;
        }

        public Float getKurtosisFT() {
            return kurtosisFT;
        }

        public Float getKurtosisHT() {
            return kurtosisHT;
        }

        public Float getKurtosisPFR() {
            return kurtosisPFR;
        }

        public Float getKurtosisSP() {
            return kurtosisSP;
        }

        public int getTotalCharTyped() {
            return totalCharTyped;
        }

        public Float getMedianFT() {
            return medianFT;
        }

        public Float getMedianHT() {
            return medianHT;
        }

        public Float getMedianPFR() {
            return medianPFR;
        }

        public Float getMedianSP() {
            return medianSP;
        }

        public Float getSdFT() {
            return sdFT;
        }

        public Float getSdHT() {
            return sdHT;
        }

        public Float getSdPFR() {
            return sdPFR;
        }

        public Float getSdSP() {
            return sdSP;
        }

        public Float getSkewnessFT() {
            return skewnessFT;
        }

        public Float getSkewnessHT() {
            return skewnessHT;
        }

        public Float getSkewnessPFR() {
            return skewnessPFR;
        }

        public Float getSkewnessSP() {
            return skewnessSP;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public String getTrained() {
            return trained;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }
    }

    private ApiInterface apiInterface;

    public DepressionDetectionApiService() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.207.200.200:5000")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.apiInterface = retrofit.create(ApiInterface.class);
    }

//    public void loadFineTuneTypingSessionData() {
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            String userID = currentUser.getUid();
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//            db.collection("typingSession")
//                    .whereEqualTo("userId", userID)
//                    .whereEqualTo("trained", "false")
//                    .get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            List<typingSession> typingData = new ArrayList<>();
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                try{
//                                    typingSession data = document.toObject(typingSession.class);
//                                    typingData.add(data);
//                                } catch (Exception e) {
//                                    Log.e("DEBUG", "Error converting doc to obj", e);
//                                }
//                            }
//                        }
//                    });
//        } else {
//            Log.e("DEBUG", "User not logged in!");
//        }
//    }

    public void loadPredictionTypingSessionData() {

    }

    public void fineTuneModel(String userID, List<typingSession> typingData, List<Float> labels, Callback<FineTuneResponse> callback) {
        FineTuneRequest request = new FineTuneRequest(userID, typingData, labels);
        Call<FineTuneResponse> call = apiInterface.fineTuneModel(request);
        call.enqueue(callback);
    }

    public void predictDepression(String userID, List<typingSession> typingData, String bestModel, Callback<PredictionResponse> callback) {
        PredictionRequest request = new PredictionRequest(userID, typingData, bestModel);
        Call<PredictionResponse> call = apiInterface.predictDepression(request);
        call.enqueue(callback);
    }
}


