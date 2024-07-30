package com.example.springbootkafkaproducer.controller;

import com.example.springbootkafkaproducer.service.ProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@RestController
public class ProducerController {

    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping("/publish/{size}")
    public ResponseEntity<?> generate(@PathVariable Integer size) {
        Instant startTime = Instant.now();
        AtomicBoolean isFirst = new AtomicBoolean(true);
        IntStream.range(0, 100_000)
                .forEachOrdered(value -> {
                            if (isFirst.get()) {
                                System.out.println("First sent time: " + Instant.now());
                                isFirst.set(false);
                            }
                            producerService.sendMessage(Arrays.toString(new byte[size]));
                        }
                );
        Duration between = Duration.between(startTime, Instant.now());
        String format = String.format("Produce millis: %d, nanos: %d", between.toMillis(), between.toNanos());
        System.out.println(format);
        return ResponseEntity.ok(format);
    }

//    @GetMapping("/measure-first-and-last/{size}")
//    public ResponseEntity<?> measureFirstAndLastMessageTimes(@PathVariable Integer size) {
//        Instant startTime = Instant.now();
//        boolean isFirst = true;
//        AtomicReference<Instant> firstMessageSentTime = new AtomicReference<>();
//        IntStream.range(0, 10_000)
//                .forEachOrdered(value -> {
//                    if (isFirst) {
//                        firstMessageSentTime.set(Instant.now());
//                        producerService.sendMessage(Arrays.toString(new byte[size]));
//                        return;
//                    }
//                    producerService.sendMessage(Arrays.toString(new byte[size]));
//
//                });
//        Duration between = Duration.between(startTime, Instant.now());
//        String format = String.format("Produce millis: %d, nanos: %d", between.toMillis(), between.toNanos());
//        System.out.println(format);
//        return ResponseEntity.ok(format);
//    }
}
