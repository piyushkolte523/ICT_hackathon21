package com.example.flashcard.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.flashcard.R;
import com.example.flashcard.adapters.QuizAdapter;
import com.example.flashcard.models.Quiz;

import java.util.List;

public class QuizWelcome extends AppCompatActivity {

    Button getStarted, checkAlzheimers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_welcome);


        getStarted = findViewById(R.id.getStartedButton);
        checkAlzheimers = findViewById(R.id.checkAlzheimers);

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent quizApp = new Intent(getBaseContext(), MainActivity.class);
                startActivity(quizApp);
            }
        });

        checkAlzheimers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent checkAlzheimersIntent = new Intent(getBaseContext(), AlzheimerDetection.class);
                startActivity(checkAlzheimersIntent);
            }
        });
    }
}