package com.eatclub.deals.controller;

import com.eatclub.deals.dto.DealsListResponse;
import com.eatclub.deals.dto.PeakTimeResponse;
import com.eatclub.deals.service.DealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for restaurant deals API.
 * Provides endpoints for querying active deals and calculating peak times.
 */
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealController {

    private final DealService dealService;

    /**
     * Task 1: Get all active deals at a specified time of day.
     * 
     * Example usage:
     *   GET /api/deals?timeOfDay=3:00pm
     *   GET /api/deals?timeOfDay=15:00
     * 
     * @param timeOfDay the time to query (e.g., "3:00pm", "6:00pm", "15:00")
     * @return JSON response containing list of active deals
     */
    @GetMapping
    public ResponseEntity<DealsListResponse> getActiveDeals(
            @RequestParam String timeOfDay) {

        long startTime = System.currentTimeMillis();
        log.info("Received request for active deals at: {}", timeOfDay);

        DealsListResponse response = dealService.getActiveDeals(timeOfDay);

        long responseTime = System.currentTimeMillis() - startTime;
        log.info("GET /api/deals?timeOfDay={} - Response time: {} ms", timeOfDay, responseTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Task 2: Get the peak time window when most deals are available.
     * 
     * Example usage:
     *   GET /api/deals/peak-time
     * 
     * @return JSON response containing peak time start and end
     */
    @GetMapping("/peak-time")
    public ResponseEntity<PeakTimeResponse> getPeakTime() {

        long startTime = System.currentTimeMillis();
        log.info("Received request for peak time calculation");

        PeakTimeResponse response = dealService.getPeakTime();

        long responseTime = System.currentTimeMillis() - startTime;
        log.info("GET /api/deals/peak-time - Response time: {} ms", responseTime);

        return ResponseEntity.ok(response);
    }
}