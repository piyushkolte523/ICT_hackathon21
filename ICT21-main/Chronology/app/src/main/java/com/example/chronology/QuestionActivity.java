package com.example.chronology;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestionActivity extends AppCompatActivity {

    Button btnPrevious, btnSubmit, btnNext;
    TextView description;
    RecyclerView optionList;
    FirebaseFirestore firestore;
    String title;

    List<Quiz> quizList;
    Quiz quiz;
    Map<String, Question> questions;
    int index = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnSubmit = findViewById(R.id.btnSubmit);

        description = findViewById(R.id.description);
        optionList = findViewById(R.id.optionList);
        title = getIntent().getStringExtra("title");
        Log.i("QuestionActivity", title);
        setUpFireStore();
        setUpEventListener();
    }

    private void setUpEventListener() {
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index--;
                bindViews();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                bindViews();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createToast("Submitting");
                Log.i("QuestionActivity", questions.toString());
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                String json = new Gson().toJson(quizList.get(0));
                intent.putExtra("QUIZ", json);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setUpFireStore() {
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("flashQuizz").whereEqualTo("title", title)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            quizList = queryDocumentSnapshots.toObjects(Quiz.class);
                            questions = quizList.get(0).questions;
                            bindViews();
                        }else{
                            createToast("No Questions Present");
                            finish();
                        }
                    }
                });
    }

    private void createToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindViews() {
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnPrevious.setVisibility(View.GONE);

        if(index == 1 && index == questions.size()){
            btnSubmit.setVisibility(View.VISIBLE);
        } else if(index == questions.size()){
            btnNext.setVisibility(View.GONE);
            btnPrevious.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            btnNext.setVisibility(View.VISIBLE);
        }
        else {
            btnNext.setVisibility(View.VISIBLE);
            btnPrevious.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
        }

        Question question = questions.get("question"+Integer.toString(index));

//        Question question = new Question("Burj Khalifa","Tallest Building",  "Twin Towers", "Antilla", "Burj Khalifa", "Empire state");
        if (question != null){
            description.setText(question.description);
            OptionAdapter adapter = new OptionAdapter(this, question);
            optionList.setLayoutManager(new LinearLayoutManager(this));
            optionList.setAdapter(adapter);
            optionList.setHasFixedSize(true);
        }
    }
}