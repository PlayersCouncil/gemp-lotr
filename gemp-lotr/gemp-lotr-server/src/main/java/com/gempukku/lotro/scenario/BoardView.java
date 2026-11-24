package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.scenario.ScenarioDefinition.SetupZone;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A scenario's live board in words a page can show: each named card and where it is, and where the game stands on
 * the adventure path.  The authoring panel's Board tab (ScenarioAuthoring.describeBoard) and a sandbox's state
 * (ScenarioSandbox.State) both describe the board this way, so that a card looks the same to the one as to the other.
 */
final class BoardView {
    private BoardView() { }

    /** Where each card comes in its hand or its pile (0 first: the top of a pile). */
    static Map<PhysicalCard, Integer> places(ScenarioTable table) {
        Map<PhysicalCard, Integer> places = new IdentityHashMap<>();
        for (Seat seat : Seat.values())
            for (SetupZone zone : ScenarioParser.ORDERED) {
                List<? extends PhysicalCard> there = table.CardsIn(seat, zone);
                for (int i = 0; i < there.size(); i++)
                    places.put(there.get(i), i);
            }
        return places;
    }

    /**
     * One card: {alias, blueprintId, name, type, owner, cardId, zone, at, wounds, tokens, on}.  'tokens' counts the
     * card's own culture's tokens; 'on' is the card it is attached to or stacked on.
     */
    static Map<String, Object> card(ScenarioTable table, String alias, PhysicalCardImpl physical, Seat owner, Map<PhysicalCard, Integer> places) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("alias", alias);
        json.put("blueprintId", physical.getBlueprintId());
        json.put("name", GameUtils.getFullName(physical));
        json.put("type", physical.getBlueprint().getCardType().name());
        json.put("owner", owner.key());
        json.put("cardId", physical.getCardId());
        json.put("zone", zoneOf(physical));
        json.put("at", places.get(physical));
        json.put("wounds", table.gameState().getWounds(physical));
        Token token = physical.getBlueprint().getCulture() == null ? null : Token.findTokenForCulture(physical.getBlueprint().getCulture());
        json.put("tokens", token == null ? 0 : table.gameState().getTokenCount(physical, token));
        json.put("on", physical.getAttachedTo() != null ? table.AliasOf(physical.getAttachedTo())
                : physical.getStackedOn() != null ? table.AliasOf(physical.getStackedOn()) : null);
        //(the modifiers put on it by hand that are in force: see CardModifiers)
        json.put("modifiers", table.DescribeModifiers(physical));
        return json;
    }

    /**
     * Where the game stands on the adventure path: each seat's marker, the sites on the path (and whose each is, and
     * who controls it), how many moves the fellowship has made this turn, and the skirmishes assigned.
     */
    static void where(ScenarioTable table, Map<String, Object> board) {
        Map<String, Object> markers = new LinkedHashMap<>();
        for (Seat seat : Seat.values())
            markers.put(seat.key(), table.gameState().getPlayerPosition(table.Player(seat)));
        board.put("markers", markers);
        board.put("currentSite", table.gameState().getCurrentSiteNumber());
        board.put("moves", table.gameState().getMoveCount());
        List<Map<String, Object>> path = new ArrayList<>();
        for (int number = 1; number <= 9; number++) {
            PhysicalCard site = table.gameState().getSite(number);
            if (site == null)
                continue;
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("number", number);
            json.put("alias", table.AliasOf(site));
            json.put("blueprintId", site.getBlueprintId());
            json.put("name", GameUtils.getFullName(site));
            json.put("printed", site.getBlueprint().getSiteNumber());
            json.put("owner", table.SeatOf(site.getOwner()).key());
            json.put("controlledBy", site.getCardController() == null ? null : table.SeatOf(site.getCardController()).key());
            path.add(json);
        }
        board.put("path", path);
        Map<String, Object> skirmishes = new LinkedHashMap<>();
        for (com.gempukku.lotro.game.state.Assignment assignment : table.gameState().getAssignments()) {
            List<String> minions = new ArrayList<>();
            for (PhysicalCard minion : assignment.getShadowCharacters())
                minions.add(table.AliasOf(minion));
            skirmishes.put(table.AliasOf(assignment.getFellowshipCharacter()), minions);
        }
        board.put("skirmishes", skirmishes);
    }

    /** A card's zone as the page names it. */
    static String zoneOf(PhysicalCard card) {
        if (card.getZone() == null)
            return "nowhere";
        return switch (card.getZone()) {
            case FREE_CHARACTERS -> "characters";
            case SUPPORT -> "support";
            case SHADOW_CHARACTERS -> "minions";
            case HAND -> "hand";
            case DECK -> "deck";
            case DISCARD -> "discard";
            case DEAD -> "deadPile";
            case REMOVED -> "removed";
            case ATTACHED -> "attached";
            case STACKED -> "stacked";
            case ADVENTURE_DECK -> "adventureDeck";
            case ADVENTURE_PATH -> "path";
            default -> card.getZone().name().toLowerCase(Locale.ROOT);
        };
    }
}
