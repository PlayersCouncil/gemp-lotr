package com.gempukku.lotro.logic;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameUtils {
    public static Side getSide(LotroGame game, String playerId) {
        return isFP(game, playerId) ? Side.FREE_PEOPLE : Side.SHADOW;
    }

    public static boolean isSide(LotroGame game, Side side, String playerId) {
        if (side == Side.FREE_PEOPLE)
            return game.getGameState().getCurrentPlayerId().equals(playerId);
        else
            return !game.getGameState().getCurrentPlayerId().equals(playerId);
    }

    public static boolean isFP(LotroGame game, String playerId) {
        return game.getGameState().getCurrentPlayerId().equals(playerId);
    }

    public static boolean isShadow(LotroGame game, String playerId) {
        return !game.getGameState().getCurrentPlayerId().equals(playerId);
    }

    public static String getFullName(PhysicalCard card) {
        LotroCardBlueprint blueprint = card.getBlueprint();
        return getFullName(blueprint);
    }

    public static String getFullName(LotroCardBlueprint blueprint) {
        if (blueprint.getSubtitle() != null)
            return blueprint.getTitle() + ", " + blueprint.getSubtitle();
        return blueprint.getTitle();
    }

    public static String getFullSanitizedName(LotroCardBlueprint blueprint) {
        if (blueprint.getSubtitle() != null)
            return blueprint.getSanitizedTitle() + ", " + Names.SanitizeName(blueprint.getSubtitle());
        return blueprint.getSanitizedTitle();
    }

    public static String getFirstShadowPlayer(LotroGame game) {
        if (game.isSolo())
            throw new InvalidSoloAdventureException("Shadow player requested");
        final String fpPlayer = game.getGameState().getCurrentPlayerId();
        final PlayOrder counterClockwisePlayOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(fpPlayer, false);
        // Skip FP player
        counterClockwisePlayOrder.getNextPlayer();
        return counterClockwisePlayOrder.getNextPlayer();
    }

    public static String[] getShadowPlayers(LotroGame game) {
        if (game.isSolo())
            throw new InvalidSoloAdventureException("Shadow player requested");
        final String fpPlayer = game.getGameState().getCurrentPlayerId();
        List<String> shadowPlayers = new LinkedList<>(game.getGameState().getPlayerOrder().getAllPlayers());
        shadowPlayers.remove(fpPlayer);
        return shadowPlayers.toArray(new String[shadowPlayers.size()]);
    }

    public static String getFreePeoplePlayer(LotroGame game) {
        return game.getGameState().getCurrentPlayerId();
    }

    public static String[] getOpponents(LotroGame game, String playerId) {
        if (game.isSolo())
            throw new InvalidSoloAdventureException("Opponent requested");
        List<String> shadowPlayers = new LinkedList<>(game.getGameState().getPlayerOrder().getAllPlayers());
        shadowPlayers.remove(playerId);
        return shadowPlayers.toArray(new String[shadowPlayers.size()]);
    }

    public static String getFirstOpponent(LotroGame game, String playerId) {
        return Arrays.stream(getOpponents(game, playerId)).findFirst().orElse(null);
    }

    public static String[] getAllPlayers(LotroGame game) {
        final GameState gameState = game.getGameState();
        final PlayerOrder playerOrder = gameState.getPlayerOrder();
        String[] result = new String[playerOrder.getPlayerCount()];

        final PlayOrder counterClockwisePlayOrder = playerOrder.getCounterClockwisePlayOrder(gameState.getCurrentPlayerId(), false);
        int index = 0;

        String nextPlayer;
        while ((nextPlayer = counterClockwisePlayOrder.getNextPlayer()) != null) {
            result[index++] = nextPlayer;
        }
        return result;
    }

    public static List<PhysicalCard> getRandomCards(List<? extends PhysicalCard> cards, int count) {
        List<PhysicalCard> randomizedCards = new ArrayList<>(cards);
        Collections.shuffle(randomizedCards, ThreadLocalRandom.current());

        return new LinkedList<>(randomizedCards.subList(0, Math.min(count, randomizedCards.size())));
    }

    public static String s(Collection<PhysicalCard> cards) {
        if (cards.size() > 1)
            return "s";
        return "";
    }

    public static String be(Collection<PhysicalCard> cards) {
        if (cards.size() > 1)
            return "are";
        return "is";
    }

    public static String getCardLink(PhysicalCard card) {
        LotroCardBlueprint blueprint = card.getBlueprint();
        return getCardLink(card.getBlueprintId(), blueprint);
    }

    public static String getCardLink(String blueprintId, LotroCardBlueprint blueprint) {
        return "<div class='cardHint' value='" + blueprintId + "'>" + (blueprint.isUnique() ? "·" : "") + GameUtils.getFullName(blueprint) + "</div>";
    }

    public static String getProductLink(String blueprintId) {
        return "<div class='cardHint' value='" + blueprintId + "'>" + blueprintId + "</div>";
    }

    public static String getDeluxeCardLink(String blueprintId, LotroCardBlueprint blueprint) {
        var culture = blueprint.getCulture();
        var cultureString = "";
        if(culture == null) {
            if (blueprint.getTitle().equals("The One Ring")) {
                cultureString = getCultureImage(culture, "one_ring");
            }
            else {
                cultureString = getCultureImage(culture, "site");
            }
        }
        else {
            cultureString = getCultureImage(culture, null);
        }
        return "<div class='cardHint' value='" + blueprintId + "'>" + cultureString
                + (blueprint.isUnique() ? "·" : "") + " " + GameUtils.getFullName(blueprint) + "</div>";
    }

    public static String getCultureImage(String cultureName) {
        Culture culture = Culture.findCulture(cultureName);
        if(culture == null)
            return null;

        return getCultureImage(culture);
    }

    public static String getCultureImage(Culture culture, String override) {
        if(override == null || override.isEmpty()) {
            override = culture.toString().toLowerCase();
        }
        return "<span class='cultureHint' ><img src='images/cultures/" + override + ".png'></span>";
    }

    public static String getCultureImage(Culture culture) {
        return "<span class='cultureHint' value='" + culture.toString() + "'><img src='images/cultures/" + culture.toString().toLowerCase() + ".png'> "
                + culture.getHumanReadable() + "</span>";
    }

    public static String getAppendedTextNames(Collection<? extends PhysicalCard> cards) {
        StringBuilder sb = new StringBuilder();
        for (PhysicalCard card : cards)
            sb.append(GameUtils.getFullName(card) + ", ");

        if (sb.length() == 0)
            return "none";
        else
            return sb.substring(0, sb.length() - 2);
    }

    public static String getAppendedNames(Collection<? extends PhysicalCard> cards) {
        ArrayList<String> cardStrings = new ArrayList<>();
        for (PhysicalCard card : cards) {
            cardStrings.add(GameUtils.getCardLink(card));
        }

        if (cardStrings.size() == 0)
            return "none";

        return String.join(", ", cardStrings);
    }

    public static String substituteText(String text, ActionContext context)
    {
        String result = text;
        while (result.contains("{")) {
            int startIndex = result.indexOf("{");
            int endIndex = result.indexOf("}");
            String memoryName = result.substring(startIndex + 1, endIndex);
            String memory = memoryName.toLowerCase();
            String found = null;
            String culture = getCultureImage(memory);
            if(culture != null) {
                found = culture;
            }
            else if(context != null){
                if(memory.equals("you")) {
                    found = context.getPerformingPlayer();
                    if(found == null) {
                        found = "nobody";
                    }
                }
                else if(memory.equals("owner")) {
                    var source = context.getSource();
                    if(source != null) {
                        found = source.getOwner();
                    }
                    if(found == null) {
                        found = "nobody";
                    }
                }
                else if(memory.equals("opponent")) {
                    found = getFirstOpponent(context.getGame(), context.getSource().getOwner());
                    if(found == null) {
                        found = "nobody";
                    }
                }
                else if(memory.equals("freeps") || memory.equals("free peoples")) {
                    found = getFreePeoplePlayer(context.getGame());
                    if(found == null) {
                        found = "nobody";
                    }
                }
                else if(memory.equals("shadow")) {
                    found = getFirstShadowPlayer(context.getGame());
                } else if (memory.equals("self")) {
                    found = GameUtils.getAppendedNames(Collections.singleton(context.getSource()));
                }
                else {
                    found = GameUtils.getAppendedNames(context.getCardsFromMemory(memory));
                    if(found.equalsIgnoreCase("none")) {
                        try {
                            found = context.getValueFromMemory(memory);
                        }
                        catch(IllegalArgumentException ex) {
                            found = "NONE";
                        }
                    }
                }
            }
            if(found == null) {
                found = "NOTHING";
            }
            result = result.replace("{" + memoryName + "}", found);
        }

        return result;
    }
    // "If you can spot X [elven] tokens on conditions..."
    public static int getSpottableCultureTokensOfType(LotroGame game, Token token, Filterable... filters) {
        int tokensTotal = 0;

        final var cards = Filters.filterActive(game, Filters.and(filters, Filters.hasToken(token)));

        for (PhysicalCard physicalCard : cards)
            tokensTotal += game.getGameState().getTokenCount(physicalCard, token);

        return tokensTotal;
    }

    // "If you can spot X culture tokens on conditions..."
    public static int getAllSpottableCultureTokens(LotroGame game, Filterable... filters) {
        int tokensTotal = 0;

        final var cards = Filters.filterActive(game, Filters.and(filters, Filters.hasAnyCultureTokens()));

        for (PhysicalCard physicalCard : cards) {
            var tokens = game.getGameState().getTokens(physicalCard);
            for(var token : tokens.entrySet()) {
                if(token.getKey().getCulture() != null) {
                    tokensTotal += token.getValue();
                }
            }
        }

        return tokensTotal;
    }

    public static int getSpottableCulturesCount(LotroGame game, Filterable... filters) {
        Set<Culture> cultures = new HashSet<>();
        for (PhysicalCard physicalCard : Filters.filterActive(game, filters)) {
            final Culture culture = physicalCard.getBlueprint().getCulture();
            if (culture != null)
                cultures.add(culture);
        }
        return cultures.size();
    }

    public static int getSpottableRacesCount(LotroGame game, Filterable... filters) {
        Set<Race> races = new HashSet<>();
        for (PhysicalCard physicalCard : Filters.filterActive(game, filters)) {
            final Race race = physicalCard.getBlueprint().getRace();
            if (race != null)
                races.add(race);
        }
        return races.size();
    }

    public static String formatNumber(int effective, int requested) {
        if (effective != requested)
            return effective + "(out of " + requested + ")";
        else
            return String.valueOf(effective);
    }

    public static int getRegion(LotroGame game) {
        return getRegion(game.getGameState().getCurrentSiteNumber());
    }

    public static int getRegion(int siteNumber) {
        return 1 + ((siteNumber - 1) / 3);
    }

    public static int getSpottableFPCulturesCount(LotroGame game, String playerId) {
        return game.getModifiersQuerying().getNumberOfSpottableFPCultures(game, playerId);
    }

    public static int getSpottableShadowCulturesCount(LotroGame game, String playerId) {
        return game.getModifiersQuerying().getNumberOfSpottableShadowCultures(game, playerId);
    }

    public static int getControlledSitesCountByPlayer(LotroGame game, String playerId) {
        return game.getModifiersQuerying().getNumberOfSpottableControlledSites(game, playerId);
    }

    public static int getControlledSitesCountOfOpponents(LotroGame game, String playerId) {
        int total = 0;

        for(var opponent : getOpponents(game, playerId)) {
            total += getControlledSitesCountByPlayer(game, opponent);
        }
        return total;
    }
}
