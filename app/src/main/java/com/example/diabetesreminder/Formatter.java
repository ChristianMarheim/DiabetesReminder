package com.example.diabetesreminder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatter {


    public static String format(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy MMMM EEE d HH:mm:ss");
        if(date.getTime() == Long.MAX_VALUE){
            return "Long.MAX_VALUE";
        }
        return format.format(date);
    }
}
