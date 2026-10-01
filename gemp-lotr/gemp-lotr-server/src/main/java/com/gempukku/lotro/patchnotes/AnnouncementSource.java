package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;

/**
 * Where the Patch Notes feed gets the server announcements it shows among the notes (the {@code announcements}
 * table, through {@code TransferDAO.getPastAnnouncements()}).  Tests pass a fake.
 */
@FunctionalInterface
public interface AnnouncementSource {
    /**
     * @return the announcements whose start has passed, in any order, with their Markdown content as stored (the
     * library drops the ones that repeat a patch note, and any whose start is still ahead)
     */
    List<DBDefs.Announcement> getPastAnnouncements();
}
