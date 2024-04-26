package com.gempukku.lotro.db;

import com.gempukku.lotro.cache.Cached;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;

public class CachedDeckDAO implements DeckDAO, Cached {
    private final DeckDAO _delegate;
    private final Map<String, Set<Map.Entry<String, String>>> _playerDeckNames = Collections.synchronizedMap(new LRUMap(100));
    private final Map<String, LotroDeck> _decks = Collections.synchronizedMap(new LRUMap(100));

    public CachedDeckDAO(DeckDAO delegate) {
        _delegate = delegate;
    }

    @Override
    public void clearCache() {
        _playerDeckNames.clear();
        _decks.clear();
    }

    @Override
    public int getItemCount() {
        return _playerDeckNames.size()+_decks.size();
    }

    @Override
    public LotroDeck buildDeckFromContents(String deckName, String contents, String target_format, String notes) {
        return _delegate.buildDeckFromContents(deckName, contents, target_format, notes);
    }

    private String constructPlayerDeckNamesKey(Player player) {
        return player.getName();
    }
    private String constructDeckKey(Player player, String name) {
        return player.getName()+"-"+name;
    }

    @Override
    public void deleteDeckForPlayer(Player player, String name) {
        _delegate.deleteDeckForPlayer(player, name);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
    }

    @Override
    public LotroDeck getDeckForPlayer(Player player, String name) {
        String key = constructDeckKey(player, name);
        LotroDeck deck = (LotroDeck) _decks.get(key);
        if (deck == null) {
            deck = _delegate.getDeckForPlayer(player, name);
            _decks.put(key, deck);
        }
        return deck;
    }

    @Override
    public Set<Map.Entry<String, String>> getPlayerDeckNames(Player player) {
        String cacheKey = constructPlayerDeckNamesKey(player);
        Set<Map.Entry<String, String>> deckNames = _playerDeckNames.get(cacheKey);
        if (deckNames == null) {
            deckNames = Collections.synchronizedSet(new HashSet<>(_delegate.getPlayerDeckNames(player)));
            _playerDeckNames.put(cacheKey, deckNames);
        }
        return deckNames;
    }

    @Override
    public LotroDeck renameDeck(Player player, String oldName, String newName) {
        LotroDeck lotroDeck = _delegate.renameDeck(player, oldName, newName);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
        _decks.remove(constructDeckKey(player, oldName));
        _decks.put(constructDeckKey(player, newName), lotroDeck);

        return lotroDeck;
    }

    @Override
    public void saveDeckForPlayer(Player player, String name, String target_format, String notes, LotroDeck deck) {
        _delegate.saveDeckForPlayer(player, name, target_format, notes, deck);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
        _decks.put(constructDeckKey(player, name), deck);
    }
}
