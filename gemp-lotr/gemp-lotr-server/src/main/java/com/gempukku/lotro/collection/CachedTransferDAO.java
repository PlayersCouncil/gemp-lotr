package com.gempukku.lotro.collection;

import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.Player;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CachedTransferDAO implements TransferDAO, Cached {
    private final TransferDAO _delegate;
    private final Set<String> _playersWithoutDelivery = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> _playersWithoutPacks = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> _playersWithoutAnnouncements = Collections.synchronizedSet(new HashSet<>());
    private final MarkdownParser _markdownParser;
    private DBDefs.Announcement _parsedAnnouncement;

    public CachedTransferDAO(TransferDAO delegate, MarkdownParser parser) {
        _delegate = delegate;
        _markdownParser = parser;
    }

    @Override
    public void clearCache() {
        _playersWithoutDelivery.clear();
        _playersWithoutPacks.clear();
        _playersWithoutAnnouncements.clear();
        _parsedAnnouncement = null;
    }

    @Override
    public int getItemCount() {
        return _playersWithoutDelivery.size() + _playersWithoutPacks.size() + _playersWithoutAnnouncements.size();
    }

    @Override
    public boolean hasUndeliveredPackages(Player player) {
        if (_playersWithoutDelivery.contains(player.getName()))
            return false;
        boolean value = _delegate.hasUndeliveredPackages(player);
        if (!value) {
            _playersWithoutDelivery.add(player.getName());
        }
        return value;
    }

    @Override
    public boolean hasUndeliveredSealedContents(Player player) {
        if (_playersWithoutPacks.contains(player.getName()))
            return false;
        boolean value = _delegate.hasUndeliveredSealedContents(player);
        if (!value) {
            _playersWithoutPacks.add(player.getName());
        }
        return value;
    }

    @Override
    public boolean hasUndeliveredAnnouncement(Player player) {
        if (_playersWithoutAnnouncements.contains(player.getName()))
            return false;
        boolean value = _delegate.hasUndeliveredAnnouncement(player);
        if (!value) {
            _playersWithoutAnnouncements.add(player.getName());
        }
        return value;
    }

    @Override
    public Map<String, ? extends CardCollection> consumeUndeliveredPackages(Player player) {
        return _delegate.consumeUndeliveredPackages(player);
    }

    @Override
    public Map<String, ? extends CardCollection> consumeUndeliveredPackContents(Player player) {
        return _delegate.consumeUndeliveredPackContents(player);
    }

    @Override
    public int addTransferTo(boolean notifyPlayer, String player, String reason, String collectionName, int currency, CardCollection items) {
        if (notifyPlayer) {
            _playersWithoutDelivery.remove(player);
            _playersWithoutPacks.remove(player);
        }
        return _delegate.addTransferTo(notifyPlayer, player, reason, collectionName, currency, items);
    }

    @Override
    public int addTransferFrom(String player, String reason, String collectionName, int currency, CardCollection items) {
        return _delegate.addTransferFrom(player, reason, collectionName, currency, items);
    }

    @Override
    public DBDefs.Announcement getUndeliveredAnnouncement(Player player) {
        return this.getUndeliveredAnnouncement(player, getCurrentAnnouncement());
    }

    @Override
    public DBDefs.Announcement getUndeliveredAnnouncement(Player player, DBDefs.Announcement announcement) {
        return _delegate.getUndeliveredAnnouncement(player, announcement);
    }

    @Override
    public DBDefs.Transfer addAnnouncementEntryForPlayer(DBDefs.Announcement announcement, Player player) {
        if(getCurrentAnnouncement() != null) {
            return _delegate.addAnnouncementEntryForPlayer(_parsedAnnouncement, player);
        }

        return null;
    }

    @Override
    public void dismissAnnouncement(DBDefs.Announcement announcement, Player player) {
        _delegate.dismissAnnouncement(announcement, player);
    }

    @Override
    public void snoozeAnnouncement(DBDefs.Announcement announcement, Player player) {
        _delegate.snoozeAnnouncement(announcement, player);
    }

    private void cacheCurrentAnnouncement() {
        _parsedAnnouncement = _delegate.getCurrentAnnouncement();
        if(_parsedAnnouncement != null) {
            _parsedAnnouncement.content = _markdownParser.renderMarkdown(_parsedAnnouncement.content, false);
        }
    }

    @Override
    public DBDefs.Announcement getCurrentAnnouncement() {
        if(_parsedAnnouncement == null) {
            cacheCurrentAnnouncement();
        }

        return _parsedAnnouncement;
    }

    @Override
    public int addServerAnnouncement(String title, String markdown, ZonedDateTime start, ZonedDateTime until) {
        clearCache();

        int id = _delegate.addServerAnnouncement(title, markdown, start, until);
        cacheCurrentAnnouncement();

        return id;
    }
}
