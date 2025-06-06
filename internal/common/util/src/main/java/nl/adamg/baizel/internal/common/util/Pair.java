package nl.adamg.baizel.internal.common.util;

public final class Pair<T1, T2> {
    private final T1 first;
    private final T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        return this.first.equals(((Pair<?, ?>) o).getFirst()) && this.second.equals(((Pair<?, ?>) o).getSecond());
    }

    @Override
    public String toString() {
        return first + ", " + second;
    }
}
