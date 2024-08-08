//package com.demo.kafka.service;
//
//import com.demo.kafka.constants.FileConstant;
//import com.example.springbootkafkaproducer.data.BaseMessage;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.nio.file.Files;
//import java.nio.file.OpenOption;
//import java.nio.file.StandardOpenOption;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Queue;
//
//@Service
//@Slf4j
//public class QueueListenerService {
//
//    private int expectedItemCount = 100_000;
//    private File file;
//    private final List<byte[]> receivedMessages = new ArrayList<>();
//    private final KafkaPollingService kafkaPollingService;
//
//    public QueueListenerService(KafkaPollingService kafkaPollingService) {
//        this.kafkaPollingService = kafkaPollingService;
//        generateFile();
//        startListening();
//    }
//
//    private void startListening() {
//        Runnable listenerTask = () -> {
//            Queue<byte[]> queue = kafkaPollingService.getRecordQueue();
//            while (true) {
//                if (!queue.isEmpty()) {
//                    byte[] record = queue.poll();
//                    processRecord(record);
//                }
//            }
//        };
//
//        Thread pollingThread = new Thread(listenerTask);
//        pollingThread.setDaemon(true);
//        pollingThread.start();
//    }
//
//    private void processRecord(byte[] record) {
//        receivedMessages.add(record);
//
//        if (receivedMessages.size() == expectedItemCount) {
//            log.info("Received {} items, dumping reports...", expectedItemCount);
//
//            try {
//                dumpReport();
//            } catch (Exception e) {
//                log.error("Exception on report dump.", e);
//            }
//        }
//    }
//
//
//    public void setExpectedItemCount(final int value) {
//        this.expectedItemCount = value;
//        reset();
//    }
//
//    private BaseMessage deserialize(byte[] bytes) {
//        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//             ObjectInputStream ois = new ObjectInputStream(bis)) {
//            return (BaseMessage) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            log.error("Exception.", e);
//            return null;
//        }
//    }
//
//    public String dumpReport() {
//        write("Report Result:\n", StandardOpenOption.TRUNCATE_EXISTING);
//
//        long totalElapsedTime = 0;
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < receivedMessages.size(); ++i) {
//            BaseMessage message = deserialize(receivedMessages.get(i));
//            message.setRecvTimestamp(receivedMessagesTs.get(i));
//
//            long elapsedTime = ChronoUnit.MILLIS.between(message.getSendTimestamp(), message.getRecvTimestamp());
//            totalElapsedTime += elapsedTime;
//
//            sb.append(String.format(
//                    "Msg-%d: sendTs %s, recvTs %s -> elapsedTime %d ms\n",
//                    i + 1,
//                    message.getSendTimestamp().toString(),
//                    message.getRecvTimestamp().toString(),
//                    elapsedTime));
//
//        }
//        String result = ((1.0f * totalElapsedTime) / receivedMessages.size()) + " ms";
//        sb.append("AVERAGE: ").append(result).append("\n");
//        write(sb.toString(), StandardOpenOption.APPEND);
//
//        reset();
//        return result;
//    }
//
//    private void write(final String data, final OpenOption openOption) {
//        try {
//            Files.write(file.toPath(), data.getBytes(), StandardOpenOption.WRITE, openOption);
//        } catch (Exception e) {
//            log.error("Write failed.", e);
//        }
//    }
//
//    private void reset() {
//        log.info("Resetting listener.");
//
//        receivedMessages.clear();
//        receivedMessagesTs.clear();
//    }
//
//    private void generateFile() {
//        try {
//            file = new File(FileConstant.OUTPUT_FILE_PATH);
//
//            if (file.createNewFile()) {
//                log.info("Created file {}", file.getPath());
//            } else {
//                log.error("File creation failed for {}", FileConstant.OUTPUT_FILE_PATH);
//            }
//        } catch (IOException e) {
//            log.error("Exception.", e);
//        }
//    }
//}
