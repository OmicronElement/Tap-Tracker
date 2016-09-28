package com.bwisni.pub1521;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by Bryan on 4/14/2016.
 */
public class Drinker extends SugarRecord implements Serializable {
    String name = "Default";
    String nfcId = "";
    int credits = 0;
    int totalDrank = 0;

    public Drinker() {
    }

    public Drinker(String name, int credits, String NfcId) {
        this.name = name;
        this.credits = credits;
        this.nfcId = NfcId;
    }

    public void subtractCredit(){
        if(credits > 0) {
            credits--;
            totalDrank++;
        }
    }

    public String toString(){
        return (this.name + " : " + this.credits+" credits");
    }
}
