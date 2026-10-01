package com.gempukku.lotro.game.formats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Short, player-facing facts about a format, shared by the Play popup's format (i) (through the format JSON's
 * {@code setSummary}) and Help › Format Definitions.
 */
public final class FormatSummary {
    private FormatSummary() {
    }

    /** Player's Council supplement sets are numbered from here: 100 is V0, 101 is V1, and so on. */
    private static final int FIRST_V_SET = 100;
    private static final int LAST_V_SET = 149;

    /**
     * "Cards from sets 1-10, V1-V3": the format's legal sets, as runs.  Consecutive numbers of three or more collapse
     * to "first-last", a pair stays "1, 2"; the Player's Council supplements (sets 100-149) are written V0, V1, ...
     * and never run into the numbered sets.  One set reads "Cards from set 1"; no sets (or none that parse) gives
     * null.
     */
    public static String describeSets(Collection<String> sets) {
        if (sets == null)
            return null;
        TreeSet<Integer> numbers = new TreeSet<>();
        for (String set : sets) {
            if (set == null)
                continue;
            try {
                numbers.add(Integer.parseInt(set.trim()));
            } catch (NumberFormatException ignored) {
                // a set code that is not a number is left out of the summary
            }
        }
        if (numbers.isEmpty())
            return null;

        List<String> parts = new ArrayList<>();
        Integer runStart = null;
        Integer previous = null;
        for (int number : numbers) {
            if (previous != null && number == previous + 1 && isVSet(number) == isVSet(previous)) {
                previous = number;
                continue;
            }
            if (runStart != null)
                addRun(parts, runStart, previous);
            runStart = number;
            previous = number;
        }
        addRun(parts, runStart, previous);

        return (numbers.size() == 1 ? "Cards from set " : "Cards from sets ") + String.join(", ", parts);
    }

    private static void addRun(List<String> parts, int first, int last) {
        if (last - first >= 2) {
            parts.add(label(first) + "-" + label(last));
        } else {
            for (int number = first; number <= last; number++)
                parts.add(label(number));
        }
    }

    private static boolean isVSet(int number) {
        return number >= FIRST_V_SET && number <= LAST_V_SET;
    }

    private static String label(int number) {
        return isVSet(number) ? "V" + (number - FIRST_V_SET) : String.valueOf(number);
    }

    /** How players write a set number: "1" for set 1, "V1" for set 101 (Player's Council supplements). */
    public static String setLabel(int number) {
        return label(number);
    }

    /**
     * The id of a format's entry on Help › Format Definitions, e.g. "format-pc_fotr_block".  The Play popup links to
     * "#format-&lt;code&gt;"; anything but letters, digits, '_' and '-' becomes '-', so the id is always a valid
     * fragment.
     */
    public static String anchorId(String code) {
        return "format-" + (code == null ? "" : code.replaceAll("[^A-Za-z0-9_-]", "-"));
    }
}
