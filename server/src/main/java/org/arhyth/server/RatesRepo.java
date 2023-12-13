package org.arhyth.server;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Qualifier;

import org.arhyth.oemspr.rates.Rate;
import org.arhyth.oemspr.rates.Repository;

@Qualifier("noop")
public class RatesRepo implements Repository {
    private final Rate rate;

    public RatesRepo() {
        this.rate = new Rate();
        this.rate.timestamp = Instant.now().truncatedTo(ChronoUnit.HOURS).getEpochSecond();
        this.rate.base = "USD";
        this.rate.pairs = new HashMap<String, Float>();
        this.rate.pairs.put("PHP", 55.0f);
    }

    public Rate latest() {
        return rate;
    }
}