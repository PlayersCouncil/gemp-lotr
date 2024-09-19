package com.gempukku.lotro.draft;

import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.MutableCardCollection;
import com.gempukku.lotro.packs.PacksStorage;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.TournamentCallback;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// TODO - it has to be thread safe
public class DefaultDraft implements Draft {
    // 35 seconds
    public static final int PICK_TIME = 35 * 1000;

    private final CollectionsManager _collectionsManager;
    private final CollectionType _collectionType;
    private final ProductLibrary _productLibrary;
    private final DraftPack _draftPack;
    private final List<String> _players;

    private final List<MutableCardCollection> _cardChoices = new ArrayList<>();
    private final Map<String, MutableCardCollection> _cardChoice = new HashMap<>();

    private final int _playerCount;

    private long _lastPickStart;

    private int _nextPickNumber = 0;
    private int _nextPackIndex = 0;

    private boolean _finishedDraft;

    private final Map<String, DraftCommunicationChannel> _playerDraftCommunications = new HashMap<>();
    private int _nextChannelNumber = 0;

    public DefaultDraft(CollectionsManager collectionsManager, CollectionType collectionType, ProductLibrary productLibrary, DraftPack draftPack, Set<String> players) {
        _collectionsManager = collectionsManager;
        _collectionType = collectionType;
        _productLibrary = productLibrary;
        _draftPack = draftPack;
        _players = new ArrayList(players);
        Collections.shuffle(_players, ThreadLocalRandom.current());
        
        _playerCount = _players.size();

        CardCollection fixedCollection = _draftPack.getFixedCollection();
        for (String player : _players)
            _collectionsManager.addPlayerCollection(false, "New draft fixed collection", player, _collectionType, fixedCollection);
    }

    @Override
    public void advanceDraft() {
        if (haveAllPlayersPicked()) {
            if (haveAllCardsBeenChosen()) {
                if (haveMorePacks()) {
                    openNextPacks();
                } else {
                    _finishedDraft = true;
                }
            } else {
                presentNewCardChoices();
            }
            for (DraftCommunicationChannel draftCommunicationChannel : _playerDraftCommunications.values())
                draftCommunicationChannel.draftChanged();
        } else {
            if (choiceTimePassed()) {
                forceRandomCardChoice();
                for (DraftCommunicationChannel draftCommunicationChannel : _playerDraftCommunications.values())
                    draftCommunicationChannel.draftChanged();
            }
        }
    }

    @Override
    public void playerChosenCard(String playerName, String cardId) {
        playerChosen(playerName, cardId);
        _playerDraftCommunications.get(playerName).draftChanged();
    }

    public void signUpForDraft(String playerName, DraftChannelVisitor draftChannelVisitor) {
        DraftCommunicationChannel draftCommunicationChannel = new DraftCommunicationChannel(_nextChannelNumber++);
        _playerDraftCommunications.put(playerName, draftCommunicationChannel);
        draftCommunicationChannel.processCommunicationChannel(getCardChoice(playerName), getChosenCards(playerName), draftChannelVisitor);
    }

    public DraftCommunicationChannel getCommunicationChannel(String playerName, int channelNumber) throws SubscriptionExpiredException, SubscriptionConflictException {
        DraftCommunicationChannel communicationChannel = _playerDraftCommunications.get(playerName);
        if (communicationChannel != null) {
            if (communicationChannel.getChannelNumber() == channelNumber) {
                return communicationChannel;
            } else {
                throw new SubscriptionConflictException();
            }
        } else {
            throw new SubscriptionExpiredException();
        }
    }

    public DraftCardChoice getCardChoice(String playerName) {
        MutableCardCollection cardChoice = _cardChoice.get(playerName);

        return new DefaultDraftCardChoice(cardChoice, _lastPickStart + PICK_TIME);
    }

    @Override
    public CardCollection getChosenCards(String playerName) {
        return _collectionsManager.getPlayerCollection(playerName, _collectionType.getCode());
    }

    @Override
    public boolean isFinished() {
        return _finishedDraft;
    }

    private void forceRandomCardChoice() {
        Map<String, MutableCardCollection> cardChoiceCopy = new HashMap<>(_cardChoice);

        for (Map.Entry<String, MutableCardCollection> playerChoices : cardChoiceCopy.entrySet()) {
            String playerName = playerChoices.getKey();
            MutableCardCollection collection = playerChoices.getValue();
            String cardId = collection.getAll().iterator().next().getBlueprintId();
            playerChosen(playerName, cardId);
        }
    }

    private void playerChosen(String playerName, String cardId) {
        MutableCardCollection cardChoice = _cardChoice.get(playerName);
        if (cardChoice != null) {
            if (cardChoice.removeItem(cardId, 1)) {
                _collectionsManager.addItemsToPlayerCollection(false, "Pick in draft", playerName, _collectionType, List.of(CardCollection.Item.createItem(cardId, 1)));
                _cardChoice.remove(playerName);
            }
        }
    }

    private void presentNewCardChoices() {
        for (int i = 0; i < _players.size(); i++) {
            _cardChoice.put(_players.get(i), _cardChoices.get((i + _nextPickNumber) % _playerCount));
        }
        _nextPickNumber++;
        _lastPickStart = System.currentTimeMillis();
    }

    private void openNextPacks() {
        _cardChoices.clear();
        String packId = _draftPack.getPacks().get(_nextPackIndex);
        for (int i = 0; i < _playerCount; i++) {
            MutableCardCollection cardCollection = new DefaultCardCollection();
            cardCollection.addItem(packId, 1);
            cardCollection.openPack(packId, null, _productLibrary);
            _cardChoices.add(cardCollection);
        }
        _nextPackIndex++;
        _nextPickNumber = 0;
    }

    private boolean choiceTimePassed() {
        return System.currentTimeMillis() > PICK_TIME + _lastPickStart;
    }

    private boolean haveMorePacks() {
        return _nextPackIndex < _draftPack.getPacks().size();
    }

    private boolean haveAllCardsBeenChosen() {
        return _cardChoices.size() == 0 || !_cardChoices.get(0).getAll().iterator().hasNext();
    }

    private boolean haveAllPlayersPicked() {
        return _cardChoice.isEmpty();
    }
}
