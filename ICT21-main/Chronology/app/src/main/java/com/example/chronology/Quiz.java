package com.example.chronology;

import java.util.HashMap;
import java.util.Map;


public class Quiz {
    public Quiz(){}
    public String id = "";
    public String title ;
    public Map<String, Question> questions = new HashMap<String, Question>();

    public Quiz(String id, String title ){
        this.id = id;
        this.title = title;
    }

}
