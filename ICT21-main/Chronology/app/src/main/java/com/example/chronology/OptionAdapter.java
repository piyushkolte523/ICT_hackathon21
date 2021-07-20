package com.example.chronology;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronology.Question;

import java.util.ArrayList;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.ViewHolder>{

    Question question;
    Context context;
    private ArrayList<String> options = new ArrayList<>() ;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView optionView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            optionView = itemView.findViewById(R.id.quizOption);
        }

        public TextView getTextView() {
            return optionView;
        }
    }

    public OptionAdapter(Context context, Question question) {
        this.context = context;
        this.question = question;
        this.options.add(this.question.option1);
        this.options.add( this.question.option2);
        this.options.add(this.question.option3);
        this.options.add(this.question.option4);
        System.out.println(this.options);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.option_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionAdapter.ViewHolder holder, final int position) {
        holder.getTextView().setText(this.options.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                question.userAnswer = options.get(position);
                notifyDataSetChanged();
            }

        });
        if (question.userAnswer == options.get(position)){
            holder.itemView.setBackgroundResource(R.drawable.option_item_selected_bg);
        }else{
            holder.itemView.setBackgroundResource(R.drawable.option_item_bg);
        }

    }

    @Override
    public int getItemCount() {
        return this.options.size();
    }
}
