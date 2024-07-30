package com.demo.kafka.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class KafkaConsumerListener {

	private final long LIMIT = 100_000;
	private int count = 0;

	private boolean isFirst = true;
	private Instant startTime;

	@KafkaListener(
			topics = "${spring.kafka.topic.name}",
			groupId = "${spring.kafka.group-id}")
	public void listen(String message) {
		count++;
		if (isFirst) {
			isFirst = false;
			startTime = Instant.now();
			System.out.println("First processed time: " + Instant.now());
		}
//		System.out.printf("consumed in thread: %s%n", Thread.currentThread().getName());
		if (count == LIMIT) {
			Duration between = Duration.between(startTime, Instant.now());
			System.out.printf("Consume millis: %d, nanos: %d%n", between.toMillis(), between.toNanos());
			count = 0;
			isFirst = true;
		}
	}
}
