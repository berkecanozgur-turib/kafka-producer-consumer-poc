package com.turib.demo.kafka.producer.controller;

import com.turib.demo.kafka.commons.BaseMessage;
import com.turib.demo.kafka.producer.service.ProducerService;
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

        if (testCount < 1 || itemCount < 1 || kiloBytePerItem < 1) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Producer throughput test: Preparing {} items with {} kilobytes...", itemCount, kiloBytePerItem);

        List<byte[]> objectsToSend = new ArrayList<>();
        for (int i = 0; i < itemCount; ++i) {
            objectsToSend.add(serialize(new BaseMessage(kiloBytePerItem)));
        }

        List<String> results = new ArrayList<>();
        results.add(String.format("Producer throughput test %d items with %d kilobytes.", itemCount, kiloBytePerItem));

        log.info("Producer throughput test: Sending {} items with {} kilobytes...", itemCount, kiloBytePerItem);

        for (int i = 0; i < testCount; ++i) {
            String report = String.format("  TEST-%d: %s", i + 1, sendThroughputMessages(objectsToSend));
            log.info("{}", report);
            results.add(report);
        }

        log.info("Producer throughput test: Sent {} items with {} kilobytes.", itemCount, kiloBytePerItem);

        return ResponseEntity.ok(String.join("\n", results));
    }

    @GetMapping("/rttTest/testCount={testCount}/itemProduceSleepMs={itemProduceSleepMs}/itemCount={itemCount}/kiloBytePerItem={kiloBytePerItem}")
    public ResponseEntity<Object> rttTest(
        @PathVariable(name = "testCount") final int testCount,
        @PathVariable(name = "itemProduceSleepMs") final int itemProduceSleepMs,
        @PathVariable(name = "itemCount") final int itemCount,
        @PathVariable(name = "kiloBytePerItem") final int kiloBytePerItem) {

        if (testCount < 1 || itemProduceSleepMs < 0 || itemCount < 1 || kiloBytePerItem < 1) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Producer rtt test: Preparing {} items with {} kilobytes...", itemCount, kiloBytePerItem);

        List<BaseMessage> objectsToSend = new ArrayList<>();
        for (int i = 0; i < itemCount; ++i) {
            objectsToSend.add(new BaseMessage(kiloBytePerItem));
        }

        log.info("Producer rtt test: Sending {} items with {} kilobytes...", itemCount, kiloBytePerItem);

        for (int i = 0; i < testCount; ++i) {
            sendRttMessages(objectsToSend, itemProduceSleepMs);
            log.info("Sent {} items", objectsToSend.size());
        }

        log.info("Producer rtt test: Sent {} items with {} kilobytes.", itemCount, kiloBytePerItem);

        return ResponseEntity.ok("Producer rtt test: Done " + objectsToSend.size() + " items with " + kiloBytePerItem + " kilobytes.");
    }

    private String sendThroughputMessages(final List<byte[]> objectsToSend) {
        Instant startTime = Instant.now();

        for (byte[] bytes : objectsToSend) {
            producerService.sendMessage(bytes);
        }

        Duration elapsed = Duration.between(startTime, Instant.now());

        return String.format("Elapsed time: %d sec/%d ms/%d ns", elapsed.toSeconds(), elapsed.toMillis(), elapsed.toNanos());
    }

    private void sendRttMessages(final List<BaseMessage> objectsToSend, final int itemProduceSleepMs) {
        for (BaseMessage message : objectsToSend) {
            message.setSendTimestamp(Instant.now());
            producerService.sendMessage(serialize(message));

            if (itemProduceSleepMs > 0) {
                try {
                    Thread.sleep(itemProduceSleepMs);
                } catch (InterruptedException e) {
                    // no action
                }
            }
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
