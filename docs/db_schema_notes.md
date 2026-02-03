# EatClub Database Schema

## ER Diagram

Present in database-schema.mermaid file/ database-schema.png(backup file).

## DB Choice

I chose PostgreSQL for this because the data has clear relationships where restaurants have many deals.
It also has good support for UUID fields which match the API objectIds and the TIME type works well for storing open and close hours.

## Table Descriptions

### RESTAURANT

Stores restaurant information including name, address, and operating hours.

### CUISINE

Normalized lookup table for cuisine types (Indian, Asian, etc.).

### RESTAURANT_CUISINE

Junction table for many-to-many relationship between restaurants and cuisines.

### DEAL

Stores deal information linked to restaurants with optional time overrides.
