package com.eatclub.deals.controller;

import com.eatclub.deals.dto.DealsListResponse;
import com.eatclub.deals.dto.PeakTimeResponse;
import com.eatclub.deals.service.DealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller for deals API endpoints
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealController {

    private final DealService dealService; // injected by Spring via @RequiredArgsConstructor

    // GET /api/deals?timeOfDay=3:00pm - returns active deals at given time
    @GetMapping
    public ResponseEntity<DealsListResponse> getActiveDeals(
            @RequestParam String timeOfDay) {

        long startTime = System.currentTimeMillis();
        log.info("Received request for active deals at: {}", timeOfDay);

        DealsListResponse response = dealService.getActiveDeals(timeOfDay);

        // Log response time for monitoring
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("GET /api/deals?timeOfDay={} - Response time: {} ms", timeOfDay, responseTime);

        return ResponseEntity.ok(response);
    }

    // GET /api/deals/peak-time - returns time window with most deals
    @GetMapping("/peak-time")
    public ResponseEntity<PeakTimeResponse> getPeakTime() {

        long startTime = System.currentTimeMillis();
        log.info("Received request for peak time calculation");

        PeakTimeResponse response = dealService.getPeakTime();

        // Log response time for monitoring
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("GET /api/deals/peak-time - Response time: {} ms", responseTime);

        return ResponseEntity.ok(response);
    }
}