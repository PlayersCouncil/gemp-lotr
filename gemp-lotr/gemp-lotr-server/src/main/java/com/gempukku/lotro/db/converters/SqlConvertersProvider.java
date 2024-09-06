package com.gempukku.lotro.db.converters;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConvertersProvider;

import java.time.LocalDate;
import java.util.Map;

public class SqlConvertersProvider implements ConvertersProvider {

    @Override
    public void fill(Map<Class<?>, Converter<?>> mapToFill) {
        mapToFill.put(LocalDate.class, new LocalDateConverter());
    }
}
