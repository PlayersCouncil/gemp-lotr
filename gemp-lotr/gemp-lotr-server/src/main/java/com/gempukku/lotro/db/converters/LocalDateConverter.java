package com.gempukku.lotro.db.converters;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class LocalDateConverter implements Converter<LocalDate> {
    @Override
    public LocalDate convert(final Object val) throws ConverterException {
        if (val instanceof java.sql.Date) {
            return ((java.sql.Date) val).toLocalDate();
        } else {
            return null;
        }
    }

    @Override
    public Object toDatabaseParam(final LocalDate val) {
        if (val == null) {
            return null;
        } else {
            return new java.sql.Date(val.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
        }
    }
}
