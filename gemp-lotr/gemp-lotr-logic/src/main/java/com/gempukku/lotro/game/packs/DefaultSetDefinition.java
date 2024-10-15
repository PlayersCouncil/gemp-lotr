package com.gempukku.lotro.game.packs;

import com.gempukku.lotro.common.JSONDefs;

import java.util.*;

public class DefaultSetDefinition implements SetDefinition {
    private final List<String> _tengwarCards = new LinkedList<>();
    private final Map<String, List<String>> _rarityList = new HashMap<>();
    private final Map<String, String> _cardsRarity = new LinkedHashMap<>();

    public final String ID;
    public final String Name;
    public final boolean IsDecipherSet;
    public final boolean Merchantable;
    public final boolean NeedsLoading;

    public DefaultSetDefinition(JSONDefs.Set setdef) {
        ID = String.valueOf(setdef.setId);
        Name = setdef.setName;
        IsDecipherSet = setdef.originalSet;
        Merchantable = setdef.merchantable;;
        NeedsLoading = setdef.needsLoading;;
    }

    public void addCard(String blueprintId, String rarity) {
        _cardsRarity.put(blueprintId, rarity);
        var cardsOfRarity = _rarityList.computeIfAbsent(rarity, k -> new LinkedList<>());
        cardsOfRarity.add(blueprintId);
    }

    public void addTengwarCard(String blueprintId) {
        _tengwarCards.add(blueprintId);
    }

    @Override
    public String getSetName() {
        return Name;
    }

    @Override
    public String getSetId() {
        return ID;
    }

    @Override
    public boolean IsDecipherSet() { return IsDecipherSet; };
    @Override
    public boolean Merchantable() { return Merchantable; };
    @Override
    public boolean NeedsLoading() { return NeedsLoading; };

    @Override
    public List<String> getCardsOfRarity(String rarity) {
        final List<String> list = _rarityList.get(rarity);
        if (list == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<String> getTengwarCards() {
        if (_tengwarCards == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(_tengwarCards);
    }

    @Override
    public String getCardRarity(String cardId) {
        return _cardsRarity.get(cardId);
    }

    @Override
    public Set<String> getAllCards() {
        return Collections.unmodifiableSet(_cardsRarity.keySet());
    }
}
