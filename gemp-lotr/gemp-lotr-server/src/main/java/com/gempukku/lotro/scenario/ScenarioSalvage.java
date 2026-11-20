package com.gempukku.lotro.scenario;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a scenario file that has mistakes in it, by leaving out what is mistaken.
 *
 * A scenario with a mistake in it cannot be played, and the parser stops at the first one it meets.  Whoever is
 * writing the scenario still has to be able to open it, though: a file edited by hand that names a card it no longer
 * declares is one line away from working, and the authoring tool is where it is put right.  So the file is read over
 * and over, each time taking out the part the parser (or the validator) complained about, until what is left can be
 * read.  Everything taken out is kept, as it was written, to be shown to the author.
 *
 * What is left is for the authoring tool only.  It is never offered to players: the file still has its mistakes.
 */
public final class ScenarioSalvage {
    private ScenarioSalvage() { }

    /** More mistakes than this and the file is not one that leaving things out will mend. */
    private static final int MAX_ROUNDS = 60;
    private static final int MAX_SHOWN = 1500;

    /** Top-level parts a scenario cannot be without. */
    private static final Set<String> NEEDED = Set.of("id", "title", "format", "cards", "sites", "ringbearer", "steps");
    /** Parts that are words for people to read: a card's name in one of them is taken out with the whole part. */
    private static final Set<String> PROSE = Set.of("text", "header", "note", "title", "summary", "message", "losemessage", "otherwise");

    private static final Pattern STEP = Pattern.compile("^step (\\d+)(?: \\([^)]*\\))?");
    private static final Pattern NO_SUCH_CARD = Pattern.compile("there is no card called '([A-Za-z0-9_-]+)'");
    private static final Pattern UNKNOWN_FIELD = Pattern.compile("unrecognized field '([^']+)'");
    private static final Pattern BAD_CARD = Pattern.compile("^cards: '([A-Za-z0-9_-]+)'");

    /**
     * Something that was left out.
     * @param where The part of the file it was in, as the message says it ("setup.shadow.hand", "step 3.do"); steps
     *              are numbered as the file has them.
     * @param message What was wrong with it.
     * @param was What was taken out, as it was written.
     * @param stepInFile The step it was in, as the file numbers them; 0 if it was in none.
     * @param step That step's number in what was read; 0 if it was in none, or the whole step was left out.
     */
    public record LeftOut(String where, String message, String was, int stepInFile, int step) { }

    /**
     * @param def What could be read; null if the file cannot be read even with its mistakes left out.
     * @param leftOut What was left out to read it, in the order it was found.
     * @param errors Every mistake found, in the order it was found: with 'def' null, the last is the one that could
     *               not be got around.
     */
    public record Result(ScenarioDefinition def, List<LeftOut> leftOut, List<String> errors) { }

    /** Whatever is wrong with a scenario that the parser does not see (ScenarioValidator, the format). */
    public interface Checker {
        List<String> check(ScenarioDefinition def);
    }

    private record Hop(JsonValue parent, String name, int index, JsonValue node) { }

    private record Taken(String where, String message, String was, int stepInFile) { }

    public static Result read(String hjson, Checker checker) {
        JsonObject root;
        try {
            JsonValue parsed = JsonValue.readHjson(hjson);
            if (!parsed.isObject())
                return new Result(null, List.of(), List.of("A scenario file has to be one { object }"));
            root = parsed.asObject();
        } catch (RuntimeException exp) {
            //(the parser says this at more length, and is asked first by everything that reads a file)
            return new Result(null, List.of(), List.of("Not valid HJSON: " + exp.getMessage()));
        }

        //The number each step has in the file, as steps are taken out from among them
        List<Integer> origin = new ArrayList<>();
        JsonValue steps = root.get("steps");
        for (int i = 0; steps != null && steps.isArray() && i < steps.asArray().size(); i++)
            origin.add(i + 1);

        List<Taken> taken = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int round = 0; round < MAX_ROUNDS; round++) {
            ScenarioDefinition def = null;
            String problem;
            try {
                def = ScenarioParser.parse(root.toString());
                List<String> problems = checker.check(def);
                problem = problems.isEmpty() ? null : problems.getFirst();
            } catch (ScenarioFormatException exp) {
                problem = exp.getMessage();
            }
            if (problem == null) {
                List<LeftOut> leftOut = new ArrayList<>();
                for (Taken one : taken)
                    leftOut.add(new LeftOut(one.where(), one.message(), one.was(), one.stepInFile(),
                            one.stepInFile() == 0 ? 0 : origin.indexOf(one.stepInFile()) + 1));
                return new Result(def, List.copyOf(leftOut), List.copyOf(errors));
            }

            String told = renumbered(problem, origin);
            errors.add(told);
            int colon = problem.indexOf(": ");
            String context = colon < 0 ? problem : problem.substring(0, colon);
            Matcher step = STEP.matcher(context);
            int stepInFile = step.find() && inRange(step.group(1), origin) ? origin.get(Integer.parseInt(step.group(1)) - 1) : 0;
            String was = takeOut(root, context, problem, origin);
            if (was == null)
                return new Result(null, List.of(), List.copyOf(errors));
            taken.add(new Taken(renumbered(context, stepInFile), colon < 0 ? told : told.substring(told.indexOf(": ") + 2), was, stepInFile));
        }
        errors.add("There are more than " + MAX_ROUNDS + " mistakes in the file; it has to be put right by hand.");
        return new Result(null, List.of(), List.copyOf(errors));
    }

    private static boolean inRange(String number, List<Integer> origin) {
        try {
            int n = Integer.parseInt(number);
            return n >= 1 && n <= origin.size();
        } catch (NumberFormatException exp) {
            return false;
        }
    }

    /** A message about "step 4" of what is left, as a message about the step the file has in that place. */
    private static String renumbered(String message, List<Integer> origin) {
        Matcher step = STEP.matcher(message);
        if (!step.find() || !inRange(step.group(1), origin))
            return message;
        return renumbered(message, origin.get(Integer.parseInt(step.group(1)) - 1));
    }

    private static String renumbered(String message, int stepInFile) {
        Matcher step = STEP.matcher(message);
        if (stepInFile == 0 || !step.find())
            return message;
        return message.substring(0, step.start(1)) + stepInFile + message.substring(step.end(1));
    }

    /**
     * Takes the part a complaint is about out of the file.
     * @return What was taken out, as it was written; null if nothing can be taken out that would help.
     */
    private static String takeOut(JsonObject root, String context, String problem, List<Integer> origin) {
        //A card whose blueprint is not one: the card goes, and whatever names it follows in the rounds after
        Matcher badCard = BAD_CARD.matcher(problem);
        if (badCard.find() && root.get("cards") != null && root.get("cards").isObject()) {
            for (JsonObject.Member seat : root.get("cards").asObject()) {
                if (!seat.getValue().isObject() || seat.getValue().asObject().get(badCard.group(1)) == null)
                    continue;
                String was = badCard.group(1) + ": " + shown(seat.getValue().asObject().get(badCard.group(1)));
                seat.getValue().asObject().remove(badCard.group(1));
                return was;
            }
            return null;
        }

        List<Hop> path = locate(root, context);
        if (path == null)
            return null;
        JsonValue node = path.isEmpty() ? root : path.getLast().node();

        //A part by a name nothing knows: that part goes, wherever it is (the top of the file among them)
        Matcher unknown = UNKNOWN_FIELD.matcher(problem);
        if (unknown.find() && node.isObject() && node.asObject().get(unknown.group(1)) != null) {
            String was = unknown.group(1) + ": " + shown(node.asObject().get(unknown.group(1)));
            node.asObject().remove(unknown.group(1));
            return was;
        }

        if (path.isEmpty())
            return null;
        Hop last = path.getLast();
        if (last.parent() == root && NEEDED.contains(last.name().toLowerCase(Locale.ROOT)))
            return null;
        String was = shown(node);

        //A card that is not declared: only what names it goes, and the rest of a list stays
        Matcher missing = NO_SUCH_CARD.matcher(problem);
        if (missing.find() && (last.name() == null || !PROSE.contains(last.name().toLowerCase(Locale.ROOT)))) {
            JsonValue rest = without(node, missing.group(1));
            if (rest != null && !rest.toString().equals(node.toString())) {
                if (last.name() != null)
                    last.parent().asObject().set(last.name(), rest);
                else
                    last.parent().asArray().set(last.index(), rest);
                return was;
            }
        }

        if (last.name() != null) {
            last.parent().asObject().remove(last.name());
        } else {
            last.parent().asArray().remove(last.index());
            if (last.parent() == root.get("steps") && last.index() < origin.size())
                origin.remove(last.index());
        }
        return was;
    }

    private static String shown(JsonValue value) {
        String text = value.isString() ? value.asString() : value.toString(Stringify.HJSON);
        return text.length() > MAX_SHOWN ? text.substring(0, MAX_SHOWN) + " ..." : text;
    }

    /**
     * Finds the part of the file a complaint's context names, as far as it can be followed: "setup.shadow.hand",
     * "step 3 (name).messages[2].highlight", "scenario.title".
     * @return The way down to it from the top (empty for the top itself); null if it names a step there is not.
     */
    private static List<Hop> locate(JsonObject root, String context) {
        List<Hop> path = new ArrayList<>();
        JsonValue node = root;
        String rest = context;
        Matcher step = STEP.matcher(context);
        if (step.find()) {
            JsonValue steps = root.get("steps");
            int number = Integer.parseInt(step.group(1));
            if (steps == null || !steps.isArray() || number < 1 || number > steps.asArray().size())
                return null;
            path.add(new Hop(root, "steps", -1, steps));
            node = steps.asArray().get(number - 1);
            path.add(new Hop(steps, null, number - 1, node));
            rest = context.substring(step.end());
            if (rest.startsWith("."))
                rest = rest.substring(1);
        } else if (rest.equals("scenario")) {
            rest = "";
        } else if (rest.startsWith("scenario.")) {
            rest = rest.substring("scenario.".length());
        }

        following:
        for (String segment : rest.isEmpty() ? new String[0] : rest.split("\\.")) {
            int bracket = segment.indexOf('[');
            String name = bracket < 0 ? segment : segment.substring(0, bracket);
            if (!name.isEmpty()) {
                if (!node.isObject())
                    break;
                String actual = null;
                for (String candidate : node.asObject().names())
                    if (candidate.equalsIgnoreCase(name))
                        actual = candidate;
                if (actual == null)
                    break;
                path.add(new Hop(node, actual, -1, node.asObject().get(actual)));
                node = node.asObject().get(actual);
            }
            Matcher index = Pattern.compile("\\[(\\d+)]").matcher(bracket < 0 ? "" : segment.substring(bracket));
            while (index.find()) {
                int number = Integer.parseInt(index.group(1));
                if (node.isArray()) {
                    if (number < 1 || number > node.asArray().size())
                        break following;
                    path.add(new Hop(node, null, number - 1, node.asArray().get(number - 1)));
                    node = node.asArray().get(number - 1);
                } else if (number != 1) {
                    //(one thing written without a [list] round it is the first of one)
                    break following;
                }
            }
        }
        return path;
    }

    /**
     * @return The value with everything that names the card taken out of it; null if that leaves nothing.
     */
    private static JsonValue without(JsonValue value, String alias) {
        Pattern named = Pattern.compile("(?<![A-Za-z0-9_-])" + Pattern.quote(alias) + "(?![A-Za-z0-9_-])");
        if (value.isString()) {
            List<String> terms;
            try {
                terms = Terms.split(value.asString(), ',', "");
            } catch (ScenarioFormatException exp) {
                return value;
            }
            List<String> kept = new ArrayList<>();
            boolean changed = false;
            for (String term : terms) {
                if (term.isEmpty())
                    continue;
                if (named.matcher(term).find())
                    changed = true;
                else
                    kept.add(term);
            }
            if (!changed)
                return value;
            return kept.isEmpty() ? null : JsonValue.valueOf(String.join(", ", kept));
        }
        if (value.isArray()) {
            JsonArray kept = new JsonArray();
            for (JsonValue entry : value.asArray()) {
                JsonValue rest = without(entry, alias);
                if (rest != null)
                    kept.add(rest);
            }
            return kept.isEmpty() ? null : kept;
        }
        if (value.isObject()) {
            JsonObject kept = new JsonObject();
            for (JsonObject.Member member : value.asObject()) {
                if (member.getName().equals(alias))
                    continue;
                //(a message's words may well have the card's name among them, as a word)
                JsonValue rest = PROSE.contains(member.getName().toLowerCase(Locale.ROOT)) ? member.getValue() : without(member.getValue(), alias);
                if (rest != null)
                    kept.add(member.getName(), rest);
            }
            return kept.isEmpty() ? null : kept;
        }
        return value;
    }
}
