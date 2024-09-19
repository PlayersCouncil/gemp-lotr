package com.gempukku.lotro.db.vo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CollectionType {
    public final static CollectionType MY_CARDS = new CollectionType("permanent", "My cards");
    public final static CollectionType TROPHY = new CollectionType("trophy", "Trophies");
    public final static CollectionType ALL_CARDS = new CollectionType("default", "All cards");

    public final static CollectionType OWNED_TOURNAMENT_CARDS = new CollectionType("permanent+trophy", "My cards");

    private final String _code;
    private final String _fullName;

    private final static Map<String, CollectionType> DEFINED_COLLECTION_TYPES = new HashMap<>();

    static {
        DEFINED_COLLECTION_TYPES.put(MY_CARDS.getCode(), MY_CARDS);
        DEFINED_COLLECTION_TYPES.put(TROPHY.getCode(), TROPHY);
        DEFINED_COLLECTION_TYPES.put(ALL_CARDS.getCode(), ALL_CARDS);
        DEFINED_COLLECTION_TYPES.put(OWNED_TOURNAMENT_CARDS.getCode(), OWNED_TOURNAMENT_CARDS);
    }

    public static CollectionType parseCollectionCode(String code) {
        return DEFINED_COLLECTION_TYPES.get(code);
    }

    public CollectionType(long code, String fullName) {
        this(String.valueOf(code), fullName);
    }

    public CollectionType(String code, String fullName) {
        _code = code;
        _fullName = fullName;
    }

    public String getCode() {
        return _code;
    }

    public String getFullName() {
        return _fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CollectionType that = (CollectionType) o;

        return Objects.equals(_code, that._code);
    }

    @Override
    public int hashCode() {
        return _code != null ? _code.hashCode() : 0;
    }
}
