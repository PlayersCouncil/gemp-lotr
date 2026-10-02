package com.gempukku.lotro.scenario;

import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * A campaign: scenarios meant to be played one after another ("Learn to Play").  A campaign file is HJSON, kept in a
 * campaigns/ folder beside the scenarios it lists:
 *
 *     {
 *         id: basics
 *         title: Learn to Play
 *         order: 10
 *         scenarios: [
 *             basics-01-into-the-wild
 *             basics-02-shadow-phase
 *         ]
 *     }
 *
 * For now a campaign does one thing: a scenario that is part of one ends by offering the next.  Nothing is locked
 * until the one before it has been finished.
 *
 * @param id What the campaign is known as.  A player's own campaign is known as "&lt;player name&gt;/&lt;id&gt;".
 * @param order Where it comes among the campaigns: lower first.
 * @param owner The player whose campaign it is; null for one that ships with the game.
 * @param scenarios The ids of its scenarios, in the order they are to be played.  In a player's own campaign these
 *                  are that player's scenarios, already written as "&lt;player name&gt;/&lt;id&gt;".
 */
public record ScenarioCampaign(String id, String title, int order, String owner, List<String> scenarios) {
    /**
     * @param owner The player whose folder the file is in; null for the canonical folder.
     * @throws ScenarioFormatException if the file is not a campaign.
     */
    public static ScenarioCampaign parse(String hjson, String owner) throws ScenarioFormatException {
        JsonValue root;
        try {
            root = JsonValue.readHjson(hjson);
        } catch (RuntimeException exp) {
            throw new ScenarioFormatException("Not valid HJSON: " + exp.getMessage(), exp);
        }
        if (!root.isObject())
            throw new ScenarioFormatException("A campaign file has to be one { object }");
        JsonObject object = root.asObject();
        for (String name : object.names())
            if (!List.of("id", "title", "order", "unlock", "summary", "scenarios").contains(name))
                throw new ScenarioFormatException("campaign: unrecognized field '" + name + "' (known fields: id, title, order, summary, unlock, scenarios)");

        String id = string(object, "id");
        if (id == null || !id.matches("[a-z0-9][a-z0-9-]*"))
            throw new ScenarioFormatException("campaign.id: has to be lowercase letters, digits and dashes");
        String title = string(object, "title");
        if (title == null)
            throw new ScenarioFormatException("campaign.title: is missing");
        int order = 0;
        if (object.get("order") != null) {
            try {
                order = Integer.parseInt(string(object, "order"));
            } catch (NumberFormatException exp) {
                throw new ScenarioFormatException("campaign.order: has to be a whole number");
            }
        }
        JsonValue list = object.get("scenarios");
        if (list == null || !list.isArray())
            throw new ScenarioFormatException("campaign.scenarios: has to be a [list] of scenario ids");
        List<String> scenarios = new ArrayList<>();
        for (JsonValue value : list.asArray()) {
            String scenario = value.isString() ? value.asString().trim() : value.toString();
            if (scenario.isEmpty())
                continue;
            scenario = owner == null ? scenario : owner + "/" + scenario;
            if (scenarios.contains(scenario))
                throw new ScenarioFormatException("campaign.scenarios: '" + scenario + "' is listed twice");
            scenarios.add(scenario);
        }
        return new ScenarioCampaign(owner == null ? id : owner + "/" + id, title, order, owner, List.copyOf(scenarios));
    }

    private static String string(JsonObject object, String name) {
        JsonValue value = object.get(name);
        if (value == null || value.isNull())
            return null;
        return value.isString() ? value.asString().trim() : value.toString();
    }
}
