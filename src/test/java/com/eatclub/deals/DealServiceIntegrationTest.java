package com.eatclub.deals;

import com.eatclub.deals.dto.DealsListResponse;
import com.eatclub.deals.dto.PeakTimeResponse;
import com.eatclub.deals.service.DealService;
import com.eatclub.deals.util.TimeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Deal Service.
 * Tests the main functionality against the real external API.
 */
@SpringBootTest
class DealServiceIntegrationTest {

    @Autowired
    private DealService dealService;

    @Test
    void testGetActiveDeals_At3PM() {
        DealsListResponse response = dealService.getActiveDeals("3:00pm");
        
        assertNotNull(response);
        assertNotNull(response.getDeals());
        
        System.out.println("Deals at 3:00pm: " + response.getDeals().size());
        response.getDeals().forEach(deal -> 
            System.out.println("  - " + deal.getRestaurantName() + ": " + deal.getDiscount() + "% off")
        );
        
        assertTrue(response.getDeals().size() > 0, "Should have active deals at 3pm");
    }

    @Test
    void testGetActiveDeals_At6PM() {
        DealsListResponse response = dealService.getActiveDeals("6:00pm");
        
        assertNotNull(response);
        assertNotNull(response.getDeals());
        
        System.out.println("Deals at 6:00pm: " + response.getDeals().size());
        response.getDeals().forEach(deal -> 
            System.out.println("  - " + deal.getRestaurantName() + ": " + deal.getDiscount() + "% off")
        );
        
        assertTrue(response.getDeals().size() > 0, "Should have active deals at 6pm");
    }

    @Test
    void testGetActiveDeals_At9PM() {
        DealsListResponse response = dealService.getActiveDeals("9:00pm");
        
        assertNotNull(response);
        assertNotNull(response.getDeals());
        
        System.out.println("Deals at 9:00pm: " + response.getDeals().size());
        response.getDeals().forEach(deal -> 
            System.out.println("  - " + deal.getRestaurantName() + ": " + deal.getDiscount() + "% off")
        );
    }

    @Test
    void testGetPeakTime() {
        PeakTimeResponse response = dealService.getPeakTime();
        
        assertNotNull(response);
        assertNotNull(response.getPeakTimeStart());
        assertNotNull(response.getPeakTimeEnd());
        
        System.out.println("Peak time: " + response.getPeakTimeStart() + " to " + response.getPeakTimeEnd());
        
        // Verify the times can be parsed
        assertDoesNotThrow(() -> TimeUtils.parseTime(response.getPeakTimeStart()));
        assertDoesNotThrow(() -> TimeUtils.parseTime(response.getPeakTimeEnd()));
    }

    @Test
    void testTimeUtils_ParseTime() {
        // Test 12-hour format
        assertEquals(LocalTime.of(15, 0), TimeUtils.parseTime("3:00pm"));
        assertEquals(LocalTime.of(9, 30), TimeUtils.parseTime("9:30am"));
        assertEquals(LocalTime.of(12, 0), TimeUtils.parseTime("12:00pm"));
        assertEquals(LocalTime.of(0, 0), TimeUtils.parseTime("12:00am"));
        
        // Test with uppercase
        assertEquals(LocalTime.of(15, 0), TimeUtils.parseTime("3:00PM"));
    }

    @Test
    void testTimeUtils_IsTimeWithinRange() {
        LocalTime time = LocalTime.of(15, 0); // 3pm
        LocalTime start = LocalTime.of(12, 0); // 12pm
        LocalTime end = LocalTime.of(21, 0); // 9pm
        
        assertTrue(TimeUtils.isTimeWithinRange(time, start, end));
        
        // Test boundary - should be inclusive
        assertTrue(TimeUtils.isTimeWithinRange(start, start, end));
        assertTrue(TimeUtils.isTimeWithinRange(end, start, end));
        
        // Test outside range
        LocalTime before = LocalTime.of(11, 0);
        LocalTime after = LocalTime.of(22, 0);
        assertFalse(TimeUtils.isTimeWithinRange(before, start, end));
        assertFalse(TimeUtils.isTimeWithinRange(after, start, end));
    }
}