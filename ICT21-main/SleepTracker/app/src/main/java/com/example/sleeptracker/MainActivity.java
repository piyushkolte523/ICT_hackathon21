package com.example.sleeptracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Boolean is_sleep = false;
    long sleepDuration;
    String sleepDate, wakeUpDate;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance("https://chronology-88080-default-rtdb.firebaseio.com/").getReference().child("SleepDuration").child("ghD8OKCTfNRCsxmaOHdVq1625vV2");

        final SharedPreferences sharedPreferences = getSharedPreferences("com.example.sleeptracker", MODE_PRIVATE);

        final Button recordSleepButton = findViewById(R.id.recordSleepButton);
        final TextView trackFlagTextView = findViewById(R.id.trackFlagTextView);

        recordSleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long sleepTime = 0;
                if (is_sleep) {
                    Calendar calendar = Calendar.getInstance();
                    long wakeUpTimeMillis = calendar.getTimeInMillis();
                    wakeUpDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    Log.i("SLEEP END", String.valueOf(wakeUpTimeMillis));

                    if (sharedPreferences.getLong("SleepTime", 0) != 0) {
                        sleepTime = sharedPreferences.getLong("SleepTime", 0);
                        sharedPreferences.edit().clear().apply();
                    }

                    if (sleepDate.trim().equals(wakeUpDate.trim())) {
                        Log.i("PREV DURATION", String.valueOf(sleepDuration));
                        sleepDuration += wakeUpTimeMillis - sleepTime;
                    } else {
                        sleepDuration = wakeUpTimeMillis - sleepTime;
                    }
                    Log.i("DURATION", String.valueOf(sleepDuration));

                    final long second = (sleepDuration / 1000) % 60;
                    final long minute = (sleepDuration / (1000 * 60)) % 60;
                    final long hour = (sleepDuration / (1000 * 60 * 60)) % 60;

                    Log.i("HOUR", String.valueOf(hour));
                    Log.i("MIN", String.valueOf(minute));
                    Log.i("SEC", String.valueOf(second));

                    Log.i("TOTAL SLEEP", String.valueOf(sleepDuration));

                    Toast.makeText(MainActivity.this, hour+":"+minute+":"+second, Toast.LENGTH_SHORT).show();
                    databaseReference.child(sleepDate).child("duration").setValue(hour+":"+minute+":"+second).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i("INFO", "Success");
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(MainActivity.this, "Failed: "+error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    is_sleep = false;
                    trackFlagTextView.setVisibility(View.INVISIBLE);
                    recordSleepButton.setText("Start Recording");

                } else {
                    Calendar calendar = Calendar.getInstance();
                    long sleepTimeMillis = calendar.getTimeInMillis();
                    sleepDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    sharedPreferences.edit().putLong("SleepTime", sleepTimeMillis).apply();
                    Log.i("SLEEP ON", String.valueOf(sleepTimeMillis));
                    is_sleep = true;
                    trackFlagTextView.setVisibility(View.VISIBLE);
                    recordSleepButton.setText("Stop Recording");
                }
            }
        });
    }
}