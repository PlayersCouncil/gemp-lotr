package com.gempukku.lotro.collection;

import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.patchnotes.PatchNoteAnnouncer;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.LongSupplier;

public class CachedTransferDAO implements TransferDAO, Cached {
    private final TransferDAO _delegate;
    private final Set<String> _playersWithoutDelivery = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> _playersWithoutPacks = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> _playersWithoutAnnouncements = Collections.synchronizedSet(new HashSet<>());
    private final MarkdownParser _markdownParser;
    private DBDefs.Announcement _parsedAnnouncement;

    /** how often the current announcement is looked up again, so one that starts or ends later shows up / goes away */
    public static final long ANNOUNCEMENT_RECHECK_MS = 60_000;
    private final LongSupplier _clock;
    private volatile long _announcementCheckedAt;

    public CachedTransferDAO(TransferDAO delegate, MarkdownParser parser) {
        this(delegate, parser, System::currentTimeMillis);
    }

    CachedTransferDAO(TransferDAO delegate, MarkdownParser parser, LongSupplier clock) {
        _delegate = delegate;
        _markdownParser = parser;
        _clock = clock;
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
        recheckCurrentAnnouncement();
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
    public int addTransferTo(boolean notifyPlayer, String player, String reason, String collectionName, int currency,
            CardCollection items, String message) {
        return _delegate.addTransferTo(notifyPlayer, player, reason, collectionName, currency, items, message);
    }

    @Override
    public int addTransferToRaw(boolean notifyPlayer, String player, String reason, String collectionName, int currency,
            String rawContents, String message) {
        return _delegate.addTransferToRaw(notifyPlayer, player, reason, collectionName, currency, rawContents, message);
    }

    @Override
    public int addTransferFrom(String player, String reason, String collectionName, int currency, CardCollection items) {
        return _delegate.addTransferFrom(player, reason, collectionName, currency, items);
    }

    @Override
    public boolean hasUndeliveredLeagueNotifications(Player player) {
        return _delegate.hasUndeliveredLeagueNotifications(player);
    }

    @Override
    public List<DBDefs.Transfer> consumeUndeliveredLeagueNotifications(Player player) {
        return _delegate.consumeUndeliveredLeagueNotifications(player);
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
        _announcementCheckedAt = _clock.getAsLong();
        _parsedAnnouncement = parse(_delegate.getCurrentAnnouncement());
    }

    private DBDefs.Announcement parse(DBDefs.Announcement announcement) {
        if(announcement != null) {
            // a patch note's announcement starts with a marker line (an HTML comment, which the renderer would show)
            announcement.content = _markdownParser.renderMarkdown(PatchNoteAnnouncer.stripMarker(announcement.content), false);
        }
        return announcement;
    }

    /**
     * The current announcement is the one that started last of those running (DbTransferDAO.getCurrentAnnouncement).
     * It is looked up again every ANNOUNCEMENT_RECHECK_MS, so an announcement that starts later, or one added by
     * another server process, shows up without a cache clear, and one that has ended stops being shown (within that
     * time; the database decides what is running).  When it changes, players are checked for it again.
     */
    private synchronized void recheckCurrentAnnouncement() {
        long now = _clock.getAsLong();
        if (now - _announcementCheckedAt < ANNOUNCEMENT_RECHECK_MS)
            return;
        _announcementCheckedAt = now;
        DBDefs.Announcement cached = _parsedAnnouncement;
        DBDefs.Announcement current = _delegate.getCurrentAnnouncement();
        int cachedId = cached == null ? -1 : cached.id;
        int currentId = current == null ? -1 : current.id;
        if (cachedId == currentId)
            return;
        _parsedAnnouncement = parse(current);
        _playersWithoutAnnouncements.clear();
    }

    @Override
    public DBDefs.Announcement getCurrentAnnouncement() {
        recheckCurrentAnnouncement();
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

    // Patch notes feed
    // Not cached here: the patch notes library keeps them for a couple of minutes (the raw Markdown, which it renders
    // itself; _parsedAnnouncement holds the popup's rendering).
    @Override
    public List<DBDefs.Announcement> getPastAnnouncements() {
        return _delegate.getPastAnnouncements();
    }

    // Patch note announcements

    @Override
    public int addServerAnnouncementIfAbsent(String marker, String title, String markdown, ZonedDateTime start, ZonedDateTime until) {
        int id = _delegate.addServerAnnouncementIfAbsent(marker, title, markdown, start, until);
        if (id > 0) {
            clearCache();
            cacheCurrentAnnouncement();
        }
        return id;
    }
}
