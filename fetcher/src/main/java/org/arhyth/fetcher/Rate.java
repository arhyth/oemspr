package org.arhyth.fetcher;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "disclaimer", "license" })
public class Rate {
    public long timestamp;
    public String base;
    public Map<String, Float> rates;
}
