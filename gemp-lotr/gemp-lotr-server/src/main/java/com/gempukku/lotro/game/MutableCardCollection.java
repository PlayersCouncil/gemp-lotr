package com.gempukku.lotro.game;

import com.gempukku.lotro.packs.ProductLibrary;

import java.util.Map;

public interface MutableCardCollection extends CardCollection {
    void addItem(String itemId, int count);
    void addItem(CardCollection.Item item);

    boolean removeItem(String itemId, int count);

    void addCurrency(int currency);

    boolean removeCurrency(int currency);

    CardCollection openPack(String packId, String selection, ProductLibrary productLibrary);

    void setExtraInformation(Map<String, Object> extraInformation);


}
