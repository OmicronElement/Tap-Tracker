package com.bwisni.pub1521;

/**
 * Created by Bryan on 10/4/2016.
 */
public class DailyStat {
    private int numPours;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumPours() {
        return numPours;
    }

    public void setNumPours(int numPours) {
        this.numPours = numPours;
    }

    public DailyStat(String name, int numPours) {

        this.name = name;
        this.numPours = numPours;
    }
}
