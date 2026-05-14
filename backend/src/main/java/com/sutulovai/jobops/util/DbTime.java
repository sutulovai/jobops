package com.sutulovai.jobops.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class DbTime {

    public static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private DbTime() {}

    /** Monday 00:00 Europe/Berlin for the calendar week containing {@code day} (Berlin local date). */
    public static Instant weekStartInstantMondayBerlin(LocalDate dayInBerlin) {
        return dayInBerlin.with(DayOfWeek.MONDAY).atStartOfDay(BERLIN).toInstant();
    }

    /** Midnight at the start of {@code day} in Europe/Berlin. */
    public static Instant startOfDayBerlin(LocalDate dayInBerlin) {
        return dayInBerlin.atStartOfDay(BERLIN).toInstant();
    }
}
