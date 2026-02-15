package com.matvey.perelman.gdxcollider.scheduler.collections;

import java.util.ArrayList;

public class IndexedHeap<I extends Comparable<I> & IndexedHeapElement>{
    private static final int DEFAULT_CAPACITY = 2048;
    private final ArrayList<I> items;

    public IndexedHeap(){
        this(DEFAULT_CAPACITY);
    }
    public IndexedHeap(int capacity){
        items = new ArrayList<>(capacity);
    }

    public void add(I item){
        int pos = items.size();
        items.add(item);
        item.setHeapIndex(pos);
        shiftUp(pos);
    }
    public I first(){
        if (items.isEmpty()) return null;
        return items.get(0);
    }
    public boolean isEmpty(){
        return items.isEmpty();
    }
    public I removeFirst() {
        if (items.isEmpty()) return null;
        I minItem = items.get(0);
        minItem.setHeapIndex(-1);
        I newItem = items.remove(items.size() - 1);
        items.set(0, newItem);
        newItem.setHeapIndex(0);
        shiftDown(0);
        return minItem;
    }
    public void remove(I item){
        int idx = item.getHeapIndex();
        if(idx == -1)
            return;

        if(idx == items.size() - 1){
            items.remove(items.size() - 1);
            return;
        }
        items.set(idx, items.remove(items.size() - 1));
        items.get(idx).setHeapIndex(idx);

        if(idx > 0 && items.get(idx).compareTo(items.get(parent(idx))) < 0){
            shiftUp(idx);
        }else{
            shiftDown(idx);
        }
    }

    private void shiftUp(int i){
        while (i > 0) {
            int p = parent(i);
            if (items.get(i).compareTo(items.get(p)) >= 0) break;
            swap(i, p);
            i = p;
        }
    }
    private void shiftDown(int i) {
        while (leftChild(i) < items.size()) {
            int left = leftChild(i);
            int right = left + 1;
            int smallest = (right < items.size()
                    && items.get(right).compareTo(items.get(left)) < 0)
                    ? right : left;
            if (items.get(i).compareTo(items.get(smallest)) <= 0) break;
            swap(i, smallest);
            i = smallest;
        }
    }
    private void swap(int i, int j) {
        I tmp = items.get(i);
        items.set(i, items.get(j));
        items.set(j, tmp);

        items.get(i).setHeapIndex(i);
        items.get(j).setHeapIndex(j);
    }

    private int parent(int i) { return (i - 1) >>> 1; }
    private int leftChild(int i) { return (i << 1) + 1; }
}
