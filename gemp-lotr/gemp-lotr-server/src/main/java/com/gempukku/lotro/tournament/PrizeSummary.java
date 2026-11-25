package com.gempukku.lotro.tournament;

import java.util.List;

/**
 * What a tournament's prizes are, as data for a client to lay out: a short label (what the hall's Prizes cell shows,
 * "Prize Breakdown" or "No prizes") and the breakdown behind it as plain-text lines (empty when there is none).
 * Neither holds any HTML.
 * <p>
 * This is the single source of the prize description: the HTML snippet the legacy hall reads
 * ({@link #toLegacyHtml()}) is derived from it, so the two cannot drift apart.  Ketura's direction for the hall port:
 * the server sends data, and the old markup stays beside it only for the legacy pages.
 */
public record PrizeSummary(String label, List<String> lines) {
    public PrizeSummary {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public static PrizeSummary of(String label, String... lines) {
        return new PrizeSummary(label, List.of(lines));
    }

    /**
     * The description as the legacy hall reads it: the label alone when there is no breakdown, otherwise
     * {@code <div class='prizeHint' value='<ul><li>...</li></ul>'>label</div>}, laid out exactly as the old
     * hand-written strings were (one {@code <li>} per line, indented by four spaces).  Text is escaped, so a line or
     * label with markup characters in it is shown as text; none of today's prizes has any.
     */
    public String toLegacyHtml() {
        if (lines.isEmpty())
            return label;

        StringBuilder list = new StringBuilder("<ul>\n");
        for (String line : lines)
            list.append("    <li>").append(escapeHtml(line)).append("</li>\n");
        list.append("</ul>");

        // The list sits in a single-quoted attribute, so it is escaped once more for that.
        String value = list.toString().replace("&", "&amp;").replace("'", "&#39;");
        return "<div class='prizeHint' value='" + value + "'>" + escapeHtml(label) + "</div>";
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }
}
