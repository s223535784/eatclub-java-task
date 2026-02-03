# Performance Improvements

This document explains the optimizations done to improve API response times.

## The Problem

Every time someone called our API, I Ire making a request to the external EatClub API. This external API takes around 100-200ms to respond. So even for simple queries, users had to wait for this external call every single time.

## What I Did

I added two simple optimizations - caching and compression.

### 1. Simple In-Memory Caching

Before the change, every API request would call the external API. Now I store the response in memory and reuse it for 1 minute.

How it works in simple terms:

- When first request comes in, I fetch data from external API and save it in a variable
- I also save the current time when I fetched it
- When next request comes, I check if 1 minute has passed
- If less than 1 minute, I return the saved data without calling external API
- If more than 1 minute, I fetch fresh data and save it again

The code is just a few lines:

---

private List<Restaurant> cachedData = null;
private long cacheTimestamp = 0;
private static final long CACHE_TTL_MS = 60000;

if (cachedData != null && (currentTime - cacheTimestamp) < CACHE_TTL_MS) {
return cachedData;
}

---

Why 1 minute? Restaurant data does not change every second. 1 minute is a good balance b/w fresh data and performance.

### 2. Response Compression

I enabled GZIP compression for JSON responses. This means the server compresses the response before sending it over the network. The browser or client automatically decompresses it.

Added these 3 lines to application.properties:

---

server.compression.enabled=true
server.compression.mime-types=application/json
server.compression.min-response-size=1024

---

This helps when response size is large. Compressed data transfers faster over the network.

### 3. Response Time Logging

I also added logging to track how long each request takes. This helps us monitor performance.

---

long startTime = System.currentTimeMillis();
// do the work
long responseTime = System.currentTimeMillis() - startTime;
log.info("Response time: " + responseTime + " ms");

---

## Before and After Comparison

### Before (No Caching)

Request 1: 600ms (calls external API)
Request 2: 600ms (calls external API again)
Request 3: 600ms (calls external API again)
Request 4: 600ms (calls external API again)

Every request was slow because I always called the external API.

### After (With Caching)

Request 1: 94ms (calls external API, saves to cache)
Request 2: 6ms (returns from cache)
Request 3: 5ms (returns from cache)
Request 4: 5ms (returns from cache)

First request is still slow because I need to fetch data. But all requests after that are very fast because I return cached data.

## Actual Test Results

I tested the APIs with curl and measured response times.

Fresh request (no cache): 94ms
Cached request: 5-6ms

This is about 16 times faster for cached requests.

## When Cache Expires

After 1 minute the cache expires. The next request will again take around 100ms to fetch fresh data. Then subsequent requests will be fast again for the next 1 minute.

## Trade-offs

Pros:

- Much faster response times for users
- Less load on external API
- Simple code that is easy to understand

Cons:

- Data might be up to 1 minute old
- Uses some memory to store cached data
- If server restarts, cache is lost

For this use case the trade-offs are acceptable because restaurant deal data does not change every second.

## How to Verify

Run the application and make multiple requests:

---

## curl http://localhost:8080/api/deals?timeOfDay=12:00

Check the logs. You will see:

- First request: "Fetching restaurant data from..." and "cache updated"
- Next requests: "Returning cached data (age: xxx ms)"
