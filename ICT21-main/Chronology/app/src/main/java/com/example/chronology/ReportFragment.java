package com.example.chronology;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.gkemon.XMLtoPDF.PdfGenerator;
import com.gkemon.XMLtoPDF.PdfGeneratorListener;
import com.gkemon.XMLtoPDF.model.FailureResponse;
import com.gkemon.XMLtoPDF.model.SuccessResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ReportFragment extends Fragment {
    DatabaseReference databaseReference;
    TextView predictionTextView, quizTextView;
    BarChart barChart;
    ProgressDialog pd;
    boolean drawGraph = true;
    String colourTheme = "#01378c";
    JSONObject post_dict;
    String uid, quizScore = "\t\t\t\t\t\t\tDate: \t\t\t\t\t\t\t\t\t\t\t\t\t\t Score:\n";
    Button downloadButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_fragment, container, false);
        predictionTextView = view.findViewById(R.id.predictionTextView);
        quizTextView = view.findViewById(R.id.quizTextView);
        barChart = view.findViewById(R.id.barChart);
        downloadButton = view.findViewById(R.id.downloadButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        uid = user.getUid();
        databaseReference= FirebaseDatabase.getInstance("https://chronology-88080-default-rtdb.firebaseio.com/").getReference().child(uid);

        post_dict = new JSONObject();
        try {
            post_dict.put("userId", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new QuizJSonTask().execute(String.valueOf(post_dict));

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadButton.setVisibility(View.INVISIBLE);
                PdfGenerator.getBuilder()
                        .setContext(getActivity())
                        .fromViewSource()
                        .fromView(view)
                        .setPageSize(PdfGenerator.PageSize.A4)
                        .setFileName("Report")
                        .setFolderName("Chronology/Report")
                        .openPDFafterGeneration(true)
                        .build(new PdfGeneratorListener() {
                            @Override
                            public void onFailure(FailureResponse failureResponse) {
                                super.onFailure(failureResponse);
                            }

                            @Override
                            public void showLog(String log) {
                                super.showLog(log);
                            }

                            @Override
                            public void onStartPDFGeneration() {
                                /*When PDF generation begins to start*/
                            }

                            @Override
                            public void onFinishPDFGeneration() {
                                /*When PDF generation is finished*/
                            }

                            @Override
                            public void onSuccess(SuccessResponse response) {
                                super.onSuccess(response);
                                downloadButton.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });

        return view;
    }

    public void makeUIChanges() {
        databaseReference.child("Prediction").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("ERROR", "Error Getting Data", task.getException());
                } else {
                    predictionTextView.setText((String) task.getResult().getValue());
                }
            }
        });
    }

    private class QuizJSonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(getContext());
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String JsonData = params[0];

            try {
                URL url = new URL("https://sleep-detection-api.herokuapp.com/quiz_score");
                connection = (HttpURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(JsonData);
                writer.close();

                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //json response in buffer

                }

                String data = buffer.toString();

                JSONObject jsonObject = new JSONObject(data);
                Log.i("DATA", jsonObject.getJSONObject("Date").toString());
                Log.i("DATA", jsonObject.getJSONObject("Score").toString());

                JSONObject date = jsonObject.getJSONObject("Date");
                JSONObject time = jsonObject.getJSONObject("Score");

                if (date.length() != 0 || time.length() != 0) {
                    drawGraph = true;

                    ArrayList<BarEntry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();

                    if (date.length() == time.length()) {
                        for (int i = 0; i < date.length(); i++) {
                            Log.i("dateData", date.getString(String.valueOf(i)));
                            labels.add(date.getString(String.valueOf(i)));
                            Log.i("timeData", time.getString(String.valueOf(i)));
                            entries.add(new BarEntry(Float.parseFloat(time.getString(String.valueOf(i))), i));
                            quizScore += "\t\t\t\t\t"+date.getString(String.valueOf(i)) + ": \t\t\t\t\t\t\t\t\t\t\t\t"+ time.getString(String.valueOf(i)) + "\n";
                        }

                        if (!entries.isEmpty()) {
                            drawGraph = true;

                            Log.i("XAxis", labels.toString());
                            Log.i("YAxis", entries.toString());

                            BarDataSet barDataSet = new BarDataSet(entries, "Score");
                            barDataSet.setColors(new int[]{Color.parseColor(colourTheme)});
                            BarData barData = new BarData(labels, barDataSet);

                            barChart.setData(barData);
                        } else {
                            drawGraph = false;
                        }
                    }
                } else {
                    drawGraph = false;
                }

                return data;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            makeUIChanges();
            if (drawGraph) {

                barChart.setVisibility(View.VISIBLE);
                barChart.getXAxis().setDrawGridLines(false);
                barChart.getAxisLeft().setDrawGridLines(false);
                barChart.getAxisRight().setDrawGridLines(false);

                barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChart.getAxisRight().setEnabled(false);

                barChart.animateXY(2000, 2000);
                barChart.invalidate();

                makeUIChanges();

                quizTextView.setText(quizScore);

                Log.i("FINISH", "FINISH");
            } else {
                Log.i("ERROR", "BLANK ENTRIES");
            }
        }
    }
}
