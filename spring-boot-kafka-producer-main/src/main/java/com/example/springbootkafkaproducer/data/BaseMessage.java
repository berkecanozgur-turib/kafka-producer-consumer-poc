package com.example.springbootkafkaproducer.data;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;

public class BaseMessage implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private byte[] content;
    private LocalDateTime timestamp;

    public BaseMessage(LocalDateTime ldt) {
        this.content = new byte[1024 * 4];
        Random random = new Random();
        random.nextBytes(content);
        this.timestamp = ldt;
    }

    public BaseMessage(int sizeInKB) {
        synchronized (this) {
            this.content = new byte[1024 * sizeInKB];
            Random random = new Random();
            random.nextBytes(content);
            this.timestamp = LocalDateTime.now();
        }
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
