package com.demo.kafka.listener;

import com.demo.kafka.constants.FileConstant;
import com.demo.kafka.service.AverageTimeService;
import com.example.springbootkafkaproducer.data.BaseMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class KafkaConsumerListener {

    private final int LIMIT = 100_000;
    private File file;
    private int count = 0;
    private List<String> lines;
    private long total = 0;

    private final ReentrantLock lock;

    private final AverageTimeService averageTimeService;
    private boolean isFirst = true;
    private LocalDateTime firstLDT = null;

    public KafkaConsumerListener(AverageTimeService averageTimeService) {
        this.averageTimeService = averageTimeService;
        lock = new ReentrantLock();
        lines = new ArrayList<>();
        generateFile();
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.name}",
            groupId = "${spring.kafka.group-id}")
    public void listen(byte[] message) {
        lock.lock();
        if (isFirst) {
            firstLDT = LocalDateTime.now();
            System.out.println("First received time: " + firstLDT);
            isFirst = false;
        }
        count++;
        LocalDateTime now = LocalDateTime.now();
        long between = ChronoUnit.MILLIS.between(deserialize(message).getTimestamp(), now);
        lines.add(String.valueOf(between));
        total += between;

        if (count == LIMIT) {
            appendLines();
            LocalDateTime endLDT = LocalDateTime.now();
            long consumerTimeDiff = ChronoUnit.MILLIS.between(firstLDT, endLDT);
            System.out.printf("Last received time: %s, diff is: %d, avg: %d%n", endLDT, consumerTimeDiff, consumerTimeDiff / count);
            lines = new ArrayList<>();
            total = 0;
            count = 0;
        }
        lock.unlock();
    }

    private BaseMessage deserialize(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (BaseMessage) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateFile() {
        try {
            file = new File(FileConstant.OUTPUT_FILE_PATH);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendLines() {
        long avg = total / LIMIT;
        try {
            averageTimeService.add(avg);
            lines.add(String.format("avg is: %d, total time is: %d", avg, total));
            Files.write(file.toPath(), lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.printf("processed record: %d, avg: %d, %n", count, avg);
        }
    }
}
