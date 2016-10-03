package com.bwisni.pub1521;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by Bryan on 10/3/2016.
 */
public class KegLevelColor implements ValueDependentColor<DataPoint> {
    @Override
    public int get(DataPoint data) {
        double y = data.getY();
        if(y <= 30)
            return Color.RED;
        else
            return Color.parseColor("#2196f3");
    }
}
