package com.matvey.perelman.gdxcollider.scheduler.pools;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ObjectPool<T> implements Supplier<T> {
    private final ArrayList<T> free;
    private final Supplier<T> supplier;

    public ObjectPool(Supplier<T> supplier){
        free = new ArrayList<>();
        this.supplier = supplier;
    }
    public T get(){
        if(free.isEmpty())
            return supplier.get();
        else
            return free.remove(free.size() - 1);
    }

    public void free(T t){
        free.add(t);
    }

    public void clearBuffer(){
        free.clear();
    }

    public static class UIDPool<T extends UID> extends ObjectPool<T>{
        public long last_uid = 0;
        public UIDPool(Supplier<T> supplier) {
            super(supplier);
        }
        public T get(){
            T value = super.get();
            last_uid += 1;
            value.init(last_uid);
            return value;
        }
    }
    public static class DisposingPool<T extends Disposable> extends ObjectPool<T> {
        public DisposingPool(Supplier<T> supplier){
            super(supplier);
        }
        @Override
        public void free(T t) {
            t.dispose();
            super.free(t);
        }
    }
}
