package com.th3hero.clantracker.app.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class DateUtils {

    public static final ZoneId ZONE_ID = ZoneId.of("America/New_York");

    /**
     * Converts a LocalDateTime to a Date.
     *
     * @param dateTime The LocalDateTime to convert.
     * @return The converted Date.
     */
    public static Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZONE_ID).toInstant());
    }

    /**
     * Converts a timestamp to a LocalDateTime.
     *
     * @param timestamp The timestamp to convert, in seconds since the epoch.
     * @return The converted LocalDateTime in the specified ZoneId.
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZONE_ID);
    }

    /**
     * Parses a date-time string to a LocalDateTime.
     *
     * @param date The date-time string to parse, in the format "M/d/yyyy H:m:s".
     * @return The parsed LocalDateTime.
     */
    public static LocalDateTime fromDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:m:s");
        return LocalDateTime.parse(date, formatter);
    }

    /**
     * Parses a date string to a LocalDateTime at the start of the day.
     *
     * @param date The date string to parse, in the format "M/d/yyyy".
     * @return The parsed LocalDateTime at the start of the day.
     */
    public static LocalDateTime fromDateString(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        return LocalDate.parse(date, formatter).atStartOfDay();
    }
}
