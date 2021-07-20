package com.example.chronology;

import android.content.Context;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder>{

    private ArrayList<Quiz> quizess = new ArrayList<>();
    private Context context;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private CardView cardContainer;
        private ImageView iconView;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textViewTitle = view.findViewById(R.id.quizTitle);
            iconView = view.findViewById(R.id.quizIcon);
            cardContainer = view.findViewById(R.id.cardContainer);
        }

        public TextView getTextView() {
            return textViewTitle;
        }
    }


    public QuizAdapter(Context context, ArrayList<Quiz> quizess) {
        this.quizess = quizess;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.flash_quiz_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizAdapter.ViewHolder holder, final int position) {
        holder.getTextView().setText(this.quizess.get(position).title);
//        holder.getTextView().setTextColor((Color.parseColor(ColorPicker.getColor())));
        holder.cardContainer.setCardBackgroundColor((Color.parseColor(ColorPicker.getColor())));
        holder.itemView.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QuestionActivity.class);
                intent.putExtra("title", quizess.get(position).title);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        try {
            System.out.println("In Get Itemcount");
            return this.quizess.size();
        } catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}
