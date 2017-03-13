package com.tashariko.rankedcontactlist;

import android.support.annotation.NonNull;
import android.telecom.Call;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Puru Chauhan on 29/11/16.
 */

public class CallModel extends RealmObject {

    public String number;
    public Integer rank=0;
    public String name;

    public Integer getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
