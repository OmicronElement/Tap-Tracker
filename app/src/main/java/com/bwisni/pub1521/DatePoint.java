package com.bwisni.pub1521;


import com.orm.SugarRecord;


/**
 * Created by Bryan on 9/28/2016.
 */
public class DatePoint extends SugarRecord{
    long date;
    int drinks;

    public DatePoint(){

    }

    public DatePoint(long x, int y) {
        date = x;
        drinks = y;
    }

    public void addDrink() {
        drinks++;
    }
}
