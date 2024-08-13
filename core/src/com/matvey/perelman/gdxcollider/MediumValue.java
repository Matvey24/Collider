package com.matvey.perelman.gdxcollider;

import com.badlogic.gdx.utils.SortedIntList;

import java.util.ArrayList;

public class MediumValue {
    public final ArrayList<Integer> list;
    public int capacity = 60;
    public int index;

    public int avg;
    public int sum;
    public int full_sum;
    public MediumValue() {
        this.list = new ArrayList<>();

    }

    public void add(int value) {
        if (list.size() != capacity) {
            list.add(value);
            sum += value;
            full_sum += value;
        } else {
            sum -= list.get(index);
            list.set(index, value);
            sum += value;
            full_sum += value;
            index++;
            if(index == capacity)
                index = 0;
        }
        avg = sum / list.size();
    }
}

