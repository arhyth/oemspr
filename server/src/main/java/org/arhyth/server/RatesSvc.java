package org.arhyth.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.arhyth.oemspr.rates.Rate;
import org.arhyth.oemspr.rates.Repository;
import org.arhyth.oemspr.rates.Service;

@Qualifier("passthrough")
public class RatesSvc implements Service {
    public final Repository repository;

    @Autowired
    public RatesSvc(@Qualifier("noop") Repository repo) {
        this.repository = repo;
    }

    public Rate latest() {
        return repository.latest();
    }
}
