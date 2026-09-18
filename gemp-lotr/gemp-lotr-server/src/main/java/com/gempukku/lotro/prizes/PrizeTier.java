package com.gempukku.lotro.prizes;

import java.util.ArrayList;

/**
 * One prize tier of an event: who qualifies (a range of places, or a minimum number of games played) and what they
 * get.  Serialised with fastjson2 as part of the event definition (for instance {@code LeagueParams.prizeTiers}),
 * hence the public fields and the no-arg constructor.
 */
public class PrizeTier {
    public enum Kind {
        /** Awarded by final standing: places {@link #from}..{@link #to}, with lenient ties. */
        PLACEMENT,
        /** Awarded for playing at least {@link #games} games, in this event or across its campaign. */
        PARTICIPATION
    }

    public enum Scope {
        /** Count only games played in this event. */
        EVENT,
        /** Count games across every event carrying the same campaign tag; awarded at most once per campaign. */
        CAMPAIGN
    }

    public Kind kind = Kind.PLACEMENT;
    /** PLACEMENT: first qualifying place, 1-based inclusive. */
    public int from = 1;
    /** PLACEMENT: last qualifying place, inclusive; {@code to >= from}. */
    public int to = 1;
    /** PARTICIPATION: minimum number of games played. */
    public int games = 0;
    /** PARTICIPATION only: whether games are counted in this event or across the campaign. */
    public Scope scope = Scope.EVENT;
    /** Optional label shown to admins.  For CAMPAIGN tiers it is also the dedup key (default {@code games>=N}). */
    public String label;
    public ArrayList<PrizeItem> items = new ArrayList<>();

    public PrizeTier() {
    }

    public static PrizeTier placement(int from, int to, PrizeItem... items) {
        var tier = new PrizeTier();
        tier.kind = Kind.PLACEMENT;
        tier.from = from;
        tier.to = to;
        for (PrizeItem item : items)
            tier.items.add(item);
        return tier;
    }

    public static PrizeTier participation(int games, Scope scope, PrizeItem... items) {
        var tier = new PrizeTier();
        tier.kind = Kind.PARTICIPATION;
        tier.games = games;
        tier.scope = scope;
        for (PrizeItem item : items)
            tier.items.add(item);
        return tier;
    }

    /**
     * The label used to identify this tier when it must be recognised again later (campaign dedup): the explicit
     * label, or {@code games>=N} for an unlabelled participation tier, or {@code places F-T} for a placement tier.
     */
    public String effectiveLabel() {
        if (label != null && !label.isBlank())
            return label.trim();
        if (kind == Kind.PARTICIPATION)
            return "games>=" + games;
        return "places " + from + "-" + to;
    }
}
