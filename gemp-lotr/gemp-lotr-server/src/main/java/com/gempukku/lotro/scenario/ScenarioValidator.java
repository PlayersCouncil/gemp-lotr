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

        //Scenarios begin at the start of the first turn for now: reaching a later moment means playing up to it,
        // and nothing yet says how that play should go.
        if (def.setup.turn != 1 || def.setup.phase != Phase.FELLOWSHIP)
            problems.add("setup.startAt: only turn 1, fellowship is supported so far");

        for (Step step : def.steps) {
            String context = "step " + step.label();
            requirements(step.when.require, context + ".when.require", problems);
            if (step.until != null)
                requirements(step.until.require, context + ".until.require", problems);
            if (step.skipWhen != null)
                requirements(step.skipWhen.require, context + ".skipWhen.require", problems);
            if (step.allow != null)
                requirements(step.allow.passWhen, context + ".allow.passWhen", problems);
        }
        requirements(def.outcome.win, "outcome.win", problems);
        requirements(def.outcome.lose, "outcome.lose", problems);

        return problems;
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
