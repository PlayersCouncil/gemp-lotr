package com.gempukku.lotro.packs;

import com.gempukku.lotro.game.CardCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FixedPackBox implements PackBox {
    private Map<String, Integer> _contents = new LinkedHashMap<String, Integer>();

    public FixedPackBox(String packName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(FixedPackBox.class.getResourceAsStream("/" + packName + ".pack")));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                    String[] result = line.split("x", 2);
                    _contents.put(result[1], Integer.parseInt(result[0]));
                }
            }
        } finally {
            bufferedReader.close();
        }

    }

    @Override
    public List<CardCollection.Item> openPack() {
        List<CardCollection.Item> result = new LinkedList<CardCollection.Item>();
        for (Map.Entry<String, Integer> contentsEntry : _contents.entrySet()) {
            String blueprintId = contentsEntry.getKey();
            result.add(CardCollection.Item.createItem(blueprintId, contentsEntry.getValue()));
        }
        return result;
    }
}
