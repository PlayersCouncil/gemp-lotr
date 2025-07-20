package com.gempukku.lotro.game;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.mysql.cj.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class ReplayMetadata {

    public class DeckMetadata {
        public String Owner;
        public String TargetFormat;
        public String DeckName;
        public List<String> AdventureDeck;
        public List<String> DrawDeck;
        public String RingBearer;
        public String Ring;
        public List<String> StartingFellowship = new ArrayList<>();
    }

    //Version 1: First tracked version; original version was completely different
    //Version 2: Adding the highest achieved sites by player, game IDs, and game timer length information
    public Integer MetadataVersion = 2;

    public DBDefs.GameHistory GameReplayInfo;

    public Map<String, DeckMetadata> Decks = new HashMap<>();
    public Map<String, Integer> PlayerIDs = new HashMap<>();
    public Map<String, Integer> Bids = new HashMap<>();
    public String WentFirst;
    public boolean GameStarted = false;
    public boolean Conceded = false;
    public boolean Canceled = false;

    public Map<String, String> AllCards = new HashMap<>();

    public Set<Integer> SeenCards = new HashSet<>();

    public HashSet<Integer> PlayedCards = new HashSet<>();

    public ReplayMetadata() {

    }
    public ReplayMetadata(DBDefs.GameHistory game, Map<String, LotroDeck> decks) {
        GameReplayInfo = game;

        for(var pair : decks.entrySet()) {
            String player = pair.getKey();
            var deck = pair.getValue();
            var metadata = new DeckMetadata() {{
                Owner = player;
                TargetFormat = deck.getTargetFormat();
                DeckName = deck.getDeckName();
                AdventureDeck = deck.getSites();
                DrawDeck = deck.getDrawDeckCards();
                RingBearer = deck.getRingBearer();
                Ring = deck.getRing();
            }};

            Decks.put(player, metadata);
        }

        if(GameReplayInfo.lose_reason.contains("cancelled") || GameReplayInfo.win_reason.contains("cancelled")) {
            Canceled = true;
        }

        if(GameReplayInfo.lose_reason.contains("Concession") || GameReplayInfo.win_reason.contains("Concession")) {
            Conceded = true;
        }
    }

    public String GetOpponent(String player) {
        return PlayerIDs.keySet().stream().filter(x -> !x.equals(player)).findFirst().get();
    }

    private final Pattern gameStartPattern = Pattern.compile("Players in the game are: ([\\w~-]+), ([\\w~-]+)");
    private final Pattern orderPattern = Pattern.compile("([\\w~-]+) has chosen to go (.*)");
    private final Pattern bidPattern = Pattern.compile("([\\w~-]+) bid (\\d+)");
    public void ParseReplay(String player, List<GameEvent> events) {
        GameStarted = false;

        for(var event : events) {
            if(event.getType() == GameEvent.Type.SEND_MESSAGE) {
                var message = event.getMessage();
                if(StringUtils.isNullOrEmpty(message))
                    continue;

                var regex = gameStartPattern.matcher(message);
                if(regex.matches()) {
                    PlayerIDs.put(regex.group(1), 1);
                    PlayerIDs.put(regex.group(2), 2);
                    continue;
                }

                regex = orderPattern.matcher(message);
                if(regex.matches()) {
                    String bidder = regex.group(1);
                    String order = regex.group(2);
                    if(order.equals("first")) {
                        WentFirst = bidder;
                    }
                    else if(order.equals("second")) {
                        WentFirst = GetOpponent(bidder);
                    }
                    continue;
                }

                regex = bidPattern.matcher(message);
                if(regex.matches()) {
                    String bidder = regex.group(1);
                    String bid = regex.group(2);
                    Bids.put(bidder, Integer.valueOf(bid));
                    continue;
                }
            }
            else if(!GameStarted && event.getType() == GameEvent.Type.GAME_PHASE_CHANGE) {
                var phase = Phase.findPhase(event.getPhase());
                if (phase == Phase.BETWEEN_TURNS)
                {
                    GameStarted = true;
                }
            }

            else if(event.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY) {

                var bpID = event.getBlueprintId();
                var cardID = event.getCardId();
                var participantID = event.getParticipantId();
                var zone = event.getZone();
                var targetCardID = event.getTargetCardId();

                if (bpID != null && cardID != null && participantID != null && participantID.equals(player)) {
                    if (!GameStarted && (zone.equals(Zone.FREE_CHARACTERS)) || zone.equals(Zone.ATTACHED)) {
                        Decks.get(player).StartingFellowship.add(bpID);
                    }

                    switch (zone) {
                        case FREE_CHARACTERS, SUPPORT, SHADOW_CHARACTERS, ADVENTURE_PATH, VOID_FROM_HAND, VOID -> {
                            AllCards.put(cardID.toString(), bpID);
                            SeenCards.add(cardID);
                            PlayedCards.add(cardID);
                        }
                        case ATTACHED, HAND, STACKED, DEAD, DISCARD, ADVENTURE_DECK, DECK, REMOVED -> {
                            AllCards.put(cardID.toString(), bpID);
                            SeenCards.add(cardID);
                        }
                    }
                }
            }
        }
    }
}
