package com.example.flashcard.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
public class Question {
    public Question(){}
    public String answer = "";
    public String description = "";
    public String option1 = "";
    public String option2 = "";
    public String option3 = "";
    public String option4 = "";
    public String userAnswer = "";

    public Question(String answer, String description, String option1, String option2, String option3, String option4){
        this.answer = answer;
        this.description = description;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
    }

}

