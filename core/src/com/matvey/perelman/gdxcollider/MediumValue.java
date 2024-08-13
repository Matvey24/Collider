package com.matvey.perelman.gdxcollider;


import java.util.ArrayList;

public class MediumValue {
    public final ArrayList<Long> list;
    public int capacity = 60;
    public int index;

    public long avg;
    public long sum;
    public MediumValue() {
        this.list = new ArrayList<>();

    }

    public void add(long value) {
        if (list.size() != capacity) {
            list.add(value);
            sum += value;
        } else {
            sum -= list.get(index);
            list.set(index, value);
            sum += value;
            index++;
            if(index == capacity)
                index = 0;
        }
        avg = sum / list.size();
    }
}

