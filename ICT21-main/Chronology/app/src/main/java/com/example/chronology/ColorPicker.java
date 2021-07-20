package com.example.chronology;

import java.util.ArrayList;

public class ColorPicker {
    private static int currentColorIndex = 0;
    private static int currentColorTextIndex = 4;
//    private static String[] colors = {"#0818A8","#5D3FD3", "#0437F2", "#4682B4", "#87CEEB", "#0F52BA", "#4169E1", "#000080", "#3F00FF"};
    private static String[] colors = {"#ffffff", "#ffffff", "#ffffff" , "#ffffff", "#ffffff", "#ffffff", "#ffffff", "#ffffff"};

    public static String getColor(){
        currentColorIndex = (currentColorIndex+1) % colors.length;
        return colors[currentColorIndex];
    }

    public static String getTextColor(){
        currentColorTextIndex = (currentColorTextIndex+1) % colors.length;
        return colors[currentColorTextIndex];
    }

}
