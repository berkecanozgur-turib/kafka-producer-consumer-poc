package com.example.springbootkafkaproducer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {

	@Autowired
	private KafkaTemplate<String, byte[]> kafkaTemplate;

	@Value("${spring.kafka.topic.name}")
	private String topicName;

	public void sendMessage(byte[] msg) {
		kafkaTemplate.send(topicName, msg);
	}
}
