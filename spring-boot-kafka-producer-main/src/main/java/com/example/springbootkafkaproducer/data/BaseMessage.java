package com.example.springbootkafkaproducer.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;

@Getter
@Setter
public class BaseMessage implements Serializable {
    private final Random random = new Random();
    private byte[] content;
    private LocalDateTime sendTimestamp;
    private LocalDateTime recvTimestamp;

    public BaseMessage(final int sizeInKB) {
        this.content = new byte[1024 * sizeInKB];
        random.nextBytes(content);
        this.sendTimestamp = LocalDateTime.now();
    }
}
