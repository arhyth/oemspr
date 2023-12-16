package org.arhyth.oemspr.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Threads;

import org.arhyth.oemspr.rates.noop.RatesRepo;
import org.arhyth.oemspr.rates.noop.RatesSvc;

public class RatesSvcBenchmark {
    @Threads(32)
    @Benchmark
    public void testLatest() {
        RatesSvc svc = new RatesSvc(new RatesRepo());
        svc.latest();
    }
}
