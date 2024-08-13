package com.matvey.perelman.gdxcollider.scheduler;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class CallbackProvider<T>{
    private final HashSet<Consumer<T>> objects;
    public CallbackProvider(){
        objects = new LinkedHashSet<>();
    }

    public void register(Consumer<T> t){
        objects.add(t);
    }

    public void unregister(Consumer<T> t){
        objects.remove(t);
    }

    public void provide(T val){
        objects.forEach(c -> c.accept(val));
    }
    public boolean hasConsumers(){
        return !objects.isEmpty();
    }
}
