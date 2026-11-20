package com.gempukku.lotro.scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Scenario files borrow the card definitions' habit of writing a list of things on one line, separated by commas,
 * with anything that needs a parameter written like a function call:
 *
 *     when: freeps, action, phase(fellowship), turn(2)
 *     answer: play(gandalf), choose(frodo), pass
 *
 * This splits such a line into its terms.  Commas inside parentheses belong to the term they are in.
 */
public final class Terms {
    private Terms() { }

    /**
     * One term: a name, and the parameters inside its parentheses (if it had any).
     */
    public record Term(String name, List<String> args, String raw) {
        public boolean is(String candidate) {
            return name.equalsIgnoreCase(candidate);
        }

        public String arg(int index, String context) throws ScenarioFormatException {
            if (index >= args.size())
                throw new ScenarioFormatException(context + ": '" + raw + "' needs at least " + (index + 1) + " parameter(s)");
            return args.get(index);
        }

        /** Everything inside the parentheses as one string, for terms whose parameter is free text. */
        public String wholeArg() {
            int open = raw.indexOf('(');
            if (open < 0)
                return "";
            return raw.substring(open + 1, raw.lastIndexOf(')')).trim();
        }
    }

    public static List<Term> parse(String text, String context) throws ScenarioFormatException {
        List<Term> result = new ArrayList<>();
        for (String part : split(text, ',', context)) {
            if (part.isEmpty())
                continue;
            int open = part.indexOf('(');
            if (open < 0) {
                result.add(new Term(part, List.of(), part));
            } else {
                if (!part.endsWith(")"))
                    throw new ScenarioFormatException(context + ": '" + part + "' has text after its closing parenthesis");
                String name = part.substring(0, open).trim();
                String inner = part.substring(open + 1, part.length() - 1);
                result.add(new Term(name, split(inner, ',', context), part));
            }
        }
        return result;
    }

    /**
     * Splits on a separator, ignoring separators inside parentheses, and trims each piece.
     */
    public static List<String> split(String text, char separator, String context) throws ScenarioFormatException {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch == '(')
                depth++;
            if (ch == ')') {
                depth--;
                if (depth < 0)
                    throw new ScenarioFormatException(context + ": mismatched closing parenthesis in '" + text + "'");
            }
            if (ch == separator && depth == 0) {
                parts.add(sb.toString().trim());
                sb = new StringBuilder();
            } else {
                sb.append(ch);
            }
        }
        if (depth != 0)
            throw new ScenarioFormatException(context + ": unclosed parenthesis in '" + text + "'");
        String last = sb.toString().trim();
        if (!last.isEmpty() || !parts.isEmpty())
            parts.add(last);
        return parts;
    }
}
