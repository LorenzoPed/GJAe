package com.laundry.app.service;

import com.laundry.app.dto.Status;
import com.laundry.app.model.Machine;
import com.laundry.app.model.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight health/status service for the laundry backend.
 */
@Service
public class LaundryService {

    /**
     * Return a simple status DTO indicating the service is running.
     *
     * @return status information
     */
    public Status getStatus() {
        return new Status("laundry-backend", "running");
    }
}
