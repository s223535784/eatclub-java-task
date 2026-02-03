# EatClub Restaurant Deals API

A REST API built for the EatClub technical challenge. It fetches restaurant deals from an external source and lets you query which deals are active at any given time.

## What it does

Task 1 - Find all active deals at a specific time
Task 2 - Calculate when the most deals are available (peak time)
Task 3 - Database schema design (see docs folder)

## Tech used

Java 17
Spring Boot 3.2
Maven
Lombok

## Running locally

Make sure you have Java 17 installed. Check with java -version command.

Then run mvn spring-boot:run and the API will start at http://localhost:8080

### Using Helper Scripts (Windows)

The project includes batch files for easy start/stop

Start the application (kills existing process, cleans, and starts)

```
start.bat
```

Stop the application

```
stop.bat
```

These scripts automatically handle port 8080 conflicts so you do not need to manually kill processes.

I have configured Swagger UI to show the running APIs : - http://localhost:8080/swagger-ui/index.html

## API Endpoints

Get active deals at a specific time

GET /api/deals?timeOfDay=3:00pm

Returns all deals that are active at that time. You can use formats like 3:00pm, 6:00pm, or 15:00.

Get peak time

GET /api/deals/peak-time

Returns the time window when the most deals are available.

## Testing

The challenge asked to test with these times

timeOfDay=3:00pm
timeOfDay=6:00pm
timeOfDay=9:00pm

You can test in browser by opening these URLs

http://localhost:8080/api/deals?timeOfDay=3:00pm
http://localhost:8080/api/deals?timeOfDay=6:00pm
http://localhost:8080/api/deals?timeOfDay=9:00pm
http://localhost:8080/api/deals/peak-time

## Project structure

controller folder has REST endpoints
service folder has business logic
model folder has data classes from external API
dto folder has response objects
util folder has time parsing helpers
exception folder has error handling

## Database Schema for Task 3

I chose PostgreSQL for this because the data has clear relationships where restaurants have many deals. It also has good support for UUID fields which match the API objectIds and the TIME type works well for storing open and close hours.

The schema diagram is in docs/database-schema.mermaid

Main tables are restaurant, deal, cuisine, and restaurant_cuisine which links restaurants to their cuisines.

## Performance Optimizations

### 1. Simple In-Memory Caching

The API caches external API responses to avoid repeated network calls. This is implemented in DealService.java using a simple approach

```java
// Cache variables
private List<Restaurant> cachedData = null;
private long cacheTimestamp = 0;
private static final long CACHE_TTL_MS = 60000; // 1 minute

// In fetchRestaurantData method
long currentTime = System.currentTimeMillis();
if (cachedData != null && (currentTime - cacheTimestamp) < CACHE_TTL_MS) {
    return cachedData; // Return cached data
}
// Otherwise fetch fresh data and update cache
```

How it works

- First request fetches from external API (~180ms) and stores in cache
- Subsequent requests within 1 minute return cached data (~1ms)
- After 1 minute the cache expires and fresh data is fetched

Explanation: This is a simple TTL (Time To Live) cache. We store the data and timestamp when it was fetched. On each request we check if the cache is still valid by comparing current time with the stored timestamp. If the difference is less than TTL we return cached data otherwise we fetch fresh data.

### 2. Response Compression

Enabled GZIP compression for JSON responses in application.properties

```properties
server.compression.enabled=true
server.compression.mime-types=application/json
server.compression.min-response-size=1024
```

This compresses responses larger than 1KB which reduces network transfer time.

### 3. Response Time Logging

Each API endpoint logs its response time in milliseconds

```java
long startTime = System.currentTimeMillis();
// ... API logic ...
long responseTime = System.currentTimeMillis() - startTime;
log.info("GET /api/deals - Response time: {} ms", responseTime);
```

Sample log output

```
INFO  GET /api/deals?timeOfDay=3:00pm - Response time: 80 ms
INFO  GET /api/deals/peak-time - Response time: 31 ms
```

### Performance Results

| Scenario                 | Response Time |
| ------------------------ | ------------- |
| First request (no cache) | ~600ms        |
| Cached request           | ~5-30ms       |
| External API alone       | ~180ms        |

## Notes

The external API sometimes uses open/close for deal times and sometimes uses start/end. The code handles both cases.

If a deal does not have its own time window it uses the restaurant hours instead.
