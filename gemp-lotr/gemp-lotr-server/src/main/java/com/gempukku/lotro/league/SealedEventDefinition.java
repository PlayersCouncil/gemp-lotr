package com.gempukku.lotro.league;

import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SealedEventDefinition {
    private final String _name;
    private final String _id;
    private final LotroFormat _format;
    private final List<List<CardCollection.Item>> _seriesProduct = new ArrayList<>();
    private final boolean _hallVisible;

    public SealedEventDefinition(String name, String id, LotroFormat format, List<List<String>> product, boolean hallVisible) {
        _name = name;
        _id = id;
        _format = format;
        _hallVisible = hallVisible;

        for(var serie : product) {
            List<CardCollection.Item> items = new ArrayList<>();
            for(String def : serie) {
                var item = CardCollection.Item.createItem(def);
                items.add(item);
            }

            _seriesProduct.add(items);
        }
    }

    public int GetSerieCount() { return _seriesProduct.size(); }

    public String GetName() { return _name; }
    public String GetID() { return _id; }
    public LotroFormat GetFormat() { return _format; }
    public List<List<CardCollection.Item>> GetAllSeriesProducts() { return Collections.unmodifiableList(_seriesProduct); }
    public List<CardCollection.Item> GetProductForSerie(int serie) { return Collections.unmodifiableList(_seriesProduct.get(serie)); }

    public JSONDefs.SealedTemplate Serialize() {
        return new JSONDefs.SealedTemplate() {{
           name = _name;
           id = _id;
           format = _format.getCode();
           seriesProduct = _seriesProduct.stream()
                   .map(x->x.stream().map(CardCollection.Item::toString).collect(Collectors.toList()))
                   .collect(Collectors.toList());
           hall = _hallVisible;
        }};
    }
}
