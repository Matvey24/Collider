package com.matvey.perelman.gdxcollider.scheduler;

import com.matvey.perelman.gdxcollider.scheduler.pools.ObjectPool;

public class TreeSet<T extends Comparable<T>> {
    private final ObjectPool<TreeNode> pool;
    private TreeNode root;
    private int count;

    public TreeSet(){
        pool = new ObjectPool<>(TreeNode::new);
    }

    public class TreeNode{
        public int height;
        public TreeNode l, r;
        public T value;

    }
}
