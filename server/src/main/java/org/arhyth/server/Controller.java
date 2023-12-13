package org.arhyth.oemspr.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.arhyth.oemspr.rates.Rate;
import org.arhyth.oemspr.rates.Service;

@RestController
@RequestMapping("/api")
public class Controller {
    private final Service ratesService;

    @Autowired
    public Controller(@Qualifier("passthrough") Service ratesService) {
        this.ratesService = ratesService;
    }

    @GetMapping("/latest.json")
    public ResponseEntity<byte[]> latest() throws JsonProcessingException {
        Rate rate = ratesService.latest();
        ObjectMapper mapper = new ObjectMapper();
        byte[] resp = mapper.writeValueAsBytes(rate);
        HttpHeaders hdrs = new HttpHeaders();
        hdrs.set("Content-Type", "application/json");
        return new ResponseEntity<byte[]>(resp, hdrs, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<String>("An internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
