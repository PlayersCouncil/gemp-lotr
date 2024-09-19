package com.gempukku.lotro.packs;

import com.gempukku.lotro.game.CardCollection;

import java.util.List;

public interface PackBox {
    List<CardCollection.Item> openPack();
    List<CardCollection.Item> openPack(int selection);
    List<String> GetAllOptions();
}
