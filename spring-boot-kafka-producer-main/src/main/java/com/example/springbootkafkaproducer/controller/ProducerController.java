package com.example.springbootkafkaproducer.controller;

import com.example.springbootkafkaproducer.data.BaseMessage;
import com.example.springbootkafkaproducer.service.ProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@RestController
public class ProducerController {

    private static final int LOOP_LIMIT = 100_000;
    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping("/generate-1kb")
    public ResponseEntity<?> generateSmall() {
        Instant startTime = Instant.now();
        AtomicBoolean isFirst = new AtomicBoolean(true);
        IntStream.range(0, LOOP_LIMIT)
                .forEachOrdered(value -> {
                            if (isFirst.get()) {
                                System.out.println("First sent time: " + Instant.now());
                                isFirst.set(false);
                            }
                            producerService.sendMessage(serialize(new BaseMessage(1)));
                        }
                );
        Duration between = Duration.between(startTime, Instant.now());
        String format = String.format("Produce millis: %d, nanos: %d", between.toMillis(), between.toNanos());
        System.out.println(format);
        return ResponseEntity.ok(format);
    }

    @GetMapping("/generate-16kb")
    public ResponseEntity<?> generateHuge() {
        Instant startTime = Instant.now();
        AtomicBoolean isFirst = new AtomicBoolean(true);
        IntStream.range(0, LOOP_LIMIT)
                .forEachOrdered(value -> {
                            if (isFirst.get()) {
                                System.out.println("First sent time: " + Instant.now());
                                isFirst.set(false);
                            }
                            producerService.sendMessage(serialize(new BaseMessage(16)));
                        }
                );
        Duration between = Duration.between(startTime, Instant.now());
        String format = String.format("Produce millis: %d, nanos: %d", between.toMillis(), between.toNanos());
        System.out.println(format);
        return ResponseEntity.ok(format);
    }

    @GetMapping("/generate-4kb")
    public ResponseEntity<?> generateMean() {
        Instant startTime = Instant.now();
        AtomicBoolean isFirst = new AtomicBoolean(true);
        IntStream.range(0, LOOP_LIMIT)
                .forEachOrdered(value -> {
                            if (isFirst.get()) {
                                System.out.println("First sent time: " + Instant.now());
                                isFirst.set(false);
                            }
                            producerService.sendMessage(serialize(new BaseMessage(4)));
                        }
                );
        Duration between = Duration.between(startTime, Instant.now());
        String format = String.format("Produce millis: %d, nanos: %d", between.toMillis(), between.toNanos());
        System.out.println(format);
        return ResponseEntity.ok(format);
    }

    private byte[] serialize(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
