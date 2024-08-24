package com.matvey.perelman.gdxcollider;


import com.badlogic.gdx.utils.Array;

public abstract class AverageValue<T> {
    public final Array<T> list;
    public int capacity = 60;
    public int index;

    public T avg;
    public T sum;

    public AverageValue() {
        this.list = new Array<>();
    }
    public abstract T sum(T a, T b);
    public abstract T sub(T a, T b);
    public abstract T part(T a, int size);
    public void add(T value) {
        if (list.size != capacity) {
            list.add(value);
            sum = sum(sum, value);
        } else {
            sum = sub(sum, list.get(index));
            list.set(index, value);
            sum = sum(sum, value);
            index++;
            if(index == capacity)
                index = 0;
        }
        avg = part(sum, list.size);
    }

    public static class AverageLong extends AverageValue<Long>{
        public AverageLong(){
            sum = 0L;
            avg = 0L;
        }
        @Override
        public Long sum(Long a, Long b) {
            return a + b;
        }

        @Override
        public Long sub(Long a, Long b) {
            return a - b;
        }

        @Override
        public Long part(Long a, int size) {
            return a / size;
        }
    }
    public static class AverageFloat extends AverageValue<Float>{
        public AverageFloat(){
            sum = 0f;
            avg = 0f;
        }
        @Override
        public Float sum(Float a, Float b) {
            return a + b;
        }

        @Override
        public Float sub(Float a, Float b) {
            return a - b;
        }

        @Override
        public Float part(Float a, int size) {
            return a / size;
        }
    }
}

