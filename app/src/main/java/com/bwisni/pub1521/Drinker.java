package com.bwisni.pub1521;

/**
 * Created by Bryan on 4/14/2016.
 */
public class Drinker {
    private String name;
    private int credits;
    private int totalDrank;

    public Drinker(String name, int credits){
        this.name = name;
        this.credits = credits;
    }

    public int getTotalDrank() {
        return totalDrank;
    }

    public void setTotalDrank(int totalDrank) {
        this.totalDrank = totalDrank;
    }


    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void addCredits(int credits) {
        this.credits += credits;
    }
    public void subtractCredit() {
        this.credits--;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
