package com.demo.kafka.controller;

import com.demo.kafka.service.AverageTimeService;
//import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/time-records")
public class ConsumerController {

    private final AverageTimeService averageTimeService;

    public ConsumerController(AverageTimeService averageTimeService) {
        this.averageTimeService = averageTimeService;
    }

    @GetMapping("/retrieve")
//    @ApiOperation(value = "Get measurement results.",
//            notes = "Returns all records that measured between produce and consume process.")
    public ResponseEntity<?> retrieveTimeRecords() {
        return ResponseEntity.ok(averageTimeService.get());
    }

    @DeleteMapping("/clear")
//    @ApiOperation(value = "Clear measure list.", notes = "Clear measure list.")
    public ResponseEntity<Void> clearTimeRecords() {
        averageTimeService.clear();
        return ResponseEntity.ok().build();
    }
}
