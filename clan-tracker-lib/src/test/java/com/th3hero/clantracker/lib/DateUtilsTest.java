package com.th3hero.clantracker.lib;

import com.th3hero.clantracker.lib.utils.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DateUtilsTest {
    @Test
    void toDate_convertsLocalDateTimeToDate() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 5, 5, 0, 0);
        Date date = DateUtils.toDate(dateTime);
        assertThat(date).isNotNull();
        assertThat(date.getTime()).isEqualTo(1714881600000L);
    }

    @Test
    void fromTimestamp_convertsTimestampToLocalDateTime() {
        long timestamp = 1723608000L;
        LocalDateTime dateTime = DateUtils.fromTimestamp(timestamp);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2024, 8, 14, 0, 0));
    }

    @Test
    void fromDateTime_parsesDateTimeStringToLocalDateTime() {
        String dateTimeString = "1/1/2024 12:00:00";
        LocalDateTime dateTime = DateUtils.fromDateTime(dateTimeString);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0, 0));
    }

    @Test
    void fromDateString_parsesDateStringToLocalDateTimeAtStartOfDay() {
        String dateString = "1/1/2024";
        LocalDateTime dateTime = DateUtils.fromDateString(dateString);
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
    }

    @Test
    void fromDateTime_handlesInvalidDateTimeString() {
        assertThatExceptionOfType(DateTimeParseException.class)
            .isThrownBy(() -> DateUtils.fromDateTime("invalid date"));
    }

    @Test
    void fromDateString_handlesInvalidDateString() {
        assertThatExceptionOfType(DateTimeParseException.class)
            .isThrownBy(() -> DateUtils.fromDateString("invalid date"));
    }

}
