package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.scenario.ScenarioDefinition.CardDef;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Checks a parsed scenario against the card library: that its cards exist and are the kinds of card they are used
 * as, and that the card-definition fragments it borrows (rules, requirements) are ones the card builder understands.
 *
 * This still does not play the scenario; ScenarioRunner does that.  What it catches, it catches with a message that
 * points at the scenario file rather than at wherever the game eventually fell over.
 */
public final class ScenarioValidator {
    private ScenarioValidator() { }

    /**
     * @return Everything wrong with the scenario, in the order it appears in the file.  Empty if nothing is.
     */
    public static List<String> validate(ScenarioDefinition def, LotroCardBlueprintLibrary library) {
        List<String> problems = new ArrayList<>();

        for (CardDef card : def.cards.values()) {
            LotroCardBlueprint blueprint = blueprint(library, card.blueprintId(), "cards: '" + card.alias() + "'", problems);
            if (blueprint != null && blueprint.getCardType() == CardType.SITE)
                problems.add("cards: '" + card.alias() + "' is a site.  Sites go under 'sites', not in a deck");
        }

        for (Seat seat : Seat.values()) {
            String ringBearer = def.ringBearer.get(seat);
            LotroCardBlueprint blueprint = blueprintOrNull(library, def.cards.get(ringBearer).blueprintId());
            if (blueprint != null && blueprint.getCardType() != CardType.COMPANION)
                problems.add("ringBearer." + seat.key() + ": '" + ringBearer + "' is not a companion");

            String ring = def.ring.get(seat);
            if (ring != null) {
                blueprint = blueprintOrNull(library, def.cards.get(ring).blueprintId());
                if (blueprint != null && blueprint.getCardType() != CardType.THE_ONE_RING)
                    problems.add("ring." + seat.key() + ": '" + ring + "' is not The One Ring");
            }

            List<String> sites = def.sites.get(seat);
            for (int i = 0; i < sites.size(); i++) {
                blueprint = blueprint(library, sites.get(i), "sites." + seat.key(), problems);
                if (blueprint != null && blueprint.getCardType() != CardType.SITE)
                    problems.add("sites." + seat.key() + ": " + sites.get(i) + " is not a site");
            }
        }

        for (Map.Entry<Seat, String> rules : def.rules.entrySet()) {
            try {
                ScenarioTable.checkRules(rules.getValue());
            } catch (IllegalArgumentException exp) {
                problems.add("rules." + rules.getKey().key() + ": " + exp.getMessage());
            }
        }

        start(def, library, problems);

        for (Step step : def.allSteps()) {
            String context = step.context();
            if (step.when.opening && step != def.steps.getFirst())
                problems.add(context + ".when: the opening comes before the first turn, so only the first step can wait for it");
            requirements(step.when.require, context + ".when.require", problems);
            if (step.until != null)
                requirements(step.until.require, context + ".until.require", problems);
            if (step.skipWhen != null)
                requirements(step.skipWhen.require, context + ".skipWhen.require", problems);
            if (step.allow != null)
                requirements(step.allow.passWhen, context + ".allow.passWhen", problems);
        }
        for (Step step : def.allSteps())
            for (int i = 0; i < step.messages.size(); i++) {
                ScenarioDefinition.Message message = step.messages.get(i);
                //(said as the parser says where a message is, so that whatever reads the two can find it)
                String context = step.context() + ".messages[" + (i + 1) + "]";
                if (message.zoom != null) {
                    String problem = CardFaces.check(def, message.zoom, library);
                    if (problem != null)
                        problems.add(context + ".zoom: " + problem);
                }
                List<String> anchors = new ArrayList<>(message.highlight);
                anchors.addAll(message.avoid);
                anchors.addAll(message.anchor);
                for (String anchor : anchors) {
                    String part = Anchors.printedPart(anchor);
                    String problem = part == null ? null : CardFaces.checkPart(def, Anchors.cardAlias(anchor), part, library);
                    if (problem != null)
                        problems.add(context + ": " + anchor + ": " + problem);
                }
            }
        for (int i = 0; i < def.orders.size(); i++)
            requirements(def.orders.get(i).when().require, "opponent.orders[" + (i + 1) + "].when.require", problems);
        requirements(def.outcome.win, "outcome.win", problems);
        requirements(def.outcome.lose, "outcome.lose", problems);

        return problems;
    }

    /**
     * Whether where the story has got to when the game begins (setup.startAt and what goes with it) is a place a
     * game can be: the path reaches the markers, the phase fits how far the fellowship has come this turn, the
     * skirmishes are between cards that are on the table on their sides, and the sites controlled are behind every
     * marker.
     */
    private static void start(ScenarioDefinition def, LotroCardBlueprintLibrary library, List<String> problems) {
        ScenarioDefinition.Setup setup = def.setup;
        int opponentSite = setup.opponentSite(def);

        if (def.markersTogether && !def.oneSided)
            problems.add("rules.markers: 'together' is for a one-sided game (rules.oneSided: true), where the Shadow seat never "
                    + "takes a turn to move its own marker");

        //The path as it is laid out when the game begins, site 1 first: each site's name in the scenario, or null
        // where there is none to lay
        List<String> path = path(def, library, Math.max(setup.site, opponentSite), problems);

        if (setup.phase == Phase.FELLOWSHIP && setup.moves() > 0)
            problems.add("setup.startAt.moves: in the Fellowship phase the fellowship has not moved yet this turn (its move ends that phase)");
        if (setup.phase != Phase.FELLOWSHIP && setup.moves() < 1)
            problems.add("setup.startAt.moves: past the Fellowship phase the fellowship has moved at least once this turn (the move ends that phase)");
        if (setup.moves() > setup.site - 1)
            problems.add("setup.startAt: a fellowship at site " + setup.site + " cannot have moved " + setup.moves() + " time(s) this turn: "
                    + "each move is to the next site" + (setup.phase != Phase.FELLOWSHIP && setup.site == 1
                    ? ".  A game that begins past the Fellowship phase begins at site 2 or later (site: 2)" : ""));

        if (!setup.skirmishes.isEmpty() && setup.phase != Phase.ASSIGNMENT && setup.phase != Phase.SKIRMISH)
            problems.add("setup.skirmishes: skirmishes are assigned in the Assignment phase and fought in the Skirmish phase; "
                    + "a game that begins in the " + setup.phase.name().toLowerCase() + " phase has none (startAt.phase: skirmish)");
        if (setup.skirmishes.isEmpty() && setup.phase == Phase.SKIRMISH)
            problems.add("setup.startAt.phase: a game that begins in the Skirmish phase needs its skirmishes (setup.skirmishes: { frodo: runner1 }); "
                    + "with none the game goes straight on to the Regroup phase (startAt.phase: regroup)");
        for (java.util.Map.Entry<String, List<String>> skirmish : setup.skirmishes.entrySet()) {
            String character = skirmish.getKey();
            if (!placed(def, character, Seat.FREEPS, ScenarioDefinition.SetupZone.CHARACTERS))
                problems.add("setup.skirmishes." + character + ": '" + character + "' is not on the table: put it among the Free Peoples "
                        + "characters (setup.freeps.characters)");
            for (String minion : skirmish.getValue())
                if (!placed(def, minion, Seat.SHADOW, ScenarioDefinition.SetupZone.MINIONS))
                    problems.add("setup.skirmishes." + character + ": '" + minion + "' is not on the table: put it among the minions "
                            + "(setup.shadow.minions)");
        }

        int behind = Math.min(setup.site, opponentSite);
        for (java.util.Map.Entry<Seat, List<String>> entry : setup.controlled.entrySet())
            for (String site : entry.getValue()) {
                String context = "setup.controlled." + entry.getKey().key();
                int number = site.matches("\\d+") ? Integer.parseInt(site) : path.indexOf(site) + 1;
                if (number == 0) {
                    problems.add(context + ": '" + site + "' is not on the adventure path when the game begins (the path is laid "
                            + "out to the markers, from the Free Peoples seat's sites unless setup.path says otherwise)");
                    continue;
                }
                if (number >= behind)
                    problems.add(context + ": site " + number + " cannot be controlled when the game begins: a site is controlled once every "
                            + "marker is past it, and " + (setup.site <= opponentSite ? "the fellowship is at site " + setup.site
                            : "the Shadow seat's marker is at site " + opponentSite + " (setup.startAt.opponentSite"
                            + (def.oneSided ? ", or rules.markers: together" : "") + ")"));
            }
    }

    /**
     * The adventure path as the game begins with it, as far as a site: each site's name in the scenario, site 1
     * first (see ScenarioTable.layPath).  What cannot be laid is said.
     */
    private static List<String> path(ScenarioDefinition def, LotroCardBlueprintLibrary library, int last, List<String> problems) {
        List<String> freepsSites = def.sites.getOrDefault(Seat.FREEPS, List.of());
        List<String> freepsAliases = def.siteAliases.getOrDefault(Seat.FREEPS, List.of());
        boolean unordered = !freepsSites.isEmpty() && siteNumber(library, freepsSites.getFirst()) == 0;
        List<String> path = new ArrayList<>();
        java.util.Set<String> used = new java.util.HashSet<>(def.setup.path);
        for (int number = 1; number <= Math.max(last, def.setup.path.size()); number++) {
            String alias = number <= def.setup.path.size() ? def.setup.path.get(number - 1) : null;
            if (alias != null) {
                ScenarioDefinition.CardDef site = def.siteCards.get(alias);
                int printed = siteNumber(library, site.blueprintId());
                if (printed != 0 && printed != number && printed != -1)
                    problems.add("setup.path: '" + alias + "' is site " + printed + ", so it cannot be site " + number + " of the path");
            } else {
                for (int at = 0; at < freepsSites.size() && alias == null; at++) {
                    String candidate = freepsAliases.get(at);
                    if (used.contains(candidate))
                        continue;
                    if (unordered || siteNumber(library, freepsSites.get(at)) == number)
                        alias = candidate;
                }
                if (alias == null) {
                    problems.add("setup.startAt: the path cannot be laid out to site " + last + ": the Free Peoples seat's sites have no site "
                            + number + " (list one under sites.freeps, or say whose site " + number + " is in setup.path)");
                    return path;
                }
                used.add(alias);
            }
            path.add(alias);
        }
        return path;
    }

    /** A site's printed number: 0 for one of the sites that have none (from Shadows on); -1 if there is no such card. */
    private static int siteNumber(LotroCardBlueprintLibrary library, String blueprintId) {
        LotroCardBlueprint blueprint = blueprintOrNull(library, blueprintId);
        return blueprint == null ? -1 : blueprint.getSiteNumber();
    }

    /** Whether the setup puts one of a seat's cards on the table there (a Ring-bearer is always among the characters). */
    private static boolean placed(ScenarioDefinition def, String alias, Seat seat, ScenarioDefinition.SetupZone zone) {
        if (zone == ScenarioDefinition.SetupZone.CHARACTERS && alias.equals(def.ringBearer.get(seat)))
            return true;
        return def.setup.zones.getOrDefault(seat, java.util.Map.of()).getOrDefault(zone, List.of()).contains(alias);
    }

    private static void requirements(String json, String context, List<String> problems) {
        if (json == null)
            return;
        try {
            ScenarioTable.buildRequirements(json);
        } catch (IllegalArgumentException exp) {
            problems.add(context + ": " + exp.getMessage());
        }
    }

    private static LotroCardBlueprint blueprint(LotroCardBlueprintLibrary library, String blueprintId, String context, List<String> problems) {
        LotroCardBlueprint blueprint = blueprintOrNull(library, blueprintId);
        if (blueprint == null)
            problems.add(context + ": there is no card " + blueprintId);
        return blueprint;
    }

    private static LotroCardBlueprint blueprintOrNull(LotroCardBlueprintLibrary library, String blueprintId) {
        try {
            return library.getLotroCardBlueprint(blueprintId);
        } catch (CardNotFoundException exp) {
            return null;
        }
    }
}
