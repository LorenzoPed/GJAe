package com.laundry.app.laundry_backend.service;

import com.laundry.app.dto.Status;
import com.laundry.app.service.LaundryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LaundryService.
 */
class LaundryServiceTest {

    @Test
    void getStatus_returnsExpectedValues() {
        LaundryService svc = new LaundryService();
        Status s = svc.getStatus();

        assertNotNull(s);
        assertEquals("laundry-backend", s.name());
        assertEquals("running", s.status());
    }
}
