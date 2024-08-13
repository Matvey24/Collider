package com.matvey.perelman.gdxcollider.scheduler;


import com.matvey.perelman.gdxcollider.scheduler.pools.ObjectPool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

public class LinkedList<T> implements Iterable<T> {
    private final HashMap<T, Linkable<T>> nodes;
    private final ObjectPool<Linkable<T>> pool;
    private Linkable<T> first, last;
    public LinkedList(){
        this(new ObjectPool<>(Linkable::new));
    }
    private LinkedList(ObjectPool<Linkable<T>> pool){
        this.pool = pool;
        this.nodes = new HashMap<>();
    }
    public T first(){
        if(first == null)
            return null;
        return first.value;
    }
    public T last(){
        if(last == null)
            return null;
        return last.value;
    }
    public T next(T value){
        Linkable<T> node = nodes.get(value);
        if(node == null || node.next == null)
            return null;
        return node.next.value;
    }
    public T prev(T value){
        Linkable<T> node = nodes.get(value);
        if(node == null || node.prev == null)
            return null;
        return node.prev.value;
    }
    public void addBefore(T value, T before){
        Linkable<T> b = nodes.get(before);
        assert b != null;
        Linkable<T> val = pool.get();
        val.value = value;
        if(nodes.putIfAbsent(value, val) != null)
            throw new RuntimeException("Adding existing value");
        val.next = b;
        val.prev = b.prev;
        b.prev = val;
        if(val.prev != null)
            val.prev.next = val;
        else
            first = val;
    }
    public void addAfter(T value, T after){
        Linkable<T> a = nodes.get(after);
        assert a != null;
        Linkable<T> val = pool.get();
        val.value = value;
        if(nodes.putIfAbsent(value, val) != null)
            throw new RuntimeException("Adding existing value");
        val.prev = a;
        val.next = a.next;
        a.next = val;
        if(val.next != null)
            val.next.prev = val;
        else
            last = val;
    }
    public void addFirst(T value){
        Linkable<T> val = pool.get();
        val.value = value;
        if(nodes.putIfAbsent(value, val) != null)
            throw new RuntimeException("Adding existing value");
        val.prev = null;
        val.next = first;
        first = val;
        if(val.next == null)
            last = val;
        else
            val.next.prev = val;
    }
    public void addLast(T value){
        Linkable<T> val = pool.get();
        val.value = value;
        if(nodes.putIfAbsent(value, val) != null)
            throw new RuntimeException("Adding existing value");
        val.next = null;
        val.prev = last;
        last = val;
        if(val.prev == null)
            first = val;
        else
            val.prev.next = val;
    }
    public T removeBefore(T value){
        Linkable<T> v = nodes.get(value);
        assert v != null;
        Linkable<T> prev = v.prev;
        if(prev == null)
            return null;
        v.prev = prev.prev;
        if(v.prev != null)
            v.prev.next = v;
        else
            first = v;
        T ret = prev.value;
        nodes.remove(ret);
        pool.free(prev);
        return ret;
    }
    public T removeAfter(T value){
        Linkable<T> v = nodes.get(value);
        assert v != null;
        Linkable<T> next = v.next;
        if(next == null)
            return null;
        v.next = next.next;
        if(v.next != null)
            v.next.prev = v;
        else
            last = v;
        T ret = next.value;
        nodes.remove(ret);
        pool.free(next);
        return ret;
    }
    public T removeFirst(){
        Linkable<T> v = first;
        if(v == null)
            return null;
        T ret = v.value;
        first = v.next;
        if(v.next == null)
            last = null;
        else
            v.next.prev = null;
        nodes.remove(ret);
        pool.free(v);
        return ret;
    }
    public T removeLast(){
        Linkable<T> v = last;
        if(v == null)
            return null;
        T ret = v.value;
        last = v.prev;
        if(v.prev == null)
            first = null;
        else
            v.prev.next = null;
        nodes.remove(ret);
        pool.free(v);
        return ret;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }
    public static class Linkable<T> {
        Linkable<T> next;
        Linkable<T> prev;
        T value;
    }
    private class LinkedListIterator implements Iterator<T>{
        private Linkable<T> current;
        public LinkedListIterator(){
            current = LinkedList.this.first;
        }
        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if(current == null)
                return null;
            T ret = current.value;
            current = current.next;
            return ret;
        }

    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Linkable<T> cur = first;
        while(cur != null){
            action.accept(cur.value);
            cur = cur.next;
        }
    }
}
