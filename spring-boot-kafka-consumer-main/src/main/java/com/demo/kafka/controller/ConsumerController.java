package com.demo.kafka.controller;

import com.demo.kafka.service.KafkaPollingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerController {
    private final KafkaPollingService kafkaPollingService;

    public ConsumerController(KafkaPollingService queueListenerService) {
        this.kafkaPollingService = queueListenerService;
    }

    @GetMapping("/listen/itemCount={itemCount}")
    public ResponseEntity<Void> listen(@PathVariable(name = "itemCount") final int itemCount) {
        if (itemCount < 1) {
            return ResponseEntity.badRequest().build();
        }

        kafkaPollingService.setExpectedItemCount(itemCount);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/report")
    public ResponseEntity<Object> report() {
        return ResponseEntity.ok(kafkaPollingService.dumpReport());
    }
}
