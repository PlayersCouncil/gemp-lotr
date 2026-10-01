package com.gempukku.lotro.patchnotes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Culture icons in patch notes: {@code :isengard:} or {@code [isengard]} (any case, or one of the aliases in
 * {@link #ALIASES}) becomes the culture's icon, inline at text size, named in its alt text and tooltip.  The icons are
 * the ones Help › PC Errata's Culture column shows ({@code images/cultures/<code>.png}, 20px tall, each at its own
 * width).  An unknown name is left as it is written.
 * <p>
 * Twilight icons ride along: {@code (0)}..{@code (9)} and {@code (X)} (upper case only; one digit, so {@code (10)} stays
 * text), and {@code :0twilight:}..{@code :9twilight:} / {@code :xtwilight:} (any case), become the twilight cost icons
 * {@code images/cultures/twilight_<n>.svg}, named "(1) twilight" and so on.  Same places as the culture icons.
 * <p>
 * Where tokens count is up to the caller: {@link PatchNoteRenderer} looks only at plain text (not code, not the text
 * of a link or an image, not a {@code [[card link]]}); {@link #find} itself skips card links and the {@code [x][y]} /
 * {@code [x](y)} / {@code ![x]} shapes.
 */
public final class CultureIcons {
    /**
     * The icon of one culture (or twilight cost): its code (for a culture, its file in {@code images/cultures/} without
     * .png), its English name (alt text and tooltip) and its file in {@code images/cultures/}.
     */
    public record Icon(String code, String name, String file) {
        /** a culture: {@code images/cultures/<code>.png} */
        public Icon(String code, String name) {
            this(code, name, code + ".png");
        }

        /** relative to hall.html */
        public String src() {
            return "images/cultures/" + file;
        }
    }

    /** A token in a text: where it is and which culture it names. */
    public record Match(int start, int end, Icon icon) {
    }

    /** every name a note can use (lower case) -> its icon, in README order */
    public static final Map<String, Icon> ALIASES;

    /** twilight cost ("0".."9", "X") -> its icon */
    public static final Map<String, Icon> TWILIGHT;

    static {
        Map<String, Icon> aliases = new LinkedHashMap<>();
        add(aliases, "dwarven", "Dwarven", "dwarven", "dwarf", "dwarves", "dwarvish");
        add(aliases, "elven", "Elven", "elven", "elf", "elves", "elvish");
        add(aliases, "gandalf", "Gandalf", "gandalf");
        add(aliases, "gollum", "Gollum", "gollum");
        add(aliases, "gondor", "Gondor", "gondor");
        add(aliases, "rohan", "Rohan", "rohan");
        add(aliases, "shire", "Shire", "shire");
        add(aliases, "dunland", "Dunland", "dunland", "dunlending", "dunlendings");
        add(aliases, "isengard", "Isengard", "isengard");
        add(aliases, "men", "Men", "men");
        add(aliases, "moria", "Moria", "moria");
        add(aliases, "orc", "Orc", "orc", "orcs");
        add(aliases, "raider", "Raider", "raider", "raiders");
        add(aliases, "sauron", "Sauron", "sauron");
        add(aliases, "uruk_hai", "Uruk-hai", "uruk-hai", "uruk_hai", "urukhai", "uruk");
        add(aliases, "wraith", "Ringwraith", "ringwraith", "ringwraiths", "wraith", "wraiths");
        // the Hobbit draft cultures
        add(aliases, "esgaroth", "Esgaroth", "esgaroth");
        add(aliases, "gundabad", "Gundabad", "gundabad");
        add(aliases, "mirkwood", "Mirkwood", "mirkwood");
        add(aliases, "smaug", "Smaug", "smaug");
        add(aliases, "spider", "Spider", "spider", "spiders");
        add(aliases, "troll", "Troll", "troll", "trolls");
        ALIASES = Collections.unmodifiableMap(aliases);

        Map<String, Icon> twilight = new LinkedHashMap<>();
        for (String cost : List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "X")) {
            String code = "twilight_" + cost.toLowerCase(Locale.ROOT);
            twilight.put(cost, new Icon(code, "(" + cost + ") twilight", code + ".svg"));
        }
        TWILIGHT = Collections.unmodifiableMap(twilight);
    }

    private static void add(Map<String, Icon> aliases, String code, String name, String... names) {
        Icon icon = new Icon(code, name);
        for (String alias : names)
            aliases.put(alias, icon);
    }

    /**
     * {@code :name:} (not right after a letter, digit, {@code /} or {@code \}, nor right before a letter or digit) or
     * {@code [name]} (not part of {@code [[..]]}, {@code ![..]}, {@code [..][..]}, {@code [..](..)} or a
     * {@code [..]:} definition).
     */
    public static final Pattern TOKEN = Pattern.compile(
            "(?<![\\p{L}\\p{N}_/\\\\]):([A-Za-z][A-Za-z_-]*[A-Za-z]):(?![\\p{L}\\p{N}_])"
                    + "|(?<![\\p{L}\\p{N}_/\\\\\\[\\]!])\\[([A-Za-z][A-Za-z_-]*[A-Za-z])](?![\\[(:\\]])");

    /**
     * {@code (1)} / {@code (X)} (not right after a letter, digit, {@code ]} or {@code \}, nor right before a letter or
     * digit) or {@code :1twilight:} / {@code :xtwilight:} (any case; not right after a letter, digit, {@code /} or
     * {@code \}, nor right before a letter or digit).
     */
    public static final Pattern TWILIGHT_TOKEN = Pattern.compile(
            "(?<![\\p{L}\\p{N}_\\]\\\\])\\(([0-9X])\\)(?![\\p{L}\\p{N}_])"
                    + "|(?<![\\p{L}\\p{N}_/\\\\]):([0-9xX])(?i:twilight):(?![\\p{L}\\p{N}_])");

    private CultureIcons() {
    }

    /** @return the culture a name (any case, or an alias) stands for, or null */
    public static Icon icon(String name) {
        return name == null ? null : ALIASES.get(name.toLowerCase(Locale.ROOT));
    }

    /** @return the icon behind a code ({@link Icon#code()}: a culture's or {@code twilight_1}), or null */
    public static Icon byCode(String code) {
        if (code != null && code.startsWith("twilight_")) {
            String cost = code.substring("twilight_".length()).toUpperCase(Locale.ROOT);
            Icon icon = TWILIGHT.get(cost);
            return icon != null && icon.code().equals(code) ? icon : null;
        }
        return icon(code);
    }

    /** The tokens in a piece of text that name a culture or a twilight cost, outside {@code [[card links]]}; in order. */
    public static List<Match> find(String text) {
        List<Match> matches = new ArrayList<>();
        if (text == null || (text.indexOf(':') < 0 && text.indexOf('[') < 0 && text.indexOf('(') < 0))
            return matches;
        List<int[]> cardLinks = new ArrayList<>();
        Matcher card = PatchNoteRenderer.CARD_LINK.matcher(text);
        while (card.find())
            cardLinks.add(new int[]{card.start(), card.end()});
        Matcher matcher = TOKEN.matcher(text);
        while (matcher.find()) {
            Icon icon = icon(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (icon == null || inside(cardLinks, matcher.start(), matcher.end()))
                continue;
            matches.add(new Match(matcher.start(), matcher.end(), icon));
        }
        Matcher twilight = TWILIGHT_TOKEN.matcher(text);
        boolean added = false;
        while (twilight.find()) {
            String cost = twilight.group(1) != null ? twilight.group(1) : twilight.group(2).toUpperCase(Locale.ROOT);
            if (inside(cardLinks, twilight.start(), twilight.end()) || overlaps(matches, twilight.start(), twilight.end()))
                continue;
            matches.add(new Match(twilight.start(), twilight.end(), TWILIGHT.get(cost)));
            added = true;
        }
        if (added)
            matches.sort((a, b) -> Integer.compare(a.start(), b.start()));
        return matches;
    }

    private static boolean overlaps(List<Match> matches, int start, int end) {
        for (Match match : matches) {
            if (start < match.end() && end > match.start())
                return true;
        }
        return false;
    }

    private static boolean inside(List<int[]> ranges, int start, int end) {
        for (int[] range : ranges) {
            if (start < range[1] && end > range[0])
                return true;
        }
        return false;
    }

    /** The icon as the Patch Notes page shows it (patchNotes.css {@code .patchnote-culture}). */
    public static String html(Icon icon) {
        String name = escape(icon.name());
        return "<img class=\"patchnote-culture\" src=\"" + icon.src() + "\" alt=\"" + name + "\" title=\"" + name + "\">";
    }

    /**
     * Plain text (a note's summary) as HTML: escaped, with its culture tokens as icons; null when it has no tokens
     * (the page then shows the text as it is).
     */
    public static String plainTextHtml(String text) {
        List<Match> matches = find(text);
        if (matches.isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        int done = 0;
        for (Match match : matches) {
            sb.append(escape(text.substring(done, match.start()))).append(html(match.icon()));
            done = match.end();
        }
        return sb.append(escape(text.substring(done))).toString();
    }

    /** Plain text with its culture tokens as the cultures' names (for text-only places: share previews). */
    public static String plainText(String text) {
        List<Match> matches = find(text);
        if (matches.isEmpty())
            return text;
        StringBuilder sb = new StringBuilder();
        int done = 0;
        for (Match match : matches) {
            sb.append(text, done, match.start()).append(match.icon().name());
            done = match.end();
        }
        return sb.append(text.substring(done)).toString();
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
