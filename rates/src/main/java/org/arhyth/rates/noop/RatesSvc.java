package org.arhyth.oemspr.rates.noop;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.arhyth.oemspr.rates.Rate;
import org.arhyth.oemspr.rates.Repository;
import org.arhyth.oemspr.rates.Service;

@Qualifier("noop")
public class RatesSvc implements Service {
    private final Repository repository;
    private Map<String, Call> calls = new HashMap<String, Call>();

    @Autowired
    public RatesSvc(@Qualifier("noop") Repository repo) {
        this.repository = repo;
    }

    public Rate latest() {
        Call<Rate> call;
        Boolean isFirst = false;

        synchronized(calls) {
            if (calls.get("latest") != null) {
                call = calls.get("latest");
                call.dupes++;
            } else {
                isFirst = true;
                call = new Call<Rate>();
                calls.put("latest", call);
            }
        }

        if (isFirst) {
            Rate rate = repository.latest();
            call.value = rate;
            call.latch.countDown();
            synchronized(calls) {
                calls.remove("latest");
            }
        } else {
            try {
                call.latch.await();
                return call.value;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return call.value;
    }
}
