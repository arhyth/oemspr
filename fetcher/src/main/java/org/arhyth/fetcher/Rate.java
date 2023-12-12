package org.arhyth.fetcher;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "disclaimer", "license" })
public class Rate {
    public long timestamp;
    public String base;
    @JsonProperty("rates")
    public Map<String, Float> pairs;
}
