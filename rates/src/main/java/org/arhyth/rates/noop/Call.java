package org.arhyth.oemspr.rates.noop;

import java.util.concurrent.CountDownLatch;

public class Call<V> {
    public Integer dupes;
    public CountDownLatch latch;
    public V value;

    public Call() {
        this.dupes = 0;
        this.latch = new CountDownLatch(1);
    }
}
