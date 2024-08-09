package com.turib.demo.kafka.consumer.service;

import com.turib.demo.kafka.commons.BaseMessage;
import com.turib.demo.kafka.consumer.constants.FileConstant;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class KafkaPollingService {
    private int expectedItemCount = 100_000;
    private File file;
    private final List<byte[]> receivedMessages = new ArrayList<>();
    private final List<Instant> receivedMessagesTs = new ArrayList<>();

    @Getter
    private final Queue<byte[]> recordQueue = new LinkedList<>();

    private final Consumer<String, byte[]> kafkaConsumer;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    public KafkaPollingService(final ConsumerFactory<String, byte[]> consumerFactory) {
        this.kafkaConsumer = consumerFactory.createConsumer();
    }

    public void setExpectedItemCount(final int value) {
        this.expectedItemCount = value;
        reset();
    }

    @PostConstruct
    public void init() {
        try {
            generateFile();
            kafkaConsumer.subscribe(Collections.singletonList(topicName));
            startPolling();
        } catch (Exception e) {
            log.error("Failed to initialize Kafka consumer: {}", e.getMessage(), e);
        }
    }

    public void startPolling() {
        Runnable pollTask = () -> {
            while (true) {
                try {
                    ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofMillis(0));

                    if (records.count() > 0 && log.isDebugEnabled()) {
                        log.debug("RECEIVED {} items", records.count());
                    }

                    Instant now = Instant.now();

                    for (ConsumerRecord<String, byte[]> record : records) {
                        processRecord(record.value(), now);
                    }
                } catch (Exception e) {
                    log.error("Cannot consume record.", e);
                }
            }
        };

        Thread pollingThread = new Thread(pollTask);
        pollingThread.setDaemon(true);
        pollingThread.start();
    }


    private void processRecord(final byte[] record, final Instant now) {
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

    public String dumpReport() {
        log.info("Dumping results...");

        Duration totalElapsedTime = Duration.ZERO;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < receivedMessages.size(); ++i) {
            BaseMessage message = deserialize(receivedMessages.get(i));
            message.setRecvTimestamp(receivedMessagesTs.get(i));

            Duration elapsedTime = Duration.between(message.getSendTimestamp(), message.getRecvTimestamp());
            totalElapsedTime = totalElapsedTime.plus(elapsedTime);

            sb.append(String.format(
                "Msg-%d: sendTs %s, recvTs %s -> elapsedTime %f ms, %d ns\n",
                i + 1,
                LocalDateTime.ofInstant(message.getSendTimestamp(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(message.getRecvTimestamp(), ZoneId.systemDefault()),
                elapsedTime.toNanos() / 1_000_000.0f,
                elapsedTime.toNanos()
            ));
        }

        write("Report Result:\n", StandardOpenOption.TRUNCATE_EXISTING);

        String result = (((1.0f * totalElapsedTime.toNanos()) / receivedMessages.size()) / 1_000_000) + " ms";
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

    private void reset() {
        log.info("Resetting listener.");

        receivedMessages.clear();
        receivedMessagesTs.clear();
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
}
