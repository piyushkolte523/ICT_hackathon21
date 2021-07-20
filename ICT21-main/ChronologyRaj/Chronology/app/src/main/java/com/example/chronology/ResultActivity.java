package com.example.chronology;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    Quiz quiz;
    private int score = 0;
    TextView txtScore, txtAnswer;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        reference= FirebaseDatabase.getInstance("https://chronology-88080-default-rtdb.firebaseio.com/").getReference().child(mUser.getUid()).child("QuizScore");

        txtScore = findViewById(R.id.txtScore);
        txtAnswer = findViewById(R.id.txtAnswer);
        setUpViews();
    }

    private void setUpViews() {
        String quizdata = getIntent().getStringExtra("QUIZ");
        quiz = new Gson().fromJson(quizdata, Quiz.class);
        calculateScore();
        setAnswerView();
    }

    private void calculateScore() {

        for (Map.Entry<String, Question> sc : quiz.questions.entrySet()){
            Question question = sc.getValue();

            System.out.println(question.answer+" "+question.userAnswer);
            if (question.answer.equals(question.userAnswer)){
                score+=10;
            }
        }
        txtScore.setText("Your Score : "+score);
        Calendar calendar = Calendar.getInstance();
        long wakeUpTimeMillis = calendar.getTimeInMillis();
        String Date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        reference.child(Date).setValue(String.valueOf(score)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i("INFO", "Success");
                } else {
                    String error = task.getException().toString();
                    Toast.makeText(ResultActivity.this, "Failed: "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setAnswerView() {
        StringBuilder builder = new StringBuilder("");
        for (Map.Entry<String, Question> sc : quiz.questions.entrySet()){
            Question question = sc.getValue();
            builder.append("<font color='#18206F'><b>Question: "+question.description+"</b></font><br/><br/>");
            builder.append("<font color='#00FFFF'>Answer: "+question.answer+"</font><br/><br/>");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtAnswer.setText(Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            txtAnswer.setText(Html.fromHtml(builder.toString()));
        }
    }
}