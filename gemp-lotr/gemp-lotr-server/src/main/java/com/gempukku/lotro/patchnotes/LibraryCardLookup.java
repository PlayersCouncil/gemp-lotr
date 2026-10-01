package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.common.CardInfo;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.ErrataCatalog;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * {@link CardLookup} against the card library.
 * <ul>
 *     <li>A blueprint id ({@code 1_5}, {@code 51_5}) names exactly that card (an errata id shows the errata).</li>
 *     <li>A collector's info ({@code 1C5}, {@code V1R10}) or a card name names the printed card: the errata versions of
 *     a card (sets 50-89, 150-199) and the reprints the library maps to it count as that one card, so
 *     {@code [[Cleaving Blow]]} is 1_5 although 51_5 has the same name.</li>
 *     <li>A name is matched against "Title, Subtitle" first, then against the title alone, ignoring case, accents,
 *     punctuation and spacing ("Ereth&oacute;n" = "Erethon", "Bill the Pony" = "bill the pony").  A name several
 *     different cards share ("Aragorn", "Hobbit Sword") is an error that lists them, so the note can use an id.</li>
 * </ul>
 * The index is built on first use; {@link #invalidate()} drops it (the card library was reloaded).
 */
public class LibraryCardLookup implements CardLookup {
    public static final Pattern BLUEPRINT_ID = Pattern.compile("^\\d{1,3}_\\d{1,4}$");
    private static final Pattern NOT_LETTER_OR_DIGIT = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final int MAX_LISTED = 6;

    private final LotroCardBlueprintLibrary _library;
    private volatile Index _index;

    private record Index(Map<String, Set<String>> byFullName, Map<String, Set<String>> byTitle,
                         Map<String, Set<String>> byCollectorInfo) {
    }

    public LibraryCardLookup(LotroCardBlueprintLibrary library) {
        _library = library;
    }

    /** The card library changed: the next lookup rebuilds the index. */
    public void invalidate() {
        _index = null;
    }

    @Override
    public Result resolve(String reference) {
        String ref = reference == null ? "" : reference.trim();
        if (ref.isEmpty())
            return Result.failed("the card link is empty");

        if (BLUEPRINT_ID.matcher(ref).matches()) {
            try {
                LotroCardBlueprint card = _library.getLotroCardBlueprint(ref);
                return Result.found(ref, card.getFullName());
            } catch (CardNotFoundException exp) {
                return Result.failed("there is no card with the id " + ref);
            }
        }

        Index index = index();
        Set<String> ids = index.byCollectorInfo().get(collectorKey(ref));
        if (ids == null) {
            String key = nameKey(ref);
            ids = index.byFullName().get(key);
            if (ids == null)
                ids = index.byTitle().get(key);
        }
        if (ids == null || ids.isEmpty())
            return Result.failed("no card is named or numbered '" + ref + "'");
        if (ids.size() > 1)
            return Result.failed("'" + ref + "' matches " + ids.size() + " different cards (" + describe(ids)
                    + "); use the one's id instead, e.g. [[" + ids.iterator().next() + "|" + ref + "]]");
        String id = ids.iterator().next();
        return Result.found(id, nameOf(id));
    }

    private String nameOf(String id) {
        try {
            return _library.getLotroCardBlueprint(id).getFullName();
        } catch (CardNotFoundException exp) {
            return id;
        }
    }

    private String describe(Set<String> ids) {
        List<String> listed = new ArrayList<>();
        for (String id : ids) {
            if (listed.size() == MAX_LISTED) {
                listed.add("...");
                break;
            }
            String info = null;
            try {
                LotroCardBlueprint card = _library.getLotroCardBlueprint(id);
                CardInfo cardInfo = card.getCardInfo();
                info = card.getFullName() + (cardInfo != null && cardInfo.collInfo != null ? " " + cardInfo.collInfo.trim() : "");
            } catch (CardNotFoundException ignored) {
                // listed by id alone
            }
            listed.add(info == null ? id : id + " " + info);
        }
        return String.join(", ", listed);
    }

    private Index index() {
        Index index = _index;
        if (index == null) {
            index = build();
            _index = index;
        }
        return index;
    }

    private Index build() {
        Map<String, LotroCardBlueprint> cards = _library.getBaseCards();
        Map<String, Set<String>> byFullName = new HashMap<>();
        Map<String, Set<String>> byTitle = new HashMap<>();
        Map<String, Set<String>> byCollectorInfo = new HashMap<>();
        for (Map.Entry<String, LotroCardBlueprint> entry : cards.entrySet()) {
            String id = entry.getKey();
            if (LotroCardBlueprintLibrary.isPlaceholderId(id))
                continue;
            LotroCardBlueprint card = entry.getValue();
            String printed = printedCard(id, cards);
            if (card.getTitle() != null) {
                add(byFullName, nameKey(card.getFullName()), printed);
                add(byTitle, nameKey(card.getTitle()), printed);
            }
            CardInfo info = card.getCardInfo();
            if (info != null && info.collInfo != null && !info.collInfo.isBlank())
                add(byCollectorInfo, collectorKey(info.collInfo), printed);
        }
        return new Index(freeze(byFullName), freeze(byTitle), freeze(byCollectorInfo));
    }

    /** The printed card an id stands for: a reprint's original, an errata's original. */
    private String printedCard(String id, Map<String, LotroCardBlueprint> cards) {
        String base = _library.getBaseBlueprintId(id);
        String original = ErrataCatalog.errataBase(base);
        if (original != null && cards.containsKey(original))
            return _library.getBaseBlueprintId(original);
        return base;
    }

    private static void add(Map<String, Set<String>> map, String key, String id) {
        if (key.isEmpty())
            return;
        map.computeIfAbsent(key, k -> new TreeSet<>(LibraryCardLookup::compareIds)).add(id);
    }

    private static Map<String, Set<String>> freeze(Map<String, Set<String>> map) {
        map.replaceAll((k, v) -> Collections.unmodifiableSet(v));
        return Collections.unmodifiableMap(map);
    }

    /** "1_5" before "1_45" before "11_1": by set, then card number. */
    static int compareIds(String a, String b) {
        String[] pa = a.split("_");
        String[] pb = b.split("_");
        try {
            int set = Integer.compare(Integer.parseInt(pa[0]), Integer.parseInt(pb[0]));
            if (set != 0)
                return set;
            return Integer.compare(Integer.parseInt(pa[1]), Integer.parseInt(pb[1]));
        } catch (RuntimeException exp) {
            return a.compareTo(b);
        }
    }

    /** Lower case, accents dropped, only letters and digits: "Ereth&oacute;n, Naith Lieutenant" -> "erethonnaithlieutenant". */
    static String nameKey(String name) {
        if (name == null)
            return "";
        String decomposed = Normalizer.normalize(name, Normalizer.Form.NFD);
        String plain = MARKS.matcher(decomposed).replaceAll("");
        return NOT_LETTER_OR_DIGIT.matcher(plain.toLowerCase(Locale.ROOT)).replaceAll("");
    }

    static String collectorKey(String collectorInfo) {
        return collectorInfo == null ? "" : collectorInfo.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
