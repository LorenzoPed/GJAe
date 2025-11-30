package com.laundry.app.service;

import com.laundry.app.dto.Status;
import com.laundry.app.model.Machine;
import com.laundry.app.model.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LaundryService {

    // -------- Status --------
    public Status getStatus() {
        return new Status("laundry-backend", "running");
    }
}
