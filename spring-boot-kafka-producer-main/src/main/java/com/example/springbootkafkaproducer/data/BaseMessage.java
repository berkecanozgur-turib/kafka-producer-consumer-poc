package com.example.springbootkafkaproducer.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.Random;

public class BaseMessage implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private byte[] content;
    private Instant timestamp;

    public BaseMessage() {
        this.content = new byte[1024 * 4];
        Random random = new Random();
        random.nextBytes(content);
        this.timestamp = Instant.now();
    }

    public BaseMessage(int sizeInKB) {
        this.content = new byte[1024 * sizeInKB];
        Random random = new Random();
        random.nextBytes(content);
        this.timestamp = Instant.now();
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}
