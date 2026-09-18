package com.gempukku.lotro.prizes;

/**
 * One line of a {@link PrizeTier}: either a concrete card or pack ({@link #blueprintId}, e.g. {@code 9_1*},
 * {@code 1_2T}, {@code Event Chase Booster}) or a <em>promise</em> ({@link #promise}) — a labelled prize whose card
 * does not exist yet and is handed out as a set-404 placeholder until an admin resolves it.  Exactly one of the two
 * is set.
 */
public class PrizeItem {
    public String blueprintId;
    public String promise;
    public int count = 1;

    public PrizeItem() {
    }

    public static PrizeItem card(String blueprintId, int count) {
        var item = new PrizeItem();
        item.blueprintId = blueprintId;
        item.count = count;
        return item;
    }

    public static PrizeItem promise(String label, int count) {
        var item = new PrizeItem();
        item.promise = label;
        item.count = count;
        return item;
    }

    /**
     * Deliberately not named {@code isPromise}: fastjson2 would serialise a boolean "is" getter as a property
     * called {@code promise} and clobber the field.
     */
    public boolean hasPromise() {
        return promise != null && !promise.isBlank();
    }
}
