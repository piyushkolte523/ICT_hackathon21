package com.example.chronology;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    Boolean is_sleep = false;
    long sleepDuration;
    String sleepDate, wakeUpDate;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Spinner learn_time, learn_category;
    Button startLearning;
    String[] learn= new String[]{"Yoga","Cooking","Arts and Craft","Musical Intruments"};
    String[] timeduration=new String[]{"5 minutes","10 minutes","15 minutes","30 minutes"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance("https://chronology-88080-default-rtdb.firebaseio.com/").getReference().child(mUser.getUid()).child("SleepDuration");

        final SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.example.sleeptracker", MODE_PRIVATE);

        final Button recordSleepButton = view.findViewById(R.id.recordSleepButton);
        learn_time = view.findViewById(R.id.learn_time);
        learn_category = view.findViewById(R.id.learn_category);
        startLearning = view.findViewById(R.id.startLearning);
        final TextView trackFlagTextView = view.findViewById(R.id.trackFlagTextView);


        ArrayAdapter<String> learn_category_adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_dropdown_item,learn);
        ArrayAdapter<String> learn_time_adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_dropdown_item,timeduration);
        learn_time.setAdapter(learn_time_adapter);
        learn_category.setAdapter(learn_category_adapter);


        startLearning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String time_selected = learn_time.getSelectedItem().toString();

                final String category_selected = learn_category.getSelectedItem().toString();

                DatabaseReference reference= FirebaseDatabase.getInstance("https://chronology-88080-default-rtdb.firebaseio.com/").getReference().child("LearnCenter");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(category_selected).child(time_selected).exists()) {
                            String VideoId = dataSnapshot.child(category_selected).child(time_selected).getValue().toString();
                            startYoutube(VideoId);
                        }
                        else
                        {
                            Log.d("Video ID", "Not existing");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });




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

                    Toast.makeText(getContext(), hour+":"+minute+":"+second, Toast.LENGTH_SHORT).show();
                    databaseReference.child(sleepDate).child("duration").setValue(hour+":"+minute+":"+second).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i("INFO", "Success");
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(getContext(), "Failed: "+error, Toast.LENGTH_SHORT).show();
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

        return view;
    }

    private void startYoutube(String videoId) {
            Intent i=new Intent(getActivity(), BasicPlayerActivity.class);
            i.putExtra("Retrieved",videoId);
            startActivity(i);
            //startActivity(new Intent(getActivity(), BasicPlayerActivity.class));

            //pass VideoId to BasicPlayerAcitivity and then use it.
        }
    }
