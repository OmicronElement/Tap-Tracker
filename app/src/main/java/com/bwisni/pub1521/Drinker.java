package com.bwisni.pub1521;

import com.orm.SugarRecord;

/**
 * Created by Bryan on 4/14/2016.
 */
public class Drinker extends SugarRecord {
    String name = "Default";
    int credits = 0;
    int totalDrank = 0;

    public Drinker() {
    }

    public Drinker(String name, int credits) {
        this.name = name;
        this.credits = credits;
    }

    public String toString(){
        return (this.name + " : " + this.credits);
    }
}
