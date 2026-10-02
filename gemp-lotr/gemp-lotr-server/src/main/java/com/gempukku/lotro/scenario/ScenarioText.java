package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.patchnotes.CardLookup;
import com.gempukku.lotro.patchnotes.PatchNoteRenderer;
import com.gempukku.lotro.scenario.ScenarioDefinition.CardDef;
import com.gempukku.lotro.scenario.ScenarioDefinition.Message;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Turns the text of a scenario's messages into what the client shows.  The text is Markdown, written and rendered
 * the same way as a patch note (PatchNoteRenderer): **bold**, culture and twilight icons, and [[card links]] that
 * open the card.  The result is cleaned, so text from a scenario somebody uploaded cannot run anything.
 *
 * A card link in a scenario is looked up among the scenario's own cards first:
 *
 *     [[gandalf]]            a card alias: that card
 *     [[Gandalf]]            a name: the scenario's card of that name ("Gandalf" or "Gandalf, Stormcrow"), if it has
 *                            exactly one; this is how a link finds the right Gandalf out of the dozen in the library
 *     [[1_83]] [[Aragorn, Ranger of the North]]     anything else: as in a patch note, any card in the library
 *
 * The link shows the words that were written, or the words after a bar: [[gandalf|the wizard]].
 */
public final class ScenarioText implements LiveScenario.TextRenderer {
    private final PatchNoteRenderer _renderer;

    /**
     * @param fallback Looks up card links that are not about one of the scenario's own cards; null leaves those as
     *                 plain text.
     */
    public ScenarioText(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, CardLookup fallback) {
        _renderer = new PatchNoteRenderer(reference -> resolve(def, library, format, fallback, reference));
    }

    @Override
    public String render(String text) {
        return render(text, new ArrayList<>());
    }

    /**
     * @param problems Told about each card link that names no card, or more than one.
     */
    public String render(String text, List<String> problems) {
        if (text == null)
            return null;
        return _renderer.render(text, problems::add, true);
    }

    /**
     * @return What is wrong with the card links in a scenario's text; empty if nothing is.
     */
    public List<String> check(ScenarioDefinition def) {
        List<String> problems = new ArrayList<>();
        for (Step step : def.steps) {
            for (Message message : step.messages)
                check(message, step.describe(), problems);
            if (step.end != null)
                for (Message message : step.end.messages)
                    check(message, step.describe() + " end", problems);
            if (step.allow != null) {
                check(step.allow.otherwise, step.describe() + " otherwise", problems);
                for (String hint : step.allow.hints.values())
                    check(hint, step.describe() + " hints", problems);
            }
        }
        check(def.outcome.offScriptMessage, "outcome.offScript.message", problems);
        return problems;
    }

    private void check(Message message, String where, List<String> problems) {
        check(message.header, where, problems);
        check(message.text, where, problems);
    }

    private void check(String text, String where, List<String> problems) {
        if (text == null)
            return;
        List<String> found = new ArrayList<>();
        render(text, found);
        for (String problem : found)
            problems.add(where + ": " + problem);
    }

    private static CardLookup.Result resolve(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format,
                                             CardLookup fallback, String reference) {
        String ref = reference == null ? "" : reference.trim();
        CardDef named = def.cards.get(ref);
        if (named != null)
            return found(library, format.applyErrata(named.blueprintId()));

        String key = key(ref);
        Set<String> matches = new LinkedHashSet<>();
        for (CardDef card : def.cards.values()) {
            String blueprintId = format.applyErrata(card.blueprintId());
            try {
                LotroCardBlueprint blueprint = library.getLotroCardBlueprint(blueprintId);
                if (key.equals(key(blueprint.getFullName())) || key.equals(key(blueprint.getTitle())))
                    matches.add(blueprintId);
            } catch (CardNotFoundException ignored) {
                //The validator reports cards that do not exist
            }
        }
        if (matches.size() == 1)
            return found(library, matches.iterator().next());

        if (fallback == null)
            return CardLookup.Result.failed(matches.isEmpty() ? "the scenario has no card called '" + ref + "'"
                    : "the scenario has " + matches.size() + " different cards called '" + ref + "'; use a card alias");
        return fallback.resolve(ref);
    }

    private static CardLookup.Result found(LotroCardBlueprintLibrary library, String blueprintId) {
        try {
            return CardLookup.Result.found(blueprintId, library.getLotroCardBlueprint(blueprintId).getFullName());
        } catch (CardNotFoundException exp) {
            return CardLookup.Result.failed("there is no card with the id " + blueprintId);
        }
    }

    private static String key(String name) {
        if (name == null)
            return "";
        String plain = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return plain.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "");
    }
}
