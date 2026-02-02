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

## Notes

The external API sometimes uses open/close for deal times and sometimes uses start/end. The code handles both cases.

If a deal does not have its own time window it uses the restaurant hours instead.

The restarantSuburb typo in the response is intentional because it matches the spec.
