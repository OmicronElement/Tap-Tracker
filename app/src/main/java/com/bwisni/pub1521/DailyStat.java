package com.bwisni.pub1521;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.orm.SugarRecord;

/**
 * Created by Bryan on 10/4/2016.
 * Stores the number of pours by a particular drinker on a given day
 */
public class DailyStat extends SugarRecord {
    private int numPours;
    private String name;
    private String nfcId;
    private String date;

    // Default constructor for SugarRecord
    @SuppressWarnings("unused")
    public DailyStat() {
    }

    public DailyStat(String date, Drinker drinker, int numPours) {
        this.date = date;
        this.name = drinker.getName();
        this.nfcId = drinker.getNfcId();
        this.numPours = numPours;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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

    public void addPour() {
        numPours++;
    }
}
