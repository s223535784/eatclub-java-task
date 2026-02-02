package com.eatclub.deals.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Utility class for parsing and manipulating time strings.
 * Handles both 12-hour format (e.g., "3:00pm") and 24-hour format (e.g., "15:00").
 */
public class TimeUtils {

    // Formatter for 12-hour time format with am/pm
    private static final DateTimeFormatter TWELVE_HOUR_FORMATTER = 
            DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
    
    // Formatter for 24-hour time format
    private static final DateTimeFormatter TWENTY_FOUR_HOUR_FORMATTER = 
            DateTimeFormatter.ofPattern("H:mm");

    /**
     * Parses a time string into LocalTime.
     * Supports formats: "3:00pm", "3:00PM", "15:00", "3:00"
     * 
     * @param timeStr the time string to parse
     * @return LocalTime representation
     * @throws IllegalArgumentException if the time format is invalid
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Time string cannot be null or empty");
        }

        String normalized = timeStr.trim();
        String lowerCase = normalized.toLowerCase();

        try {
            // Try 12-hour format first (e.g., "3:00pm")
            if (lowerCase.contains("am") || lowerCase.contains("pm")) {
                // Convert to uppercase for parsing (DateTimeFormatter expects AM/PM)
                return LocalTime.parse(normalized.toUpperCase(), TWELVE_HOUR_FORMATTER);
            }

            // Try 24-hour format (e.g., "15:00" or "3:00")
            return LocalTime.parse(normalized, TWENTY_FOUR_HOUR_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr +
                    ". Expected format: 'H:MMam/pm' (e.g., '3:00pm') or 'HH:MM' (e.g., '15:00')");
        }
    }

    /**
     * Checks if a given time falls within a time range (inclusive).
     * 
     * @param queryTime the time to check
     * @param startTime the start of the range
     * @param endTime the end of the range
     * @return true if queryTime is within [startTime, endTime]
     */
    public static boolean isTimeWithinRange(LocalTime queryTime, LocalTime startTime, LocalTime endTime) {
        // Handle the case where the range doesn't cross midnight
        if (!startTime.isAfter(endTime)) {
            return !queryTime.isBefore(startTime) && !queryTime.isAfter(endTime);
        }
        // Handle the case where the range crosses midnight (e.g., 10pm to 2am)
        return !queryTime.isBefore(startTime) || !queryTime.isAfter(endTime);
    }

    /**
     * Converts LocalTime to 12-hour format string (e.g., "3:00pm").
     * 
     * @param time the LocalTime to format
     * @return formatted time string
     */
    public static String formatTo12Hour(LocalTime time) {
        return time.format(TWELVE_HOUR_FORMATTER);
    }

    /**
     * Converts LocalTime to minutes since midnight for easier calculations.
     * 
     * @param time the LocalTime to convert
     * @return minutes since midnight
     */
    public static int toMinutesSinceMidnight(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    /**
     * Converts minutes since midnight back to LocalTime.
     * 
     * @param minutes minutes since midnight
     * @return LocalTime representation
     */
    public static LocalTime fromMinutesSinceMidnight(int minutes) {
        return LocalTime.of(minutes / 60, minutes % 60);
    }
}