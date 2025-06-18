package nl.adamg.baizel.internal.common.util.collections;

import nl.adamg.baizel.internal.common.util.concurrent.Lock;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Equivalent to a directed graph, here represented as two flat maps.
 */
@ThreadSafe
public class DirectedGraph<T extends Comparable<T>> {
    private final Map<T, Set<T>> children = new TreeMap<>();
    private final Map<T, Set<T>> parents = new TreeMap<>();
    private final Lock lock = new Lock();

    public Set<T> parents(T node) {
        try(var ignored = lock.read()) {
            return Objects.requireNonNullElse(parents.get(node), Set.of());
        }
    }

    public Set<T> children(T node) {
        try(var ignored = lock.read()) {
            return Objects.requireNonNullElse(children.get(node), Set.of());
        }
    }

    public boolean contains(T node) {
        try(var ignored = lock.read()) {
            return children.containsKey(node);
        }
    }

    public void remove(T key) {
        try(var ignored = lock.write()) {
            var parents = this.parents.get(key);
            if (parents != null) {
                for (var parent : parents) {
                    this.children.get(parent).remove(key);
                }
            }
            var children = this.children.get(key);
            if (children != null) {
                for (var child : children) {
                    this.parents.get(child).remove(key);
                }
            }
            this.parents.remove(key);
            this.children.remove(key);
        }
    }

    public void add(T parent, Set<T> children) {
        try(var ignored = lock.write()) {
            for (var item : children) {
                parents.computeIfAbsent(item, k -> new TreeSet<>()).add(parent);
            }
            this.children.computeIfAbsent(parent, k -> new TreeSet<>()).addAll(children);
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        try(var ignored = lock.read()) {
            return children.size();
        }
    }

    public Set<T> all() {
        return children.keySet();
    }
}
