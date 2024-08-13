package com.matvey.perelman.gdxcollider.scheduler;

import com.matvey.perelman.gdxcollider.scheduler.pools.ObjectPool;

import java.util.TreeMap;

public class TreeSet<T extends Comparable<T>> {
    private final ObjectPool<TreeNode<T>> pool;
    private TreeNode<T> root;

    public TreeSet() {
        pool = new ObjectPool<>(TreeNode::new);
        new TreeMap<>();
    }

    public T first() {
        TreeNode<T> node = firstNode();
        return node == null? null: node.value;
    }

    public T last() {
        TreeNode<T> node = lastNode();
        return node == null? null: node.value;
    }
    public T removeFirst(){
        TreeNode<T> node = firstNode();
        if(node == null)
            return null;
        T val = node.value;
        deleteNode(node);
        return val;
    }
    public T removeLast(){
        TreeNode<T> node = lastNode();
        if(node == null)
            return null;
        T val = node.value;
        deleteNode(node);
        return val;
    }
    public boolean isEmpty(){
        return root == null;
    }
    public TreeNode<T> firstNode(){
        if (root == null)
            return null;
        TreeNode<T> node = root;
        while (node.l != null)
            node = node.l;
        return node;
    }
    public TreeNode<T> lastNode(){
        if (root == null)
            return null;
        TreeNode<T> node = root;
        while (node.r != null)
            node = node.r;
        return node;
    }
    public TreeNode<T> find(T value) {
        if (root == null)
            return null;
        TreeNode<T> node = root;
        while (true) {
            int cmp = value.compareTo(node.value);
            if (cmp == 0)
                return node;
            if (cmp < 0) {
                if (node.l == null)
                    return null;
                node = node.l;
            } else {
                if (node.r == null)
                    return null;
                node = node.r;
            }
        }
    }

    public boolean remove(T value) {
        TreeNode<T> node = find(value);
        if (node == null)
            return false;
        deleteNode(node);
        return true;
    }

    public T add(T value) {
        if (value == null)
            return null;
        TreeNode<T> t = root;
        if (t == null) {
            root = pool.get();
            root.value = value;
            return null;
        }
        int cmp;
        TreeNode<T> parent;
        // split comparator and comparable paths

        do {
            parent = t;
            cmp = value.compareTo(t.value);
            if (cmp < 0)
                t = t.l;
            else if (cmp > 0)
                t = t.r;
            else {
                T oldValue = t.value;
                t.value = value;
                return oldValue;
            }
        } while (t != null);

        TreeNode<T> e = pool.get();
        e.color = BLACK;
        e.p = parent;
        e.value = value;
        if (cmp < 0)
            parent.l = e;
        else
            parent.r = e;
        fixAfterInsertion(e);
        return null;
    }

    private void deleteNode(TreeNode<T> p) {
        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.l != null && p.r != null) {
            TreeNode<T> s = p.next();
            p.value = s.value;
            p = s;
        }// p has 2 children

        // Start fixup at replacement node, if it exists.
        TreeNode<T> replacement = (p.l != null ? p.l : p.r);

        if (replacement != null) {
            // Link replacement to parent
            replacement.p = p.p;
            if (p.p == null)
                root = replacement;
            else if (p == p.p.l)
                p.p.l = replacement;
            else
                p.p.r = replacement;

            // Null out links so they are OK to use by fixAfterDeletion.
            p.l = p.r = p.p = null;

            // Fix replacement
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
            pool.free(p);
        } else if (p.p == null) { // return if we are the only node.
            pool.free(p);
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK)
                fixAfterDeletion(p);

            if (p.p != null) {
                if (p == p.p.l)
                    p.p.l = null;
                else if (p == p.p.r)
                    p.p.r = null;
                p.p = null;
            }
            pool.free(p);
        }
    }

    private void fixAfterDeletion(TreeNode<T> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                TreeNode<T> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib)) == BLACK &&
                        colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                TreeNode<T> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                        colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

    private static <T> boolean colorOf(TreeNode<T> p) {
        return (p == null ? BLACK : p.color);
    }

    private static <T> TreeNode<T> parentOf(TreeNode<T> p) {
        return (p == null ? null : p.p);
    }

    private static <T> void setColor(TreeNode<T> p, boolean c) {
        if (p != null)
            p.color = c;
    }

    private static <T> TreeNode<T> leftOf(TreeNode<T> p) {
        return (p == null) ? null : p.l;
    }

    private static <T> TreeNode<T> rightOf(TreeNode<T> p) {
        return (p == null) ? null : p.r;
    }

    /**
     * From CLR
     */
    private void rotateLeft(TreeNode<T> p) {
        if (p != null) {
            TreeNode<T> r = p.r;
            p.r = r.l;
            if (r.l != null)
                r.l.p = p;
            r.p = p.p;
            if (p.p == null)
                root = r;
            else if (p.p.l == p)
                p.p.l = r;
            else
                p.p.r = r;
            r.l = p;
            p.p = r;
        }
    }

    /**
     * From CLR
     */
    private void rotateRight(TreeNode<T> p) {
        if (p != null) {
            TreeNode<T> l = p.l;
            p.l = l.r;
            if (l.r != null) l.r.p = p;
            l.p = p.p;
            if (p.p == null)
                root = l;
            else if (p.p.r == p)
                p.p.r = l;
            else p.p.l = l;
            l.r = p;
            p.p = l;
        }
    }

    private void fixAfterInsertion(TreeNode<T> x) {
        x.color = RED;

        while (x != null && x != root && x.p.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                TreeNode<T> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                TreeNode<T> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    public static class TreeNode<T> {
        private TreeNode<T> l, r, p;
        private T value;
        private boolean color = BLACK;

        public TreeNode<T> next() {
            TreeNode<T> t = this;
            TreeNode<T> p;
            if (t.r != null) {
                p = t.r;
                while (p.l != null)
                    p = p.l;
            } else {
                p = t.p;
                TreeNode<T> ch = t;
                while (p != null && ch == p.r) {
                    ch = p;
                    p = p.p;
                }
            }
            return p;
        }

        public TreeNode<T> prev() {
            TreeNode<T> t = this;
            TreeNode<T> p;
            if (t.l != null) {
                p = t.l;
                while (p.r != null)
                    p = p.r;
            } else {
                p = t.p;
                TreeNode<T> ch = t;
                while (p != null && ch == p.l) {
                    ch = p;
                    p = p.p;
                }
            }
            return p;
        }

        public T value() {
            return value;
        }
    }
}
