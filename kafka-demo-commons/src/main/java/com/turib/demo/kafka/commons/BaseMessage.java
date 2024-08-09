package com.turib.demo.kafka.commons;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Random;

@Getter
@Setter
public class BaseMessage implements Serializable {
    private final Random random = new Random();
    private byte[] content;
    private Instant sendTimestamp;
    private Instant recvTimestamp;

    public BaseMessage(final int sizeInKB) {
        this.content = new byte[1024 * sizeInKB];
        random.nextBytes(content);
        this.sendTimestamp = Instant.now();
    }
}
