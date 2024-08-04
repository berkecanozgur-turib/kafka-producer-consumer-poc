package com.example.springbootkafkaproducer.controller;

import com.example.springbootkafkaproducer.data.BaseMessage;
import com.example.springbootkafkaproducer.service.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@SuppressWarnings("java:S6813")
public class ProducerController {
    @Autowired private ProducerService producerService;
    private static final byte[] EMPTY_BYTES = new byte[0];

    @GetMapping("/throughputTest/testCount={testCount}/itemCount={itemCount}/kiloBytePerItem={kiloBytePerItem}")
    public ResponseEntity<Object> throughputTest(
        @PathVariable(name = "testCount") final int testCount,
        @PathVariable(name = "itemCount") final int itemCount,
        @PathVariable(name = "kiloBytePerItem") final int kiloBytePerItem) {

        log.info("Producer throughput test: Preparing {} items with {} kilobytes...", itemCount, kiloBytePerItem);

        List<byte[]> objectsToSend = new ArrayList<>();
        for (int i = 0; i < itemCount; ++i) {
            objectsToSend.add(serialize(new BaseMessage(kiloBytePerItem)));
        }

        List<String> results = new ArrayList<>();
        results.add(String.format("Producer throughput test %d items with %d kilobytes.", itemCount, kiloBytePerItem));

        for (int i = 0; i < testCount; ++i) {
            String report = String.format("  TEST-%d: %s", i + 1, sendThroughputMessages(objectsToSend));
            log.info("{}", report);
            results.add(report);
        }

        return ResponseEntity.ok(String.join("\n", results));
    }

    @GetMapping("/rttTest/testCount={testCount}/itemCount={itemCount}/kiloBytePerItem={kiloBytePerItem}")
    public ResponseEntity<Object> rttTest(
        @PathVariable(name = "testCount") final int testCount,
        @PathVariable(name = "itemCount") final int itemCount,
        @PathVariable(name = "kiloBytePerItem") final int kiloBytePerItem) {

        log.info("Producer rtt test: Preparing {} items with {} kilobytes...", itemCount, kiloBytePerItem);

        List<BaseMessage> objectsToSend = new ArrayList<>();
        for (int i = 0; i < itemCount; ++i) {
            objectsToSend.add(new BaseMessage(kiloBytePerItem));
        }

        for (int i = 0; i < testCount; ++i) {
            sendRttMessages(objectsToSend);
            log.info("Sent {} items", objectsToSend.size());
        }

        return ResponseEntity.ok("Producer rtt test: Done {} items with {} kilobytes.");
    }

    private String sendThroughputMessages(final List<byte[]> objectsToSend) {
        Instant startTime = Instant.now();

        for (byte[] bytes : objectsToSend) {
            producerService.sendMessage(bytes);
        }

        Duration between = Duration.between(startTime, Instant.now());

        return String.format("Elapsed time: %d sec/%d ms/%d ns", between.toSeconds(), between.toMillis(), between.toNanos());
    }

    private void sendRttMessages(final List<BaseMessage> objectsToSend) {
        for (BaseMessage message : objectsToSend) {
            message.setSendTimestamp(LocalDateTime.now());
            producerService.sendMessage(serialize(message));
        }
    }

    private byte[] serialize(final Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Exception on serialization.", e);
            return EMPTY_BYTES;
        }
    }
}
