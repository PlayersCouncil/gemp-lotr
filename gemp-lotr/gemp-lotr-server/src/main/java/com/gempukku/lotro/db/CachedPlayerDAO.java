package com.gempukku.lotro.db;

import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.Player;
import org.apache.commons.collections4.map.LRUMap;

import java.sql.SQLException;
import java.util.*;

public class CachedPlayerDAO implements PlayerDAO, Cached {
    private final PlayerDAO _delegate;
    private final Map<Integer, Player> _playerById = Collections.synchronizedMap(new LRUMap(500));
    private final Map<String, Player> _playerByName = Collections.synchronizedMap(new LRUMap(500));
    private Set<String> _bannedUsernames = new HashSet<>();

    public CachedPlayerDAO(PlayerDAO delegate) {
        _delegate = delegate;
    }

    @Override
    public void clearCache() {
        _playerById.clear();
        _playerByName.clear();
        _bannedUsernames.clear();
    }

    @Override
    public int getItemCount() {
        return _playerById.size() + _playerByName.size();
    }

    @Override
    public boolean resetUserPassword(String login) throws SQLException {
        final boolean success = _delegate.resetUserPassword(login);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public boolean banPlayerPermanently(String login) throws SQLException {
        final boolean success = _delegate.banPlayerPermanently(login);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public boolean banPlayerTemporarily(String login, long dateTo) throws SQLException {
        final boolean success = _delegate.banPlayerTemporarily(login, dateTo);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public boolean unBanPlayer(String login) throws SQLException {
        final boolean success = _delegate.unBanPlayer(login);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public boolean addPlayerFlag(String login, Player.Type flag) throws SQLException {
        final boolean success = _delegate.addPlayerFlag(login, flag);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public boolean removePlayerFlag(String login, Player.Type flag) throws SQLException {
        final boolean success = _delegate.removePlayerFlag(login, flag);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public List<Player> findSimilarAccounts(String login) throws SQLException {
        return _delegate.findSimilarAccounts(login);
    }

    @Override
    public Set<String> getBannedUsernames() throws SQLException {
        if(_bannedUsernames.isEmpty())
            _bannedUsernames = _delegate.getBannedUsernames();
        return new HashSet<>(_bannedUsernames);
    }

    @Override
    public Player getPlayer(int id) {
        Player player = (Player) _playerById.get(id);
        if (player == null) {
            player = _delegate.getPlayer(id);
            if (player != null) {
                _playerById.put(id, player);
                _playerByName.put(player.getName(), player);
            }
        }
        return player;
    }

    @Override
    public Player getPlayer(String playerName) {
        Player player = (Player) _playerByName.get(playerName);
        if (player == null) {
            player = _delegate.getPlayer(playerName);
            if (player != null) {
                _playerById.put(player.getId(), player);
                _playerByName.put(player.getName(), player);
            }
        }
        return player;
    }

    @Override
    public Player loginUser(String login, String password) throws SQLException {
        return _delegate.loginUser(login, password);
    }

    @Override
    public boolean registerUser(String login, String password, String remoteAddr) throws SQLException, LoginInvalidException {
        boolean registered = _delegate.registerUser(login, password, remoteAddr);
        if (registered)
            _playerByName.remove(login);
        return registered;
    }

    @Override
    public boolean registerBot(String login) throws LoginInvalidException {
        boolean registered = _delegate.registerBot(login);
        if (registered)
            _playerByName.remove(login);
        return registered;
    }

    @Override
    public void setLastReward(Player player, int currentReward) throws SQLException {
        _delegate.setLastReward(player, currentReward);
        _playerById.remove(player.getId());
        _playerByName.remove(player.getName());
    }

    @Override
    public void updateLastLoginIp(String login, String remoteAddr) throws SQLException {
        _delegate.updateLastLoginIp(login, remoteAddr);
    }

    @Override
    public List<DBDefs.Player> getAllPlayers() {
        return _delegate.getAllPlayers();
    }

    @Override
    public boolean updateLastReward(Player player, int previousReward, int currentReward) throws SQLException {
        boolean updated = _delegate.updateLastReward(player, previousReward, currentReward);
        if (updated) {
            _playerById.remove(player.getId());
            _playerByName.remove(player.getName());
        }
        return updated;
    }
}
