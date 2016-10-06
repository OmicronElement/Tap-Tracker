package com.bwisni.pub1521;


import com.orm.SugarRecord;


/**
 * Created by Bryan on 9/28/2016.
 */
public class DatePoint extends SugarRecord{
    long date;
    int pours;

    public DatePoint(){

    }

    public DatePoint(long x, int y) {
        date = x;
        pours = y;
    }

    public void addDrink() {
        pours++;
    }
}
