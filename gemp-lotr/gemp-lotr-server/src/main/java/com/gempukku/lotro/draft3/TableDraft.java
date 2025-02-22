package com.gempukku.lotro.draft3;

public interface TableDraft {
    DraftPlayer registerPlayer(String name);
    DraftPlayer getPlayer(String name);
    boolean removePlayer(String name);
    void startDraft();
    boolean readyToPick(DraftPlayer who);
    boolean passBooster(DraftPlayer from, Booster booster);
    boolean isFinished();
}
