package com.gempukku.lotro.packs;

import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.util.JsonUtils;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class ProductLibrary {
    public static class OuterPackDef {

    }
    private final Map<String, PackBox> _products = new HashMap<>();
    private final LotroCardBlueprintLibrary _cardLibrary;
    private final File _packDirectory;

    private final Semaphore collectionReady = new Semaphore(1);

    public ProductLibrary(LotroCardBlueprintLibrary cardLibrary) {
        this(cardLibrary, AppConfig.getProductPath());
    }
    public ProductLibrary(LotroCardBlueprintLibrary cardLibrary, File packDefinitionDirectory) {
        _cardLibrary = cardLibrary;
        _packDirectory = packDefinitionDirectory;

        collectionReady.acquireUninterruptibly();
        loadPacks(_packDirectory);
        collectionReady.release();
    }

    public void ReloadPacks() {
        try {
            collectionReady.acquire();
            loadPacks(_packDirectory);
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPacks(File path) {
        if (path.isFile()) {
            loadPackFromFile(path);
        }
        else if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                loadPacks(file);
            }
        }
    }

    private void loadPackFromFile(File file) {
        if (!JsonUtils.IsValidHjsonFile(file))
            return;
        JSONParser parser = new JSONParser();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            var defs = JsonUtils.ConvertArray(reader, JSONDefs.Pack.class);

            if(defs == null)
            {
                var def= JsonUtils.Convert(reader, JSONDefs.Pack.class);
                if(def != null)
                {
                    defs = new ArrayList<>();
                    defs.add(def);
                }
                else {
                    System.out.println(file.toString() + " is not a PackDefinition nor an array of PackDefinitions.  Could not load from file.");
                    return;
                }
            }

            for (var def : defs) {
                if(def == null)
                    continue;

                PackBox result = null;
                String[] rarities;
                String[] sets;
                switch (def.type)
                {
                    case RANDOM:
                        if(def.items == null || def.items.isEmpty())
                            continue;
                        if(def.items.stream().anyMatch(x -> x.contains("%"))) {
                            result = WeightedRandomPack.LoadFromArray(def.items);
                        }
                        else {
                            result = UnweightedRandomPack.LoadFromArray(def.items);
                        }

                        break;
                    case RANDOM_FOIL:
                        if(def.data == null || !def.data.containsKey("rarities") || !def.data.containsKey("sets")) {
                            System.out.println(def.name + " RANDOM_FOIL pack type must contain a definition for 'rarities' and 'sets' within data.");
                            continue;
                        }
                        rarities = def.data.get("rarities").toUpperCase().split("\\s*,\\s*");
                        sets = def.data.get("sets").split("\\s*,\\s*");
                        result = new RandomFoilPack(rarities, sets, _cardLibrary);
                        break;
                    case TENGWAR:
                        if(def.data == null || !def.data.containsKey("sets")) {
                            System.out.println(def.name + " TENGWAR pack type must contain a definition for 'sets' within data.");
                            continue;
                        }
                        sets = def.data.get("sets").split("\\s*,\\s*");
                        result = new TengwarPackBox(sets, _cardLibrary);
                        break;
                    case BOOSTER:
                        if(def.data == null || !def.data.containsKey("set")) {
                            System.out.println(def.name + " BOOSTER pack type must contain a definition for 'set' within data.");
                            continue;
                        }
                        if(def.data.get("set").contains(",")) {
                            System.out.println(def.name + " BOOSTER pack type must define exactly one set.");
                            continue;
                        }
                        String set = def.data.get("set").trim();
                        if(set.equals("9")) {
                            result = new ReflectionsPackBox(_cardLibrary);
                        }
                        else {
                            result = new RarityPackBox(_cardLibrary.getSetDefinitions().get(set));
                        }
                        break;
                    case PACK:
                    case SELECTION:
                        if(def.items == null || def.items.isEmpty())
                            continue;
                        result = FixedPackBox.LoadFromArray(def.items, def.recursive);
                        break;
                }
                if(result == null)
                {
                    System.out.println("Unrecognized pack type: " + def.type);
                    continue;
                }

//                if(def.InstantOpen) {
//                    var items = result.openPack().stream().map(CardCollection.Item::toString).toList();
//                    result = FixedPackBox.LoadFromArray(items);
//                }
                if(_products.containsKey(def.name)) {
                    System.out.println("Overwriting existing pack '" + def.name + "'!");
                }
                _products.put(def.name, result);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, PackBox> GetAllProducts() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_products);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("ProductLibrary.GetAllProducts() interrupted: ", exp);
        }
    }

    public PackBox GetProduct(String name) {
        try {
            collectionReady.acquire();
            var data = _products.get(name);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("ProductLibrary.GetProduct() interrupted: ", exp);
        }
    }
}

