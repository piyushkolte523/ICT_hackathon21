package com.example.chronology;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class QuizFragment extends Fragment {
    private static final String TAG = "MainActivity";
    QuizAdapter adapter;
    ArrayList<Quiz> quizList = new ArrayList<>();
    RecyclerView quizRecyclerView;
    FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quiz_fragment, container, false);
        quizRecyclerView = view.findViewById(R.id.flashRecyclerView);
        setUpFireStore();
        setUpViews();

        return view;
    }

    private void setUpViews() {

        adapter = new QuizAdapter(getContext(), quizList);
        quizRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
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
                    Toast.makeText(getContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
                } else {

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
    }
}
