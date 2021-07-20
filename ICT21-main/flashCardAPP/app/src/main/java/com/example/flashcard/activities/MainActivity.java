package com.example.flashcard.activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.adapters.QuizAdapter;
import com.example.flashcard.models.Quiz;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    QuizAdapter adapter;
    ArrayList<Quiz> quizList = new ArrayList<>();
    RecyclerView quizRecyclerView;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpFireStore();
        setUpViews();

    }

    private void setUpViews() {

        adapter = new QuizAdapter(this, quizList);
//        quizList.add(new Quiz("1", "General Knowledge"));
        quizRecyclerView = findViewById(R.id.flashRecyclerView);
        quizRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        quizRecyclerView.setAdapter(adapter);

    }

    private void setUpFireStore() {
        firestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestore.collection("flashQuizz");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value == null || error != null) {
                    Toast.makeText(getApplicationContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
                } else {

//                    Log.d("Data", value.toObjects(Quiz.class).toString());
//                    quizList.clear();
//                    quizList.addAll(value.toObjects(Quiz.class));
//                    adapter.notifyDataSetChanged();
                    quizList.clear();
                    System.out.println(value.getDocuments().toString());
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            Quiz quizVal = doc.getDocument().toObject(Quiz.class);
                            quizList.add(quizVal);
                            adapter.notifyDataSetChanged();
                        }
                    }

                }
            }
        });

//        collectionReference.get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot != null) {
//                            Quiz quiz = documentSnapshot.toObject(Quiz.class);
//
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
//
//                        }
//                    }
//                })


//        collectionReference.addSnapshotListener((value, error) ->{
//            if(value == null || error != null){
//                Toast.makeText(getApplicationContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
//            } else {
//                Log.d("Data", value.toObjects(Quiz.class).toString());
////
////                quizList.clear();
////                    quizList.addAll(value.toObjects(Quiz.class));
////                    adapter.notifyDataSetChanged();
//            }
//        });

    }
}