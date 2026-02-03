package com.eatclub.deals.service;

import com.eatclub.deals.dto.DealResponse;
import com.eatclub.deals.dto.DealsListResponse;
import com.eatclub.deals.dto.PeakTimeResponse;
import com.eatclub.deals.exception.ExternalApiException;
import com.eatclub.deals.exception.InvalidTimeFormatException;
import com.eatclub.deals.model.Deal;
import com.eatclub.deals.model.Restaurant;
import com.eatclub.deals.model.RestaurantDataResponse;
import com.eatclub.deals.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class containing business logic for restaurant deals.
 * Handles fetching data from external API and processing deal queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {

    private final RestTemplate restTemplate;

    @Value("${eatclub.api.url:https://eccdn.com.au/misc/challengedata.json}")
    private String apiUrl;

    // Simple in-memory cache to avoid repeated API calls
    // Cache stores the response and timestamp of last fetch
    private List<Restaurant> cachedData = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 60000; // Cache for 1 minute (60000 ms)

    /**
     * Fetches restaurant data from the external API with simple caching.
     *
     * How caching works:
     * 1. Check if cache exists and is not expired (within TTL)
     * 2. If valid cache exists, return cached data (fast - ~1ms)
     * 3. If no cache or expired, fetch from API and update cache
     *
     * @return list of restaurants with their deals
     * @throws ExternalApiException if the API call fails
     */
    public List<Restaurant> fetchRestaurantData() {
        long currentTime = System.currentTimeMillis();

        // Check if we have valid cached data
        if (cachedData != null && (currentTime - cacheTimestamp) < CACHE_TTL_MS) {
            log.info("Returning cached data (age: {} ms)", currentTime - cacheTimestamp);
            return cachedData;
        }

        // Cache miss or expired - fetch fresh data
        try {
            log.info("Fetching restaurant data from: {}", apiUrl);
            RestaurantDataResponse response = restTemplate.getForObject(apiUrl, RestaurantDataResponse.class);

            if (response == null || response.getRestaurants() == null) {
                throw new ExternalApiException("No data received from external API");
            }

            // Update cache with fresh data
            cachedData = response.getRestaurants();
            cacheTimestamp = currentTime;

            log.info("Successfully fetched {} restaurants (cache updated)", response.getRestaurants().size());
            return cachedData;
        } catch (RestClientException e) {
            log.error("Failed to fetch restaurant data: {}", e.getMessage());
            throw new ExternalApiException("Failed to fetch restaurant data from external API", e);
        }
    }

    /**
     * Task 1: Get all active deals at a specified time of day.
     * 
     * A deal is considered active if:
     * 1. The restaurant is open at the given time
     * 2. The deal's time window (if specified) includes the given time
     * 
     * @param timeOfDay the time to query (e.g., "3:00pm", "15:00")
     * @return response containing list of active deals
     */
    public DealsListResponse getActiveDeals(String timeOfDay) {
        LocalTime queryTime;
        try {
            queryTime = TimeUtils.parseTime(timeOfDay);
        } catch (IllegalArgumentException e) {
            throw new InvalidTimeFormatException(e.getMessage());
        }

        log.info("Querying for active deals at: {} (parsed as {})", timeOfDay, queryTime);

        List<Restaurant> restaurants = fetchRestaurantData();
        List<DealResponse> activeDeals = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            // Parse restaurant operating hours
            LocalTime restaurantOpen = TimeUtils.parseTime(restaurant.getOpen());
            LocalTime restaurantClose = TimeUtils.parseTime(restaurant.getClose());

            // Check if restaurant is open at the query time
            if (!TimeUtils.isTimeWithinRange(queryTime, restaurantOpen, restaurantClose)) {
                log.debug("Restaurant {} is closed at {}", restaurant.getName(), timeOfDay);
                continue;
            }

            // Check each deal
            if (restaurant.getDeals() != null) {
                for (Deal deal : restaurant.getDeals()) {
                    if (isDealActive(deal, queryTime, restaurantOpen, restaurantClose)) {
                        activeDeals.add(buildDealResponse(restaurant, deal));
                    }
                }
            }
        }

        log.info("Found {} active deals at {}", activeDeals.size(), timeOfDay);
        return DealsListResponse.builder().deals(activeDeals).build();
    }

    /**
     * Checks if a deal is active at the specified time.
     * 
     * @param deal the deal to check
     * @param queryTime the time to check against
     * @param restaurantOpen restaurant's opening time (fallback if deal has no time)
     * @param restaurantClose restaurant's closing time (fallback if deal has no time)
     * @return true if the deal is active at queryTime
     */
    private boolean isDealActive(Deal deal, LocalTime queryTime, 
                                  LocalTime restaurantOpen, LocalTime restaurantClose) {
        // Get deal's effective time window (falls back to restaurant hours if not specified)
        String dealOpenStr = deal.getEffectiveOpen();
        String dealCloseStr = deal.getEffectiveClose();

        LocalTime dealOpen = (dealOpenStr != null) ? TimeUtils.parseTime(dealOpenStr) : restaurantOpen;
        LocalTime dealClose = (dealCloseStr != null) ? TimeUtils.parseTime(dealCloseStr) : restaurantClose;

        return TimeUtils.isTimeWithinRange(queryTime, dealOpen, dealClose);
    }

    /**
     * Builds a DealResponse DTO from restaurant and deal data.
     */
    private DealResponse buildDealResponse(Restaurant restaurant, Deal deal) {
        return DealResponse.builder()
                .restaurantObjectId(restaurant.getObjectId())
                .restaurantName(restaurant.getName())
                .restaurantAddress1(restaurant.getAddress1())
                .restarantSuburb(restaurant.getSuburb()) // Note: intentional misspelling per spec
                .restaurantOpen(restaurant.getOpen())
                .restaurantClose(restaurant.getClose())
                .dealObjectId(deal.getObjectId())
                .discount(deal.getDiscount())
                .dineIn(deal.getDineIn())
                .lightning(deal.getLightning())
                .qtyLeft(deal.getQtyLeft())
                .build();
    }

    /**
     * Task 2: Calculate the peak time window when most deals are available.
     * 
     * Algorithm:
     * 1. Discretize time into minute intervals across the day
     * 2. For each deal, mark the minutes when it's active
     * 3. Find the contiguous window with the maximum number of active deals
     * 
     * @return the peak time window (start and end times)
     */
    public PeakTimeResponse getPeakTime() {
        List<Restaurant> restaurants = fetchRestaurantData();
        
        // Array to count active deals for each minute of the day (0-1439)
        int[] dealCountByMinute = new int[24 * 60];

        // Count active deals for each minute
        for (Restaurant restaurant : restaurants) {
            LocalTime restaurantOpen = TimeUtils.parseTime(restaurant.getOpen());
            LocalTime restaurantClose = TimeUtils.parseTime(restaurant.getClose());

            if (restaurant.getDeals() != null) {
                for (Deal deal : restaurant.getDeals()) {
                    String dealOpenStr = deal.getEffectiveOpen();
                    String dealCloseStr = deal.getEffectiveClose();

                    // Use deal-specific times or fall back to restaurant hours
                    LocalTime dealOpen = (dealOpenStr != null) ? 
                            TimeUtils.parseTime(dealOpenStr) : restaurantOpen;
                    LocalTime dealClose = (dealCloseStr != null) ? 
                            TimeUtils.parseTime(dealCloseStr) : restaurantClose;

                    // Also constrain to restaurant operating hours
                    int startMinute = Math.max(
                            TimeUtils.toMinutesSinceMidnight(dealOpen),
                            TimeUtils.toMinutesSinceMidnight(restaurantOpen)
                    );
                    int endMinute = Math.min(
                            TimeUtils.toMinutesSinceMidnight(dealClose),
                            TimeUtils.toMinutesSinceMidnight(restaurantClose)
                    );

                    // Increment count for each minute in the deal's active window
                    for (int minute = startMinute; minute <= endMinute; minute++) {
                        dealCountByMinute[minute]++;
                    }
                }
            }
        }

        // Find the maximum deal count
        int maxDeals = 0;
        for (int count : dealCountByMinute) {
            maxDeals = Math.max(maxDeals, count);
        }

        // Find the contiguous window with max deals
        int peakStart = -1;
        int peakEnd = -1;
        
        for (int i = 0; i < dealCountByMinute.length; i++) {
            if (dealCountByMinute[i] == maxDeals) {
                if (peakStart == -1) {
                    peakStart = i;
                }
                peakEnd = i;
            } else if (peakStart != -1 && peakEnd != -1) {
                // Found the end of a peak window
                // Continue to find if there's a longer one
                // (This simple approach takes the first contiguous peak window)
                break;
            }
        }

        LocalTime startTime = TimeUtils.fromMinutesSinceMidnight(peakStart);
        LocalTime endTime = TimeUtils.fromMinutesSinceMidnight(peakEnd);

        log.info("Peak time window: {} to {} with {} active deals", 
                TimeUtils.formatTo12Hour(startTime), 
                TimeUtils.formatTo12Hour(endTime), 
                maxDeals);

        return PeakTimeResponse.builder()
                .peakTimeStart(TimeUtils.formatTo12Hour(startTime))
                .peakTimeEnd(TimeUtils.formatTo12Hour(endTime))
                .build();
    }
}