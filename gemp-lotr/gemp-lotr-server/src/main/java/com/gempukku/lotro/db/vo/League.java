package com.gempukku.lotro.db.vo;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.*;
import com.gempukku.lotro.packs.ProductLibrary;

import java.io.IOException;
import java.util.Objects;

public class League {

    private final String _name;
    private final int _cost;
    private final long _code;

    public enum LeagueType {
        CONSTRUCTED,
        SEALED,
        SOLODRAFT;

        public static LeagueType parse(String name) {
            String nameCaps = name.toUpperCase().trim().replace(' ', '_').replace('-', '_');

            for (LeagueType type : values()) {
                if (type.toString().equals(nameCaps))
                    return type;
            }
            return null;
        }
    }

    private final LeagueType _type;
    private final int _status;
    private final String _unparsedParams;
    private LeagueData _leagueData;

    public League(DBDefs.League row) {
        this(row.name, row.cost, row.code, LeagueType.parse(row.type), row.parameters, row.status);
    }

    public League(String name, int cost, long code, LeagueType type, String parameters, int status) {
        _name = name;
        _cost = cost;
        _code = code;
        _type = type;
        _unparsedParams = parameters;
        _status = status;
    }

    public boolean inviteOnly() { return _leagueData.getParameters().inviteOnly; }

    public String getDescription() { return _leagueData.getParameters().description; }

    public int getCost() {
        return _cost;
    }

    public String getName() {
        return _name;
    }

    public long getCode() {
        return _code;
    }

    public String getCodeStr() {
        return String.valueOf(_code);
    }

    public synchronized LeagueData getLeagueData(ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions) {
        if (_leagueData == null) {
            try {
                switch (_type) {
                    case CONSTRUCTED ->
                            _leagueData = ConstructedLeague.fromRawParameters(productLibrary, formatLibrary, _unparsedParams);
                    case SEALED ->
                            _leagueData = SealedLeague.fromRawParameters(productLibrary, formatLibrary, _unparsedParams);
                    case SOLODRAFT ->
                            _leagueData = SoloDraftLeague.fromRawParameters(productLibrary,  formatLibrary, soloDraftDefinitions, _unparsedParams);
                    default ->
                            throw new IllegalArgumentException("Unrecognized league type '" + _type + "'.");
                }
            } catch (Exception exp) {
                throw new RuntimeException("Unable to create LeagueData", exp);
            }
        }
        return _leagueData;
    }

    public int getStatus() {
        return _status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        League league = (League) o;

        return Objects.equals(_type, league._type);
    }

    @Override
    public int hashCode() {
        return _type != null ? _type.hashCode() : 0;
    }
}
