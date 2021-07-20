package com.example.sleepmonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;

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
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    JSONObject post_dict;
    LineChart lineChart;
    ProgressDialog pd;
    boolean drawGraph=true;
    TextView textView;
    String avgSleepText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        assert user != null;
//        String uid = user.getUid();
        String uid = "ghD8OKCTfNRCsxmaOHdVq1625vV2";

        post_dict = new JSONObject();
        try {
            post_dict.put("userId", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        lineChart = findViewById(R.id.sleepLineChart);
        textView = findViewById(R.id.textView);
        if (post_dict.length() > 0) {
            Log.i("JSON", post_dict.toString());
            new SleepJSonTask().execute(String.valueOf(post_dict));
        }
    }

    private class SleepJSonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String JsonData = params[0];
            float avgSleep = 0;

            try {
                URL url = new URL("https://sleep-detection-api.herokuapp.com/sleep_durations");
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
                Log.i("DATA", jsonObject.getJSONObject("Hours").toString());

                JSONObject date = jsonObject.getJSONObject("Date");
                JSONObject time = jsonObject.getJSONObject("Hours");

                if (date.length() != 0 || time.length() != 0) {
                    drawGraph = true;

                    ArrayList<Entry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();

                    if (date.length() == time.length()) {
                        for (int i = 0; i < date.length(); i++) {
                            Log.i("dateData", date.getString(String.valueOf(i)));
                            labels.add(date.getString(String.valueOf(i)));

                            Log.i("timeData", time.getString(String.valueOf(i)));
                            entries.add(new Entry(Float.parseFloat(time.getString(String.valueOf(i))), i));

                            avgSleep += Float.parseFloat(time.getString(String.valueOf(i)));
                        }
                        avgSleep = avgSleep / time.length();
                        avgSleepText = "Average Sleep Duration in Hours: " + String.valueOf(avgSleep) + "Hours";

                        Log.i("XAxis", labels.toString());
                        Log.i("YAxis", entries.toString());

                        LineDataSet lineDataSet = new LineDataSet(entries, "Sleep Duration Everyday in Hours");
                        lineDataSet.setDrawFilled(true);
                        lineDataSet.setLineWidth(4f);
                        lineDataSet.setCircleRadius(6f);
                        lineDataSet.setDrawCircleHole(false);
                        lineDataSet.setValueTextSize(11f);
                        lineDataSet.setDrawCubic(true);
                        lineDataSet.setCircleColor(Color.rgb(29,78,137));
                        lineDataSet.setColor(Color.rgb(29,78,137));

                        if (Utils.getSDKInt() >= 18) {
                            Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.fade_blue);
                            lineDataSet.setFillDrawable(drawable);
                        } else {
                            lineDataSet.setFillColor(Color.DKGRAY);
                        }
                        LineData lineData = new LineData(labels, lineDataSet);
                        lineChart.setData(lineData);
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
            if (drawGraph) {

                lineChart.setVisibility(View.VISIBLE);
                lineChart.getAxisLeft().setDrawLabels(false);

                lineChart.getXAxis().setDrawGridLines(false);
                lineChart.getAxisLeft().setDrawGridLines(false);
                lineChart.getAxisRight().setDrawGridLines(false);

                lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                lineChart.getAxisRight().setEnabled(false);

                lineChart.setDescription("Date");
                lineChart.setDescriptionTextSize(20f);

                lineChart.animateY(2000);
                lineChart.invalidate();

                textView.setText(avgSleepText);
                Log.i("FINISH", "FINISH");
            } else {
                Log.i("ERROR", "BLANK ENTRIES");
            }
        }
    }
}