package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameStats;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventSerializer {
    public Node serializeEvent(Document doc, GameEvent gameEvent) {
        Element eventElem = doc.createElement("ge");
        eventElem.setAttribute("type", gameEvent.getType().getCode());
        eventElem.setAttribute("timestamp", gameEvent.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSSS")));

        if (gameEvent.getBlueprintId() != null)
            eventElem.setAttribute("blueprintId", gameEvent.getBlueprintId());
        if (gameEvent.getCardId() != null)
            eventElem.setAttribute("cardId", gameEvent.getCardId().toString());
        if (gameEvent.getIndex() != null)
            eventElem.setAttribute("index", gameEvent.getIndex().toString());
        if (gameEvent.getControllerId() != null)
            eventElem.setAttribute("controllerId", gameEvent.getControllerId());
        if (gameEvent.getParticipantId() != null)
            eventElem.setAttribute("participantId", gameEvent.getParticipantId());
        if (gameEvent.getAllParticipantIds() != null)
            eventElem.setAttribute("allParticipantIds", listToCommaSeparated(gameEvent.getAllParticipantIds()));
        if (gameEvent.getPhase() != null)
            eventElem.setAttribute("phase", gameEvent.getPhase());
        if (gameEvent.getTargetCardId() != null)
            eventElem.setAttribute("targetCardId", gameEvent.getTargetCardId().toString());
        if (gameEvent.getZone() != null)
            eventElem.setAttribute("zone", gameEvent.getZone().name());
        if (gameEvent.getHindered() != null)
            eventElem.setAttribute("hindered", gameEvent.getHindered().toString());
        if (gameEvent.getToken() != null)
            eventElem.setAttribute("token", gameEvent.getToken().name());
        if (gameEvent.getCount() != null)
            eventElem.setAttribute("count", gameEvent.getCount().toString());
        if (gameEvent.getOtherCardIds() != null)
            eventElem.setAttribute("otherCardIds", arrayToCommaSeparated(gameEvent.getOtherCardIds()));
        if (gameEvent.getMessage() != null)
            eventElem.setAttribute("message", gameEvent.getMessage());
        if (gameEvent.getVersion() != null)
            eventElem.setAttribute("version", gameEvent.getVersion().toString());
        if (gameEvent.getType() == GameEvent.Type.PARTICIPANTS)
            eventElem.setAttribute("discardPublic", String.valueOf(gameEvent.isDiscardPublic()));
        if(gameEvent.getType() == GameEvent.Type.PRE_GAME_SETUP) {
            var preGameInfo = gameEvent.getPreGameInfo();
            eventElem.setAttribute("summary", preGameInfo.getGameSummary());
            eventElem.setAttribute("notes", preGameInfo.perPlayerNotes(gameEvent.getParticipantId()));
            var maps = mapToString(preGameInfo.maps());
            if(maps != null) {
                eventElem.setAttribute("maps", maps);
            }

        }
        if (gameEvent.getGameStats() != null)
            serializeGameStats(doc, eventElem, gameEvent.getGameStats());
        if (gameEvent.getAwaitingDecision() != null)
            serializeDecision(doc, eventElem, gameEvent.getAwaitingDecision());
        if (gameEvent.getDecks() != null)
            serializeDecks(doc, eventElem, gameEvent.getDecks());

        return eventElem;
    }

    private static String arrayToCommaSeparated(int[] integers) {
        int iMax = integers.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(integers[i]);
            if (i == iMax)
                return b.toString();
            b.append(",");
        }
    }

    private static String mapToString(Map<String, String> map) {
        if(map == null)
            return null;

        var pairs = new ArrayList<String>();

        for(var key : map.keySet()) {
            pairs.add(key + ":" + map.get(key));
        }

        return String.join(",", pairs);
    }

    private static String listToCommaSeparated(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String participantId : strings) {
            if (!first) sb.append(",");
            sb.append(participantId);
            first = false;
        }
        return sb.toString();
    }

    private void serializeDecks(Document document, Element eventElem, Map<String, LotroDeck> decks) {
        for(var pair : decks.entrySet()) {
            String player = pair.getKey();
            var deck = pair.getValue();

            var deckElement = document.createElement("deckReadout");
            deckElement.setAttribute("playerId", player);
            deckElement.setAttribute("name", deck.getDeckName());
            deckElement.setAttribute("rb", deck.getRingBearer());
            deckElement.setAttribute("ring", deck.getRing());
            deckElement.setAttribute("sites", String.join(",", deck.getSites()));
            deckElement.setAttribute("deck", String.join(",", deck.getDrawDeckCards()));

            eventElem.appendChild(deckElement);
        }
    }

    private void serializeDecision(Document doc, Element eventElem, AwaitingDecision decision) {
        eventElem.setAttribute("id", String.valueOf(decision.getAwaitingDecisionId()));
        eventElem.setAttribute("decisionType", decision.getDecisionType().name());
        if (decision.getText() != null)
            eventElem.setAttribute("text", decision.getText());
        for (Map.Entry<String, String[]> paramEntry : decision.getDecisionParameters().entrySet()) {
            for (String value : paramEntry.getValue()) {
                Element decisionParam = doc.createElement("parameter");
                decisionParam.setAttribute("name", paramEntry.getKey());
                decisionParam.setAttribute("value", value);
                eventElem.appendChild(decisionParam);
            }
        }
    }

    private void serializeGameStats(Document doc, Element eventElem, GameStats gameStats) {
        if (gameStats.getWearingRing() != null)
            eventElem.setAttribute("wearingRing", String.valueOf(gameStats.getWearingRing()));
        eventElem.setAttribute("fellowshipArchery", String.valueOf(gameStats.getFellowshipArchery()));
        eventElem.setAttribute("shadowArchery", String.valueOf(gameStats.getShadowArchery()));

        eventElem.setAttribute("fellowshipStrength", String.valueOf(gameStats.getFellowshipSkirmishStrength()));
        eventElem.setAttribute("shadowStrength", String.valueOf(gameStats.getShadowSkirmishStrength()));

        eventElem.setAttribute("fellowshipDamageBonus", String.valueOf(gameStats.getFellowshipSkirmishDamageBonus()));
        eventElem.setAttribute("shadowDamageBonus", String.valueOf(gameStats.getShadowSkirmishDamageBonus()));

        eventElem.setAttribute("fpOverwhelmed", String.valueOf(gameStats.isFpOverwhelmed()));

        eventElem.setAttribute("moveLimit", String.valueOf(gameStats.getMoveLimit()));
        eventElem.setAttribute("moveCount", String.valueOf(gameStats.getMoveCount()));

        eventElem.setAttribute("initiative", String.valueOf(gameStats.getInitiativeSide()));
        if(gameStats.getFellowshipCardsDrawn() != -1) {
            eventElem.setAttribute("ruleof4", String.valueOf(gameStats.getFellowshipCardsDrawn()));
        }

        for (Map.Entry<String, Map<Zone, Integer>> playerZoneSizes : gameStats.getZoneSizes().entrySet()) {
            final Element playerZonesElem = doc.createElement("playerZones");

            playerZonesElem.setAttribute("name", playerZoneSizes.getKey());

            for (Map.Entry<Zone, Integer> zoneSizes : playerZoneSizes.getValue().entrySet())
                playerZonesElem.setAttribute(zoneSizes.getKey().name(), zoneSizes.getValue().toString());

            eventElem.appendChild(playerZonesElem);
        }

        for (Map.Entry<String, Integer> playerThreatEntry : gameStats.getThreats().entrySet()) {
            Element threatsElem = doc.createElement("threats");
            threatsElem.setAttribute("name", playerThreatEntry.getKey());
            threatsElem.setAttribute("value", playerThreatEntry.getValue().toString());
            eventElem.appendChild(threatsElem);
        }

        var charVitalities = gameStats.getCharVitalities();
        var charSiteNumbers = gameStats.getSiteNumbers();
        var charResistances = gameStats.getCharResistances();

        StringBuilder charStr = new StringBuilder();
        for (Map.Entry<Integer, Integer> characters : gameStats.getCharStrengths().entrySet()) {
            Integer charCardId = characters.getKey();
            charStr.append(",").append(charCardId).append("=").append(characters.getValue()).append("|").append(charVitalities.get(charCardId));
            if (charSiteNumbers.containsKey(charCardId))
                charStr.append("|").append(charSiteNumbers.get(charCardId));
            else if (charResistances.containsKey(charCardId))
                charStr.append("|R").append(charResistances.get(charCardId));
        }
        if (!charStr.isEmpty())
            charStr.delete(0, 1);

        if (!charStr.isEmpty())
            eventElem.setAttribute("charStats", charStr.toString());

        var hindered = gameStats.getHindered();
        var hinderStr = hindered.stream().map(String::valueOf).collect(Collectors.joining(","));
        eventElem.setAttribute("hindered", hinderStr);
    }

    public static void main(String[] args) {
        int[] array = new int[] {0, 1};
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int cardId : array) {
            if (!first) sb.append(",");
            sb.append(cardId);
            first = false;
        }
        System.out.println(sb.toString());
        System.out.println(arrayToCommaSeparated(array));
    }
}
