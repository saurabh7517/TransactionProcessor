package org.payment.processor.controller;

import org.payment.processor.dto.MsgDto;
import org.payment.processor.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {
    private static final Logger log  = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/msg")
    public ResponseEntity<String> receiveMessage(@RequestBody MsgDto msgDto) throws Exception {
        log.info("Received message with Id : {}", msgDto.getMsgId());
        messageService.processMessage(msgDto);
        return ResponseEntity.ok("New message received");
    }

}
