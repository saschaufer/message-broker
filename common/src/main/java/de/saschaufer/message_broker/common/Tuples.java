package de.saschaufer.message_broker.common;

public abstract class Tuples {

    public record Tuple2<T1, T2>(T1 t1, T2 t2) {
    }

    public record Tuple3<T1, T2, T3>(T1 t1, T2 t2, T3 t3) {
    }

    public static <T1, T2> Tuple2<T1, T2> of(final T1 t1, final T2 t2) {
        return new Tuple2<>(t1, t2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(final T1 t1, final T2 t2, final T3 t3) {
        return new Tuple3<>(t1, t2, t3);
    }
}
