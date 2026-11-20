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

    // This field looks like it's unused, but it gets serialized.
    //Version 1: First tracked version
    //Version 2: Adding the highest achieved sites by player, game IDs, and game timer length information
    //Version 3: Fixed a bug where attached cards were never added to the PlayedCards collection
    //Version 4: StartingFellowship no longer counts cards attached after the game started
    //Version 5: Play that was taken back (see Reverts) is left out of everything; Reverts and JournalFile added
    public Integer MetadataVersion = 5;

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

    public class RevertMetadata {
        public String Requester;
        // GREEN, YELLOW or RED; empty if the game was taken back by other means than a player's asking
        public String Tier;
        public String Label;
        public int Turn;
    }

    // Each time the game was taken back to an earlier point, soonest first
    public List<RevertMetadata> Reverts = new ArrayList<>();

    // The file beside the summaries' folder that holds the game's journal; null if none was kept
    public String JournalFile;

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

        // Play that was taken back counts for nothing: when a game is taken back, its recording is sent the table
        // again and then an event that says where in the recording the undone play began.  Everything from there up
        // to that event is left out.
        boolean[] undone = new boolean[events.size()];
        for (int i = 0; i < events.size(); i++) {
            var reverted = events.get(i);
            if (reverted.getType() == GameEvent.Type.GAME_REVERTED && reverted.getIndex() != null)
                for (int j = Math.max(0, reverted.getIndex()); j <= i; j++)
                    undone[j] = true;
        }

        for (int eventIndex = 0; eventIndex < events.size(); eventIndex++) {
            if (undone[eventIndex])
                continue;
            var event = events.get(eventIndex);
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
                    // Only what went down before the game started, attached cards as much as characters (F-0469)
                    if (!GameStarted && (zone.equals(Zone.FREE_CHARACTERS) || zone.equals(Zone.ATTACHED))) {
                        Decks.get(player).StartingFellowship.add(bpID);
                    }

                    switch (zone) {
                        case ATTACHED, FREE_CHARACTERS, SUPPORT, SHADOW_CHARACTERS, ADVENTURE_PATH, VOID_FROM_HAND, VOID -> {
                            AllCards.put(cardID.toString(), bpID);
                            SeenCards.add(cardID);
                            PlayedCards.add(cardID);
                        }
                        case HAND, STACKED, DEAD, DISCARD, ADVENTURE_DECK, DECK, REMOVED -> {
                            AllCards.put(cardID.toString(), bpID);
                            SeenCards.add(cardID);
                        }
                    }
                }
            }
        }
    }
}
