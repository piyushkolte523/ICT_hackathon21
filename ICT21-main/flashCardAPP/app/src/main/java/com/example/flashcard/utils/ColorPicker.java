package com.example.flashcard.utils;

import java.util.ArrayList;

public class ColorPicker {
    private static int currentColorIndex = 0;
    private static int currentColorTextIndex = 4;
    private static String[] colors = {"#3EB9DF","#3685BC", "#E44F55", "#D36280", "#FA8056", "#818BCA", "#7D659F", "#51BAB3", "#4FB66C"};

    public static String getColor(){
        currentColorIndex = (currentColorIndex+1) % colors.length;
        return colors[currentColorIndex];
    }

    public static String getTextColor(){
        currentColorTextIndex = (currentColorTextIndex+1) % colors.length;
        return colors[currentColorTextIndex];
    }

}
