package com.matvey.perelman.gdxcollider.scheduler;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.function.DoubleConsumer;

public class DoubleCallbackProvider {
    private final HashSet<DoubleConsumer> objects;
    public DoubleCallbackProvider(){
        objects = new LinkedHashSet<>();
    }

    public void register(DoubleConsumer t){
        objects.add(t);
    }

    public void unregister(DoubleConsumer t){
        objects.remove(t);
    }

    public void provide(double val){
        objects.forEach(c -> c.accept(val));
    }
    public boolean hasConsumers(){
        return !objects.isEmpty();
    }
}
