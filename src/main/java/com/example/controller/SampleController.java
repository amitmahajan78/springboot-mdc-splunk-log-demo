package com.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SampleController {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleController.class);

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        logger.info("Received request for /hello endpoint");
        String response = "Hello, World!";
        logger.debug("Sending response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/error")
    public ResponseEntity<String> error() {
        logger.error("Received request for /error endpoint - throwing sample error");
        throw new RuntimeException("This is a sample error");
    }
} 