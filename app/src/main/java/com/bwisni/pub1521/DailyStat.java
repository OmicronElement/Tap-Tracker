package com.bwisni.pub1521;

import com.orm.SugarRecord;

/**
 * Created by Bryan on 10/4/2016.
 */
public class DailyStat extends SugarRecord{
    private int numPours;
    private String name;
    private String nfcId;
    private long date;

    public DailyStat(){}

    public DailyStat(long date, String name, String nfcId, int numPours) {
        this.date = date;
        this.name = name;
        this.nfcId = nfcId;
        this.numPours = numPours;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getNfcId() {
        return nfcId;
    }

    public void setNfcId(String nfcId) {
        this.nfcId = nfcId;
    }

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

    public void addDrink() {
        numPours++;
    }
}
