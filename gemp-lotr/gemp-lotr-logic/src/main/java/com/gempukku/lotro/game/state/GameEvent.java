package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameStats;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class GameEvent {
    public enum Type {
        PARTICIPANTS("P"), GAME_PHASE_CHANGE("GPC"), TURN_CHANGE("TC"), PLAYER_POSITION("PP"),
        TWILIGHT_POOL_UPDATE("TP"),
        PUT_CARD_INTO_PLAY("PCIP"), MOVE_CARD_IN_PLAY("MCIP"), REMOVE_CARD_FROM_PLAY("RCFP"),
        ADD_ASSIGNMENT("AA"), REMOVE_ASSIGNMENT("RA"),
        START_SKIRMISH("SS"), REMOVE_FROM_SKIRMISH("RFS"), ADD_TO_SKIRMISH("ATS"), END_SKIRMISH("ES"),
        ADD_TOKENS("AT"), REMOVE_TOKENS("RT"),
        SEND_MESSAGE("M"), SEND_WARNING("W"),
        GAME_STATS("GS"),
        CHAT_MESSAGE("CM"),
        GAME_ENDED("EG"),
        CARD_AFFECTED_BY_CARD("CAC"), SHOW_CARD_ON_SCREEN("EP"), FLASH_CARD_IN_PLAY("CA"), DECISION("D"),
        PRE_GAME_SETUP("PGS");

        private final String code;

        private Type(String code) {
            this.code = code;
        }

        String getCode() {
            return code;
        }
    }

    private String _message;
    private PreGameInfo _preGameInfo;
    private String _side;
    private final Type _type;
    private Zone _zone;
    private String _participantId;
    private String _controllerId;
    private List<String> _allParticipantIds;
    private Integer _index;
    private String _blueprintId;
    private Integer _cardId;
    private Integer _targetCardId;
    private String _phase;
    private Integer _count;
    private Token _token;
    private int[] _otherCardIds;
    private Map<String, LotroDeck> _decks;
    private GameStats _gameStats;
    private AwaitingDecision _awaitingDecision;
    private ZonedDateTime _timestamp;
    private Integer _version;
    private boolean _discardIsPublic;

    public GameEvent(Type type) {
        _type = type;
        _timestamp = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public ZonedDateTime getTimestamp() { return _timestamp; }
    public Integer getVersion() { return _version; }
    public GameEvent version(int version) {
        _version = version;
        return this;
    }

    public Integer getIndex() {
        return _index;
    }

    public GameEvent index(int index) {
        _index = index;
        return this;
    }

    public Type getType() {
        return _type;
    }

    public GameStats getGameStats() {
        return _gameStats;
    }

    public GameEvent gameStats(GameStats gameStats) {
        _gameStats = gameStats;
        return this;
    }

    public AwaitingDecision getAwaitingDecision() {
        return _awaitingDecision;
    }

    public GameEvent awaitingDecision(AwaitingDecision awaitingDecision) {
        _awaitingDecision = awaitingDecision;
        return this;
    }

    public Zone getZone() {
        return _zone;
    }

    public GameEvent zone(Zone zone) {
        _zone = zone;
        return this;
    }

    public Token getToken() {
        return _token;
    }

    public GameEvent token(Token token) {
        _token = token;
        return this;
    }

    public String getMessage() {
        return _message;
    }

    public GameEvent message(String message) {
        _message = message;
        return this;
    }

    public PreGameInfo getPreGameInfo() {
        return _preGameInfo;
    }

    public GameEvent preGameInfo(PreGameInfo preGameInfo) {
        _preGameInfo = preGameInfo;
        return this;
    }

    public Integer getCount() {
        return _count;
    }

    public GameEvent count(int count) {
        _count = count;
        return this;
    }

    public int[] getOtherCardIds() {
        return _otherCardIds;
    }

    public GameEvent otherCardIds(int[] otherCardIds) {
        _otherCardIds = otherCardIds;
        return this;
    }

    public Map<String, LotroDeck> getDecks() {
        return _decks;
    }

    public GameEvent decks(Map<String, LotroDeck> decks) {
        _decks = decks;
        return this;
    }

    public String getParticipantId() {
        return _participantId;
    }

    public GameEvent participantId(String participantId) {
        _participantId = participantId;
        return this;
    }

    public List<String> getAllParticipantIds() {
        return _allParticipantIds;
    }

    public GameEvent allParticipantIds(List<String> allParticipantIds) {
        _allParticipantIds = allParticipantIds;
        return this;
    }

    public String getSide() {
        return _side;
    }

    public GameEvent side(String side) {
        _side = side;
        return this;
    }

    public String getControllerId() {
        return _controllerId;
    }

    public GameEvent controllerId(String controllerId) {
        _controllerId = controllerId;
        return this;
    }

    public GameEvent card(PhysicalCard physicalCard) {
        GameEvent gameEvent = cardId(physicalCard.getCardId()).blueprintId(physicalCard.getBlueprintId()).participantId(physicalCard.getOwner()).zone(physicalCard.getZone());
        if (physicalCard.getCardController() != null)
            gameEvent = gameEvent.controllerId(physicalCard.getCardController());
        PhysicalCard attachedTo = physicalCard.getAttachedTo();
        if (attachedTo != null)
            gameEvent = gameEvent.targetCardId(attachedTo.getCardId());
        PhysicalCard stackedOn = physicalCard.getStackedOn();
        if (stackedOn != null)
            gameEvent = gameEvent.targetCardId(stackedOn.getCardId());
        if (physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getZone().isInPlay())
            gameEvent = gameEvent.index(physicalCard.getSiteNumber());
        return gameEvent;
    }

    public String getBlueprintId() {
        return _blueprintId;
    }

    public GameEvent blueprintId(String blueprintId) {
        _blueprintId = blueprintId;
        return this;
    }

    public Integer getCardId() {
        return _cardId;
    }

    public GameEvent cardId(int cardId) {
        _cardId = cardId;
        return this;
    }

    public Integer getTargetCardId() {
        return _targetCardId;
    }

    public GameEvent targetCardId(int targetCardId) {
        _targetCardId = targetCardId;
        return this;
    }

    public String getPhase() {
        return _phase;
    }

    public GameEvent phase(String phase) {
        _phase = phase;
        return this;
    }

    public boolean isDiscardPublic() {
        return _discardIsPublic;
    }

    public GameEvent discardPublic(boolean discardPublic) {
        _discardIsPublic = discardPublic;
        return this;
    }
}
