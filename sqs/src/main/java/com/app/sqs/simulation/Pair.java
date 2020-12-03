package com.app.sqs.simulation;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Taken from Spring framework data Pair
 * @param <S>
 * @param <T>
 */
public final class Pair<S, T> {
    private final S first;
    private final T second;

    public static <S, T> Pair<S, T> of(S first, T second) {
        return new Pair(first, second);
    }

    public S getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }

    public static <S, T> Collector<Pair<S, T>, ?, Map<S, T>> toMap() {
        return Collectors.toMap(Pair::getFirst, Pair::getSecond);
    }

    public String toString() {
        return "Pair(first=" + this.getFirst() + ", second=" + this.getSecond() + ")";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Pair)) {
            return false;
        } else {
            Pair<?, ?> other = (Pair) o;
            Object this$first = this.getFirst();
            Object other$first = other.getFirst();
            if (this$first == null) {
                if (other$first != null) {
                    return false;
                }
            } else if (!this$first.equals(other$first)) {
                return false;
            }

            Object this$second = this.getSecond();
            Object other$second = other.getSecond();
            if (this$second == null) {
                if (other$second != null) {
                    return false;
                }
            } else if (!this$second.equals(other$second)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        int result = 1;
        Object $first = this.getFirst();
        result = result * 59 + ($first == null ? 43 : $first.hashCode());
        Object $second = this.getSecond();
        result = result * 59 + ($second == null ? 43 : $second.hashCode());
        return result;
    }

    private Pair(S first, T second) {
        if (first == null) {
            throw new IllegalArgumentException("first is marked non-null but is null");
        } else if (second == null) {
            throw new IllegalArgumentException("second is marked non-null but is null");
        } else {
            this.first = first;
            this.second = second;
        }
    }
}
