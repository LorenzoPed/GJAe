package com.laundry.app.controller;

import com.laundry.app.dto.Status;
import com.laundry.app.service.LaundryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    private final LaundryService service;

    public SystemController(LaundryService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/status")
    public Status status() {
        return service.getStatus();
    }
}
