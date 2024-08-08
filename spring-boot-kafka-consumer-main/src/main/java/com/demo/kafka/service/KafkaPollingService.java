package com.demo.kafka.service;

import com.demo.kafka.constants.FileConstant;
import com.example.springbootkafkaproducer.data.BaseMessage;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class KafkaPollingService {

    private int expectedItemCount = 100_000;
    private File file;
    private final List<byte[]> receivedMessages = new ArrayList<>();

    @Getter
    private final Queue<byte[]> recordQueue = new LinkedList<>();

    private final Consumer<String, byte[]> kafkaConsumer;


    @Value("${spring.kafka.topic.name}")
    private String topicName;

    private final List<Long> receivedMessagesTs = new ArrayList<>();

    public KafkaPollingService(ConsumerFactory<String, byte[]> consumerFactory) {
        this.kafkaConsumer = consumerFactory.createConsumer();
    }

    @PostConstruct
    public void init() {
        try {
            kafkaConsumer.subscribe(Collections.singletonList(topicName));
            generateFile();
            startPolling();
        } catch (Exception e) {
            log.error("Failed to initialize Kafka consumer: {}", e.getMessage());
        }
    }

    public void startPolling() {

        Runnable pollTask = () -> {
            while (true) {
                try {
                    ConsumerRecords<String, byte[]> poll = kafkaConsumer.poll(Duration.ofMillis(1));
                    long now = System.nanoTime();

                    if (poll.count() > 0) {
                        log.info("RECEIVED {} items", poll.count());
                    }

                    for (ConsumerRecord<String, byte[]> record : poll) {
                        processRecord(record.value(), now);
                    }
                } catch (Exception e) {
                    log.error("Cannot consume record. {}", e);
                }
            }
        };

        Thread pollingThread = new Thread(pollTask);
        pollingThread.setDaemon(true);
        pollingThread.start();
    }



    private void processRecord(byte[] record, long now) {
        receivedMessages.add(record);
        receivedMessagesTs.add(now);

        if (receivedMessages.size() == expectedItemCount) {
            log.info("Received {} items, dumping reports...", expectedItemCount);
            try {
                dumpReport();
            } catch (Exception e) {
                log.error("Exception on report dump.", e);
            }
        }
    }

    public void setExpectedItemCount(final int value) {
        this.expectedItemCount = value;
        reset();
    }

    private BaseMessage deserialize(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (BaseMessage) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Exception.", e);
            return null;
        }
    }

    public String dumpReport() {
        log.info("Dumping results...");

        long totalElapsedTime = 0;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < receivedMessages.size(); ++i) {
            BaseMessage message = deserialize(receivedMessages.get(i));
            message.setRecvTimestamp(receivedMessagesTs.get(i));

            long elapsedTime = message.getRecvTimestamp() - message.getSendTimestamp();
            totalElapsedTime += elapsedTime;

            sb.append(String.format(
                    "Msg-%d: sendTs %d, recvTs %d -> elapsedTime %f ms, %d ns\n",
                    i + 1,
                    message.getSendTimestamp(),
                    message.getRecvTimestamp(),
                    (1.0f * elapsedTime) / 1_000_000,
                    elapsedTime
                ));
        }

        write("Report Result:\n", StandardOpenOption.TRUNCATE_EXISTING);

        String result = (((totalElapsedTime / receivedMessages.size()) / 1_000_000) + " ms, ") + ((totalElapsedTime / receivedMessages.size()) + " ns");
        sb.append("AVERAGE: ").append(result).append("\n");
        write(sb.toString(), StandardOpenOption.APPEND);

        reset();
        return result;
    }

    private void write(final String data, final OpenOption openOption) {
        try {
            Files.write(file.toPath(), data.getBytes(), StandardOpenOption.WRITE, openOption);
        } catch (Exception e) {
            log.error("Write failed.", e);
        }
    }

    private void reset() {
        log.info("Resetting listener.");

        receivedMessages.clear();
        receivedMessagesTs.clear();
    }

    private void generateFile() {
        try {
            file = new File(FileConstant.OUTPUT_FILE_PATH);

            if (file.createNewFile()) {
                log.info("Created file {}", file.getPath());
            } else {
                log.error("File creation failed for {}", FileConstant.OUTPUT_FILE_PATH);
            }
        } catch (IOException e) {
            log.error("Exception.", e);
        }
    }
}
