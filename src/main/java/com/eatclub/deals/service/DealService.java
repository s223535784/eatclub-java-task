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

@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {

    private final RestTemplate restTemplate;

    @Value("${eatclub.api.url:https://eccdn.com.au/misc/challengedata.json}")
    private String apiUrl; // loaded from application.properties

    // Cache to store API response and avoid repeated calls
    private List<Restaurant> cachedData = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 60000; // 1 minute

    // Fetches data from external API with caching
    public List<Restaurant> fetchRestaurantData() {
        long currentTime = System.currentTimeMillis();

        // Return cached data if still valid
        if (cachedData != null && (currentTime - cacheTimestamp) < CACHE_TTL_MS) {
            log.info("Returning cached data (age: {} ms)", currentTime - cacheTimestamp);
            return cachedData;
        }

        // Fetch fresh data from API
        try {
            log.info("Fetching restaurant data from: {}", apiUrl);
            RestaurantDataResponse response = restTemplate.getForObject(apiUrl, RestaurantDataResponse.class);

            if (response == null || response.getRestaurants() == null) {
                throw new ExternalApiException("No data received from external API");
            }

            // Save to cache
            cachedData = response.getRestaurants();
            cacheTimestamp = currentTime;

            log.info("Successfully fetched {} restaurants (cache updated)", response.getRestaurants().size());
            return cachedData;
        } catch (RestClientException e) {
            log.error("Failed to fetch restaurant data: {}", e.getMessage());
            throw new ExternalApiException("Failed to fetch restaurant data from external API", e);
        }
    }

    // Returns all deals active at the given time
    public DealsListResponse getActiveDeals(String timeOfDay) {
        // parse 3:00pm or 15:00 into LocalTime, throw 400 if invalid
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
            LocalTime restaurantOpen = TimeUtils.parseTime(restaurant.getOpen());
            LocalTime restaurantClose = TimeUtils.parseTime(restaurant.getClose());

            // skip if restaurant is closed
            if (!TimeUtils.isTimeWithinRange(queryTime, restaurantOpen, restaurantClose)) {
                log.debug("Restaurant {} is closed at {}", restaurant.getName(), timeOfDay);
                continue;
            }

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

    // Uses deal time if available, otherwise falls back to restaurant hours
    private boolean isDealActive(Deal deal, LocalTime queryTime,
            LocalTime restaurantOpen, LocalTime restaurantClose) {
        String dealOpenStr = deal.getEffectiveOpen();
        String dealCloseStr = deal.getEffectiveClose();

        LocalTime dealOpen = (dealOpenStr != null) ? TimeUtils.parseTime(dealOpenStr) : restaurantOpen;
        LocalTime dealClose = (dealCloseStr != null) ? TimeUtils.parseTime(dealCloseStr) : restaurantClose;

        return TimeUtils.isTimeWithinRange(queryTime, dealOpen, dealClose);
    }

    private DealResponse buildDealResponse(Restaurant restaurant, Deal deal) {
        return DealResponse.builder()
                .restaurantObjectId(restaurant.getObjectId())
                .restaurantName(restaurant.getName())
                .restaurantAddress1(restaurant.getAddress1())
                .restarantSuburb(restaurant.getSuburb()) // typo is intentional, matches the spec
                .restaurantOpen(restaurant.getOpen())
                .restaurantClose(restaurant.getClose())
                .dealObjectId(deal.getObjectId())
                .discount(deal.getDiscount())
                .dineIn(deal.getDineIn())
                .lightning(deal.getLightning())
                .qtyLeft(deal.getQtyLeft())
                .build();
    }

    // Finds the time window when most deals are active
    public PeakTimeResponse getPeakTime() {
        List<Restaurant> restaurants = fetchRestaurantData();

        // count deals for each minute of the day
        int[] dealCountByMinute = new int[24 * 60];

        for (Restaurant restaurant : restaurants) {
            LocalTime restaurantOpen = TimeUtils.parseTime(restaurant.getOpen());
            LocalTime restaurantClose = TimeUtils.parseTime(restaurant.getClose());

            if (restaurant.getDeals() != null) {
                for (Deal deal : restaurant.getDeals()) {
                    String dealOpenStr = deal.getEffectiveOpen();
                    String dealCloseStr = deal.getEffectiveClose();

                    LocalTime dealOpen = (dealOpenStr != null) ? TimeUtils.parseTime(dealOpenStr) : restaurantOpen;
                    LocalTime dealClose = (dealCloseStr != null) ? TimeUtils.parseTime(dealCloseStr) : restaurantClose;

                    // constrain to restaurant hours
                    int startMinute = Math.max(
                            TimeUtils.toMinutesSinceMidnight(dealOpen),
                            TimeUtils.toMinutesSinceMidnight(restaurantOpen));
                    int endMinute = Math.min(
                            TimeUtils.toMinutesSinceMidnight(dealClose),
                            TimeUtils.toMinutesSinceMidnight(restaurantClose));

                    for (int minute = startMinute; minute <= endMinute; minute++) {
                        dealCountByMinute[minute]++;
                    }
                }
            }
        }

        int maxDeals = 0;
        for (int count : dealCountByMinute) {
            maxDeals = Math.max(maxDeals, count);
        }

        // find the continuous window with max deals
        int peakStart = -1;
        int peakEnd = -1;

        for (int i = 0; i < dealCountByMinute.length; i++) {
            if (dealCountByMinute[i] == maxDeals) {
                if (peakStart == -1) {
                    peakStart = i;
                }
                peakEnd = i;
            } else if (peakStart != -1 && peakEnd != -1) {
                break; // first peak window found
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