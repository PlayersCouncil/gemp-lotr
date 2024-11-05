package com.gempukku.lotro.common;

import java.text.Normalizer;

public class GameText {

    public static String ConvertTextToHTML(String text) {
        String html = GameText.ConvertNanDeckNotation(text);
        html = GameText.ConvertAnnotatedHTMLToFormattedHTML(html);
        html = GameText.ConvertPlainQuotesToSmartQuotes(html);
        html = Names.AddAccentsToPlainText(html);

        return html;
    }
    public static String ConvertNanDeckNotation(String text) {
        return text
                //Replacing a single backslash and other line breaks
                .replaceAll("\\\\", "<br>")
                .replaceAll("\n", "<br>")
                .replaceAll("\\n", "<br>")
                //non-breaking space
                .replaceAll("_", "&nbsp;")
                //em dash helper
                .replaceAll("---", "—")
                //en dash helper
                .replaceAll("--", "–")
                //en dash instead of hyphen in front of negative numbers
                .replaceAll("-(\\d)", "–$1");
    }

    public static String ConvertAnnotatedHTMLToFormattedHTML(String text) {
        return text
                .replaceAll("<helper>", "<i>")
                .replaceAll("</helper>", "</i>")
                .replaceAll("<keyword>", "<b>")
                .replaceAll("</keyword>", "</b>")
                .replaceAll("<phase>", "<b>")
                .replaceAll("</phase>", "</b>")
                .replaceAll("<br/>", "<br>");
                //TODO: symbol replacement
    }

    public static String ConvertPlainQuotesToSmartQuotes(String text) {

        return text
                //Quote characters at the beginning and end of the string are unambiguous
                .replaceAll("^'", "‘")
                .replaceAll("'$", "’")
                .replaceAll("^\"", "“")
                .replaceAll("\"$", "”")
                //If a space is on one side, also unambiguous
                .replaceAll(" '", " ‘")
                .replaceAll("' ", "’ ")
                .replaceAll(" \"", " “")
                .replaceAll("\" ", "” ")
                //When next to punctuation, it must be an end quote
                .replaceAll("([.,?!/])'", "$1’")
                .replaceAll("([.,?!/])\"", "$1”")
                //If we've figured out one of the smart quotes and it's right next
                // to a dumb quote, we can use that to figure out the other
                .replaceAll("\"‘", "“‘")
                .replaceAll("\"’", "”’")
                .replaceAll("‘\"", "‘“")
                .replaceAll("’\"", "’”")
                .replaceAll("'“", "‘“")
                .replaceAll("'”", "’”")
                .replaceAll("“'", "“‘")
                .replaceAll("”'", "”’")
                //Possessives (the s' case is already handled above)
                .replaceAll("'s", "’s")
                //If any pair of dumb quotes has evaded the above, we will assume they
                // are open or closed depending on order
                .replaceAll("\"'", "“‘")
                .replaceAll("'\"", "’”")
                //Any remaining single quotes must be in a contraction like I'm
                .replaceAll("'", "’")
                //Any remaining double quotes are just asking for it
                .replaceAll("\"", "“");
    }

    public static String ConvertSmartQuotesToPlainQuotes(String text) {
        return text
            .replaceAll("’", "'")
            .replaceAll("‘", "'")
            .replaceAll("”", "\"")
            .replaceAll("“", "\"");
    }

    public static String SanitizeHTMLToSearchText(String text) {
        String output = Normalizer.normalize(text, Normalizer.Form.NFD)
                //Removes any unicode "combining mark", aka diacritics
                .replaceAll("\\p{M}", "");
        output = ConvertSmartQuotesToPlainQuotes(output);

        return output
                .replaceAll("</?b>", "")
                .replaceAll("</?i>", "")
                .replaceAll("\\s*<br/?>\\s*", " ");
    }
}
