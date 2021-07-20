package com.example.flashcard.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.example.flashcard.R;
import com.example.flashcard.models.Question;
import com.example.flashcard.models.Quiz;
import com.google.gson.Gson;

import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    Quiz quiz;
    private int score = 0;
    TextView txtScore, txtAnswer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
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