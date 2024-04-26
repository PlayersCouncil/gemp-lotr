package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PreGameInfo;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.PlayerOrder;
import com.gempukku.lotro.logic.modifiers.ModifiersEnvironment;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;
import com.gempukku.lotro.logic.timing.rules.CharacterDeathRule;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class DefaultLotroGame implements LotroGame {
    private static final Logger log = LogManager.getLogger(DefaultLotroGame.class);

    private final GameState _gameState;
    private final ModifiersLogic _modifiersLogic = new ModifiersLogic();
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final UserFeedback _userFeedback;
    private final TurnProcedure _turnProcedure;
    private final ActionStack _actionStack;
    private boolean _cancelled;
    private boolean _finished;

    private final Adventure _adventure;
    private final LotroFormat _format;

    private final Set<String> _allPlayers;
    private final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();

    private String _winnerPlayerId;
    private final Map<String, String> _losers = new HashMap<>();

    private final Set<GameResultListener> _gameResultListeners = new HashSet<>();

    private final Set<String> _requestedCancel = new HashSet<>();
    private final LotroCardBlueprintLibrary _library;

    public DefaultLotroGame(LotroFormat format, Map<String, LotroDeck> decks, UserFeedback userFeedback, final LotroCardBlueprintLibrary library) {
        this(format, decks, userFeedback, library, "No timer", false, "Test Match");
    }

    public DefaultLotroGame(LotroFormat format, Map<String, LotroDeck> decks, UserFeedback userFeedback, final LotroCardBlueprintLibrary library,
            String timerInfo, boolean allowSpectators, String tournamentName ) {
        _library = library;
        _adventure = format.getAdventure();
        _format = format;
        _actionStack = new ActionStack();

        _allPlayers = decks.keySet();

        _actionsEnvironment = new DefaultActionsEnvironment(this, _actionStack);

        final Map<String, List<String>> cards = new HashMap<>();
        final Map<String, String> ringBearers = new HashMap<>();
        final Map<String, String> rings = new HashMap<>();
        final Map<String, String> maps = new HashMap<>();
        final Map<String, String> notes = new HashMap<>();
        final StringBuilder formatInfo = new StringBuilder();
        for (String playerId : decks.keySet()) {
            List<String> deck = new LinkedList<>();

            LotroDeck lotroDeck = decks.get(playerId);
            deck.addAll(lotroDeck.getSites());
            deck.addAll(lotroDeck.getDrawDeckCards());

            cards.put(playerId, deck);
            ringBearers.put(playerId, lotroDeck.getRingBearer());
            if (lotroDeck.getRing() != null)
                rings.put(playerId, lotroDeck.getRing());

            if(format.usesMaps()) {
                maps.put(playerId, lotroDeck.getMap());
            }

            var note = "Deck used: <a href='" + lotroDeck.getURL(playerId) + "' target='_blank'>" + lotroDeck.getDeckName() +
                    "</a> [" + lotroDeck.getTargetFormat() + "]<br/><br/>Deck Notes:<br/>";
            if(lotroDeck.getNotes() != null) {
                note += lotroDeck.getNotes();
            }
            else {
                note += "No deck notes.";
            }

            notes.put(playerId, note);
        }

        if(format.getName().contains("PC")) {
            formatInfo.append(LotroFormat.PCSummary);
        }

        _gameState = new GameState();

        CharacterDeathRule characterDeathRule = new CharacterDeathRule(_actionsEnvironment);
        characterDeathRule.applyRule();

        _turnProcedure = new TurnProcedure(this, decks.keySet(), userFeedback, _actionStack,
                new PlayerOrderFeedback() {
                    @Override
                    public void setPlayerOrder(PlayerOrder playerOrder, String firstPlayer) {
                        _gameState.init(playerOrder, firstPlayer, cards, ringBearers, rings, maps, library, format);
                    }
                },
                new PregameSetupFeedback() {
                    @Override
                    public void populatePregameInfo() {
                        var preGameInfo = new PreGameInfo(decks.keySet().stream().toList(), tournamentName, timerInfo,
                                !allowSpectators, format, formatInfo.toString(), notes, maps);

                        _gameState.initPreGame(preGameInfo);
                    }
                }, characterDeathRule);
        _userFeedback = userFeedback;

        RuleSet ruleSet = new RuleSet(this, _actionsEnvironment, _modifiersLogic);
        ruleSet.applyRuleSet();

        _adventure.applyAdventureRules(this, _actionsEnvironment, _modifiersLogic);
    }


    @Override
    public boolean shouldAutoPass(String playerId, Phase phase) {
        final Set<Phase> passablePhases = _autoPassConfiguration.get(playerId);
        if (passablePhases == null)
            return false;
        return passablePhases.contains(phase);
    }

    @Override
    public boolean isSolo() {
        return _allPlayers.size() == 1;
    }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
    }

    public void removeGameResultListener(GameResultListener listener) {
        _gameResultListeners.remove(listener);
    }

    @Override
    public LotroFormat getFormat() {
        return _format;
    }

    public void startGame() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    @Override
    public String getWinnerPlayerId() {
        return _winnerPlayerId;
    }

    public boolean isFinished() {
        return _finished;
    }

    public void cancelGame() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null) {
                _gameState.sendMessage("Game was cancelled due to an error, the error was logged and will be fixed soon.");
                _gameState.sendMessage("Please post the replay game link and description of what happened on the TLHH forum.");
            }

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public void cancelGameRequested() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null)
                _gameState.sendMessage("Game was cancelled, as requested by all parties.");

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void playerWon(String playerId, String reason) {
        if (!_finished) {
            // Any remaining players have lost
            Set<String> losers = new HashSet<>(_allPlayers);
            losers.removeAll(_losers.keySet());
            losers.remove(playerId);

            for (String loser : losers)
                _losers.put(loser, "Other player won");

            gameWon(playerId, reason);
        }
    }

    private void gameWon(String winner, String reason) {
        _winnerPlayerId = winner;

        if (_gameState != null)
            _gameState.sendMessage(_winnerPlayerId + " is the winner due to: " + reason);

        _gameState.finish();

        for (GameResultListener gameResultListener : _gameResultListeners)
            gameResultListener.gameFinished(_winnerPlayerId, reason, _losers);

        _finished = true;
    }

    @Override
    public void playerLost(String playerId, String reason) {
        if (!_finished) {
            if (_losers.get(playerId) == null) {
                _losers.put(playerId, reason);
                if (_gameState != null)
                    _gameState.sendMessage(playerId + " lost due to: " + reason);

                if (_losers.size() + 1 == _allPlayers.size()) {
                    List<String> allPlayers = new LinkedList<>(_allPlayers);
                    allPlayers.removeAll(_losers.keySet());
                    gameWon(allPlayers.get(0), "Last remaining player in game");
                }
            }
        }
    }

    public void requestCancel(String playerId) {
        _requestedCancel.add(playerId);
        if (_requestedCancel.size() == _allPlayers.size())
            cancelGameRequested();
    }

    @Override
    public GameState getGameState() {
        return _gameState;
    }

    @Override
    public LotroCardBlueprintLibrary getLotroCardBlueprintLibrary() {
        return _library;
    }

    @Override
    public ActionsEnvironment getActionsEnvironment() {
        return _actionsEnvironment;
    }

    @Override
    public ModifiersEnvironment getModifiersEnvironment() {
        return _modifiersLogic;
    }

    @Override
    public ModifiersQuerying getModifiersQuerying() {
        return _modifiersLogic;
    }

    @Override
    public UserFeedback getUserFeedback() {
        return _userFeedback;
    }

    @Override
    public void checkRingBearerCorruption() {
        GameState gameState = getGameState();
        if (gameState != null && gameState.getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && gameState.getCurrentPhase() != Phase.BETWEEN_TURNS && gameState.getCurrentPhase() != Phase.PUT_RING_BEARER) {
            // Ring-bearer death
            PhysicalCard ringBearer = gameState.getRingBearer(gameState.getCurrentPlayerId());
            Zone zone = ringBearer.getZone();
            if (zone != null && zone.isInPlay()) {
                // Ring-bearer corruption
                int ringBearerResistance = getModifiersQuerying().getResistance(this, ringBearer);
                if (ringBearerResistance <= 0)
                    playerLost(getGameState().getCurrentPlayerId(), "The Ring-Bearer is corrupted");
            }
        }
    }

    @Override
    public void checkRingBearerAlive() {
        GameState gameState = getGameState();
        if (gameState != null && gameState.getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && gameState.getCurrentPhase() != Phase.BETWEEN_TURNS && gameState.getCurrentPhase() != Phase.PUT_RING_BEARER) {
            // Ring-bearer death
            PhysicalCard ringBearer = gameState.getRingBearer(gameState.getCurrentPlayerId());
            Zone zone = ringBearer.getZone();
            if (zone == null || !zone.isInPlay())
                playerLost(getGameState().getCurrentPlayerId(), "The Ring-Bearer is dead");
        }
    }

    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        _gameState.addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameState.removeGameStateListener(gameStateListener);
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        _autoPassConfiguration.put(playerId, phases);
    }
}
