package com.sutulovai.jobops.repository.jooq;

import org.jooq.Record;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.jooq.impl.DSL.field;

/**
 * Normalizes JDBC date/timestamp types for jOOQ plain DSL (no generated types).
 */
public final class RecordTimes {

    private RecordTimes() {}

    public static Instant instantUtcOrNull(Record r, String column) {
        return toInstantUtc(r.get(field(column)));
    }

    public static Instant instantUtcOrNow(Record r, String column) {
        var i = instantUtcOrNull(r, column);
        return i != null ? i : Instant.now();
    }

    public static LocalDate localDateOrNull(Record r, String column) {
        return toLocalDate(r.get(field(column)));
    }

    private static LocalDate toLocalDate(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof LocalDate ld) {
            return ld;
        }
        if (v instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(v.toString());
    }

    private static Instant toInstantUtc(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Instant i) {
            return i;
        }
        if (v instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (v instanceof ZonedDateTime zdt) {
            return zdt.toInstant();
        }
        if (v instanceof LocalDateTime ldt) {
            return ldt.toInstant(ZoneOffset.UTC);
        }
        if (v instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (v instanceof java.util.Date d) {
            return d.toInstant();
        }
        throw new IllegalStateException(
                "Unsupported JDBC temporal type: " + v.getClass().getName());
    }
}
