package com.gempukku.lotro.db;

import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.tournament.TournamentPlayerDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DbTournamentPlayerDAO implements TournamentPlayerDAO {
    private final DbAccess _dbAccess;

    public DbTournamentPlayerDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addPlayer(String tournamentId, String playerName, LotroDeck deck) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement("insert into tournament_player (tournament_id, player, deck_name, deck) values (?, ?, ?, ?)")) {
                    statement.setString(1, tournamentId);
                    statement.setString(2, playerName);
                    if(deck == null) {
                        statement.setString(3, "");
                        statement.setString(4, "");
                    }
                    else {
                        statement.setString(3, deck.getDeckName());
                        statement.setString(4, DeckSerialization.buildContentsFromDeck(deck));
                    }

                    statement.execute();
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public void updatePlayerDeck(String tournamentId, String playerName, LotroDeck deck) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement("update tournament_player set deck_name = ?, deck = ? where tournament_id=? and player=?")) {
                    statement.setString(1, deck.getDeckName());
                    statement.setString(2, DeckSerialization.buildContentsFromDeck(deck));
                    statement.setString(3, tournamentId);
                    statement.setString(4, playerName);
                    statement.execute();
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public void dropPlayer(String tournamentId, String playerName) {
        try {
            try (Connection conn = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = conn.prepareStatement("update tournament_player set dropped=true where tournament_id=? and player=?")) {
                    statement.setString(1, tournamentId);
                    statement.setString(2, playerName);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public Set<String> getPlayers(String tournamentId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select player from tournament_player where tournament_id=?")) {
                    statement.setString(1, tournamentId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<>();
                        while (rs.next()) {
                            String player = rs.getString(1);
                            result.add(player);
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public Map<String, LotroDeck> getPlayerDecks(String tournamentId, String format) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select player, deck_name, deck from tournament_player where tournament_id=? and deck_name is not null")) {
                    statement.setString(1, tournamentId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Map<String, LotroDeck> result = new HashMap<>();
                        while (rs.next()) {
                            String player = rs.getString(1);
                            String deckName = rs.getString(2);
                            String contents = rs.getString(3);

                            result.put(player, DeckSerialization.buildDeckFromContents(deckName, contents, format, ""));
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public Set<String> getDroppedPlayers(String tournamentId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select player from tournament_player where tournament_id=? and dropped=true")) {
                    statement.setString(1, tournamentId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<>();
                        while (rs.next()) {
                            result.add(rs.getString(1));
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public LotroDeck getPlayerDeck(String tournamentId, String playerName, String format) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select deck_name, deck from tournament_player where tournament_id=? and player=?")) {
                    statement.setString(1, tournamentId);
                    statement.setString(2, playerName);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next())
                            return DeckSerialization.buildDeckFromContents(rs.getString(1), rs.getString(2), format, "");
                        else
                            return null;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }
}
