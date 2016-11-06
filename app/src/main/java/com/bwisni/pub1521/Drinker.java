package com.bwisni.pub1521;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by Bryan on 4/14/2016.
 * Represents a user of the app. Uniquely identified by nfcId, which is stored on the NFC card.
 */
public class Drinker extends SugarRecord implements Serializable {
    private String name = "Default";
    private String nfcId = "";
    private int credits = 0;
    private int totalDrank = 0;
    private int color;

    // Default constructor for SugarRecord
    @SuppressWarnings("unused")
    public Drinker() {
    }

     Drinker(String name, int credits, String NfcId) {
         this.name = name;
         this.credits = credits;
         this.nfcId = NfcId;
         ColorGenerator generator = ColorGenerator.MATERIAL;
         this.color = generator.getColor(nfcId);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


    void subtractCredit(){
        if(credits > 0) {
            credits--;
            totalDrank++;
        }
    }

    public String toString(){
        return (this.name + " : " + this.credits+" credits");
    }


    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNfcId() {
        return nfcId;
    }

    public void setNfcId(String nfcId) {
        this.nfcId = nfcId;
    }

    public int getTotalDrank() {
        return totalDrank;
    }

    public void setTotalDrank(int totalDrank) {
        this.totalDrank = totalDrank;
    }

    public String getShortName() {
        return Character.toString(name.charAt(0));
    }
}
