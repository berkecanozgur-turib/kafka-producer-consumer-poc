package com.demo.kafka.controller;

import com.demo.kafka.listener.KafkaConsumerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerController {
    @Autowired KafkaConsumerListener kafkaConsumerListener;

    @GetMapping("/listen/itemCount={itemCount}")
    public ResponseEntity<Void> listen(@PathVariable(name = "itemCount") final int itemCount) {
        if (itemCount < 1) {
            return ResponseEntity.badRequest().build();
        }

        kafkaConsumerListener.setExpectedItemCount(itemCount);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/report")
    public ResponseEntity<Object> report() {
        return ResponseEntity.ok(kafkaConsumerListener.dumpReport());
    }
}
