package com.demo.kafka.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AverageTimeService {

    private List<Long> records = new ArrayList<>();

    public List<Long> get() {
        return records;
    }

    public void add(Long time) {
        records.add(time);
    }

    public int size() {
        return records.size();
    }

    public void clear() {
        records = new ArrayList<>();
    }

}
