package com.gempukku.lotro.service;

import com.gempukku.lotro.db.IpBanDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;

import java.sql.SQLException;

public class AdminService {
    // long, not int: days * an int day length overflowed past 24 days, so a 30-day ban expired ~20 days in the past.
    public static final long DAY_IN_MILIS = 1000L * 60 * 60 * 24;
    private final PlayerDAO _playerDAO;
    private final LoggedUserHolder _loggedUserHolder;
    private final IpBanDAO _ipBanDAO;

    public AdminService(PlayerDAO playerDAO, IpBanDAO ipBanDAO, LoggedUserHolder loggedUserHolder) {
        _playerDAO = playerDAO;
        _ipBanDAO = ipBanDAO;
        _loggedUserHolder = loggedUserHolder;
    }

    public boolean resetUserPassword(String login) {
        try {
            final boolean success = _playerDAO.resetUserPassword(login);
            if (!success)
                return false;
            _loggedUserHolder.forceLogoutUser(login);
            return true;
        } catch (SQLException exp) {
            return false;
        }
    }

    public boolean banUser(String login) {
        try {
            final boolean success = _playerDAO.banPlayerPermanently(login);
            if (!success)
                return false;
            _loggedUserHolder.forceLogoutUser(login);
            return true;
        } catch (SQLException exp) {
            return false;
        }
    }

    public static long tempBanExpiry(long nowMillis, int days) {
        return nowMillis + days * DAY_IN_MILIS;
    }

    public boolean banUserTemp(String login, int days) {
        if (days <= 0)
            return false;
        try {
            final boolean success = _playerDAO.banPlayerTemporarily(login, tempBanExpiry(System.currentTimeMillis(), days));
            if (!success)
                return false;
            _loggedUserHolder.forceLogoutUser(login);
            return true;
        } catch (SQLException exp) {
            return false;
        }
    }

    public boolean unBanUser(String login) {
        try {
            return _playerDAO.unBanPlayer(login);
        } catch (SQLException exp) {
            return false;
        }
    }

    public boolean banIp(String login) {
        final Player player = _playerDAO.getPlayer(login);
        if (player == null)
            return false;
        final String lastIp = player.getLastIp();
        
        _ipBanDAO.addIpBan(lastIp);
        
        return banUser(login);
    }

    public boolean banIpPrefix(String login) {
        final Player player = _playerDAO.getPlayer(login);
        if (player == null)
            return false;
        final String lastIp = player.getLastIp();
        String lastIpPrefix = lastIp.substring(0, lastIp.lastIndexOf(".")+1);

        _ipBanDAO.addIpPrefixBan(lastIpPrefix);

        return banUser(login);
    }
}
