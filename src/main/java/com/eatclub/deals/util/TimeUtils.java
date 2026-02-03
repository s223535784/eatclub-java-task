package com.eatclub.deals.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

// helper methods for time parsing and comparison used across the app
public class TimeUtils {

    // h:mma -> parses 3:00PM, H:mm -> parses 15:00
    private static final DateTimeFormatter TWELVE_HOUR_FORMATTER = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);
    private static final DateTimeFormatter TWENTY_FOUR_HOUR_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    // Parses 3:00pm or 15:00 into LocalTime
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Time string cannot be null or empty");
        }

        String normalized = timeStr.trim();
        String lowerCase = normalized.toLowerCase();

        try {
            if (lowerCase.contains("am") || lowerCase.contains("pm")) {
                return LocalTime.parse(normalized.toUpperCase(), TWELVE_HOUR_FORMATTER); // needs uppercase for AM/PM
            }
            return LocalTime.parse(normalized, TWENTY_FOUR_HOUR_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr +
                    ". Expected format: 'H:MMam/pm' (e.g., '3:00pm') or 'HH:MM' (e.g., '15:00')");
        }
    }

    public static boolean isTimeWithinRange(LocalTime queryTime, LocalTime startTime, LocalTime endTime) {
        if (!startTime.isAfter(endTime)) {
            return !queryTime.isBefore(startTime) && !queryTime.isAfter(endTime);
        }
        // crosses midnight (e.g. 10pm to 2am)
        return !queryTime.isBefore(startTime) || !queryTime.isAfter(endTime);
    }

    public static String formatTo12Hour(LocalTime time) {
        return time.format(TWELVE_HOUR_FORMATTER);
    }

    // used in peak time calculation to work with minute-based arrays
    public static int toMinutesSinceMidnight(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    public static LocalTime fromMinutesSinceMidnight(int minutes) {
        return LocalTime.of(minutes / 60, minutes % 60);
    }
}