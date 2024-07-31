package com.demo.kafka.listener;

import com.demo.kafka.service.AverageTimeService;
import com.example.springbootkafkaproducer.data.BaseMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaConsumerListener {

    private final long LIMIT = 100_000;
    private final String outFilePath = "C:\\Users\\berkecan.ozgur\\Desktop\\project-files\\project\\kafka-produce-consume\\docs\\time-consumption.txt";
    private final Path filePath;
    private int count = 0;
    private List<String> lines;
    private long total = 0;

    private final AverageTimeService averageTimeService;

    public KafkaConsumerListener(AverageTimeService averageTimeService) {
        this.averageTimeService = averageTimeService;
        lines = new ArrayList<>();
        filePath = Paths.get(outFilePath);
        generateFile();
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.name}",
            groupId = "${spring.kafka.group-id}")
    public void listen(byte[] message) {
        count++;

        long between = ChronoUnit.MILLIS.between(deserialize(message).getTimestamp(), Instant.now());
        lines.add(String.valueOf(between));
        total += between;

        if (count == LIMIT) {
            count = 0;
            appendLines();
        }
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
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendLines() {
        try {
            long avg = total / LIMIT;
            averageTimeService.add(avg);
            lines.add(String.format("avg is: %d", avg));
            Files.write(filePath, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            total = 0;
            lines = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
