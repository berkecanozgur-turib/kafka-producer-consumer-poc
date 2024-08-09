package com.turib.demo.kafka.producer.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaAddress;

	@Value("${spring.kafka.acks}")
	private String acks;

	@Value("${spring.kafka.buffer-memory}")
	private int bufferMemory;

	@Value("${spring.kafka.retries}")
	private int retries;

	@Value("${spring.kafka.batch-size}")
	private int batchSize;

	@Value("${spring.kafka.linger-ms}")
	private int lingerMs;

	@Value("${spring.kafka.compression-type}")
	private String compressionType;

	@Value("${spring.kafka.max-block-ms}")
	private int maxBlockMs;
	
	@Bean
	public ProducerFactory<String, byte[]> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
		configProps.put(ProducerConfig.ACKS_CONFIG, acks);
		configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
		configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
		configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
		configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
		configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
		configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMs);

		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, byte[]> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}