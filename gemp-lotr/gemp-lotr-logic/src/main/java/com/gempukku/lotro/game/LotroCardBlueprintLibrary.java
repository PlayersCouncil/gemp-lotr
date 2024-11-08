package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.common.BlueprintUtils;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.packs.DefaultSetDefinition;
import com.gempukku.lotro.game.packs.SetDefinition;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hjson.JsonValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

public class LotroCardBlueprintLibrary {
    private static final Logger logger = LogManager.getLogger(LotroCardBlueprintLibrary.class);

    private final Map<String, LotroCardBlueprint> _blueprints = new HashMap<>();
    private final Map<String, String> _blueprintMapping = new HashMap<>();
    private final Map<String, Set<String>> _fullBlueprintMapping = new HashMap<>();
    private final Map<String, SetDefinition> _allSets = new LinkedHashMap<>();

    private final LotroCardBlueprintBuilder cardBlueprintBuilder = new LotroCardBlueprintBuilder();

    private final Semaphore collectionReady = new Semaphore(1);
    private final File _cardPath;
    private final File _mappingsPath;
    private final File _setDefsPath;
    private final File _raritiesFolder;

    private final Set<Runnable> refreshCallbacks = new HashSet<>();

    public LotroCardBlueprintLibrary() {
        this(AppConfig.getCardsPath(), AppConfig.getMappingsPath(), AppConfig.getSetDefinitionsPath(), AppConfig.getResourceFile("rarities"));
    }

    public LotroCardBlueprintLibrary(File cardsPath, File mappingsPath, File setDefinitionPath, File raritiesFolder) {
        _cardPath = cardsPath;
        _mappingsPath = mappingsPath;
        _setDefsPath = setDefinitionPath;
        _raritiesFolder = raritiesFolder;
        logger.info("Locking blueprint library in constructor");
        //This will be released after the library has been init'd; until then all functional uses should block
        collectionReady.acquireUninterruptibly();
        logger.info("Unlocking blueprint library in constructor");

        loadSets();
        loadMappings();
        loadCards(_cardPath, true);
        collectionReady.release();
    }

    public boolean subscribeToRefreshes(Runnable callback) {
        return refreshCallbacks.add(callback);
    }

    public boolean unsubscribeFromRefreshes(Runnable callback) {
        return refreshCallbacks.remove(callback);
    }

    public Map<String, SetDefinition> getSetDefinitions() {
        return Collections.unmodifiableMap(_allSets);
    }

    public Map<String, String> getAllMappings() {
        return Collections.unmodifiableMap(_blueprintMapping);
    }

    public Map<String, Set<String>> getFullMappings() {
        return Collections.unmodifiableMap(_fullBlueprintMapping);
    }

    public void reloadAllDefinitions() {
        reloadSets();
        reloadMappings();
        reloadCards();
        errataMappings = null;
        getErrata();

        for (var callback : refreshCallbacks) {
            callback.run();
        }
    }

    private void reloadSets() {
        try {
            collectionReady.acquire();
            loadSets();
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadMappings() {
        try {
            collectionReady.acquire();
            loadMappings();
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reloadCards() {
        try {
            collectionReady.acquire();
            loadCards(_cardPath, false);
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSets() {
        try {
            final InputStreamReader reader = new InputStreamReader(new FileInputStream(_setDefsPath), StandardCharsets.UTF_8);
            try {
                var setDefs = JsonUtils.ConvertArray(reader, JSONDefs.Set.class);

                for (JSONDefs.Set def : setDefs) {
                    if (def == null)
                        continue;

                    var set = new DefaultSetDefinition(def);
                    readSetRarityFile(set, set.getSetId(), def.rarityFile);
                    _allSets.put(set.getSetId(), set);
                }

            } finally {
                IOUtils.closeQuietly(reader);
            }
        } catch (IOException exp) {
            throw new RuntimeException("Unable to read card rarities: " + exp);
        } catch (Exception exp) {
            throw new RuntimeException("Unable to parse setConfig.hjson file: " + exp);
        }
    }

    private void loadMappings() {
        try {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(_mappingsPath), StandardCharsets.UTF_8))) {
                String line;

                _blueprintMapping.clear();
                _fullBlueprintMapping.clear();

                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        String[] split = line.split(",");
                        _blueprintMapping.put(split[0], split[1]);
                        addAlternatives(split[0], split[1]);
                    }
                }
            }
        } catch (IOException exp) {
            throw new RuntimeException("Problem loading blueprintMapping.txt", exp);
        }
    }

    private void loadCards(File path, boolean initial) {
        if (path.isFile()) {
            loadCardsFromFile(path, initial);
        } else if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                loadCards(file, initial);
            }
        }
    }

    public static Map<String, LotroCardBlueprint> loadCardsFromFile(LotroCardBlueprintBuilder cardBlueprintBuilder, InputStream inputStream) throws Exception {
        Map<String, LotroCardBlueprint> result = new HashMap<>();
        JSONParser parser = new JSONParser();
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            //This will read both json and hjson, producing standard json
            String json = JsonValue.readHjson(reader).toString();
            final JSONObject cardsFile = (JSONObject) parser.parse(json);
            final Set<Map.Entry<String, JSONObject>> cardsInFile = cardsFile.entrySet();
            for (Map.Entry<String, JSONObject> cardEntry : cardsInFile) {
                String blueprintId = cardEntry.getKey();
                final JSONObject cardDefinition = cardEntry.getValue();
                try {
                    final var lotroCardBlueprint = cardBlueprintBuilder.buildFromJson(blueprintId, cardDefinition);
                    result.put(blueprintId, lotroCardBlueprint);
                } catch (InvalidCardDefinitionException exp) {
                    logger.error("Unable to load card " + blueprintId, exp);
                }
            }
        }
        return result;
    }

    private void loadCardsFromFile(File file, boolean validateNew) {
        if (!JsonUtils.IsValidHjsonFile(file))
            return;

        try {
            Map<String, LotroCardBlueprint> loadedCards = loadCardsFromFile(cardBlueprintBuilder, new FileInputStream(file));
            for (Map.Entry<String, LotroCardBlueprint> cardBlueprintEntry : loadedCards.entrySet()) {
                String blueprintId = cardBlueprintEntry.getKey();
                if (validateNew && _blueprints.containsKey(blueprintId))
                    logger.error(blueprintId + " from " +
                            file.getAbsolutePath() + " - Replacing existing card definition!");
                _blueprints.put(blueprintId, cardBlueprintEntry.getValue());
            }
        } catch (FileNotFoundException exp) {
            logger.error("Failed to find file " + file.getAbsolutePath(), exp);
        } catch (IOException exp) {
            logger.error("Error while loading file " + file.getAbsolutePath(), exp);
        } catch (ParseException exp) {
            logger.error("Failed to parse file " + file.getAbsolutePath(), exp);
        } catch (Exception exp) {
            logger.error("Unexpected error while parsing file " + file.getAbsolutePath(), exp);
        }
        logger.debug("Loaded JSON card file " + file.getName());
    }

    public String getBaseBlueprintId(String blueprintId) {
        blueprintId = BlueprintUtils.stripModifiers(blueprintId);
        String base = _blueprintMapping.get(blueprintId);
        if (base != null)
            return base;
        return blueprintId;
    }

    private void addAlternatives(String newBlueprint, String existingBlueprint) {
        Set<String> existingAlternates = _fullBlueprintMapping.get(existingBlueprint);
        if (existingAlternates != null) {
            for (String existingAlternate : existingAlternates) {
                addAlternative(newBlueprint, existingAlternate);
                addAlternative(existingAlternate, newBlueprint);
            }
        }
        addAlternative(newBlueprint, existingBlueprint);
        addAlternative(existingBlueprint, newBlueprint);
    }

    private void addAlternative(String from, String to) {
        Set<String> list = _fullBlueprintMapping.get(from);
        if (list == null) {
            list = new HashSet<>();
            _fullBlueprintMapping.put(from, list);
        }
        list.add(to);
    }

    public Map<String, LotroCardBlueprint> getBaseCards() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_blueprints);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("LotroCardBlueprintLibrary.getBaseCard() interrupted: ", exp);
        }
    }

    public Set<String> getAllAlternates(String blueprintId) {
        try {
            collectionReady.acquire();
            var data = _fullBlueprintMapping.get(blueprintId);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("LotroCardBlueprintLibrary.getAllAlternates() interrupted: ", exp);
        }
    }

    private Map<String, JSONDefs.ErrataInfo> errataMappings = null;

    public Map<String, JSONDefs.ErrataInfo> getErrata() {
        try {
            if (errataMappings == null) {
                collectionReady.acquire();
                errataMappings = new HashMap<>();
                for (String id : _blueprints.keySet()) {
                    var parts = id.split("_");
                    int setID = Integer.parseInt(parts[0]);
                    String cardID = parts[1];
                    JSONDefs.ErrataInfo card = null;
                    String base = id;
                    if (setID >= 50 && setID <= 69) {
                        base = "" + (setID - 50) + "_" + cardID;
                    } else if (setID >= 70 && setID <= 89) {
                        base = "" + (setID - 70) + "_" + cardID;
                    } else if (setID >= 150 && setID <= 199) {
                        base = "" + (setID - 50) + "_" + cardID;
                    } else
                        continue;

                    if (errataMappings.containsKey(base)) {
                        card = errataMappings.get(base);
                    } else {
                        var basecard = _blueprints.get(base);

                        //This should only really happen when errata IDs are made
                        //that do not line up with their official counterparts, such
                        //as when making multiple errata candidates.
                        if (basecard == null)
                            continue;
                        card = new JSONDefs.ErrataInfo();
                        card.BaseID = base;
                        card.Name = GameUtils.getFullName(basecard);
                        card.LinkText = GameUtils.getDeluxeCardLink(id, basecard);
                        card.ErrataIDs = new HashMap<>();
                        errataMappings.put(base, card);

                    }

                    card.ErrataIDs.put(JSONDefs.ErrataInfo.PC_Errata, id);
                }

                collectionReady.release();
            }
            return errataMappings;
        } catch (InterruptedException exp) {
            throw new RuntimeException("LotroCardBlueprintLibrary.getErrata() interrupted: ", exp);
        }
    }

    public boolean hasAlternateInSet(String blueprintId, int setNo) {
        try {
            collectionReady.acquire();
            var alternatives = _fullBlueprintMapping.get(blueprintId);
            collectionReady.release();

            if (alternatives != null)
                for (String alternative : alternatives)
                    if (alternative.startsWith(setNo + "_"))
                        return true;

            return false;
        } catch (InterruptedException exp) {
            throw new RuntimeException("LotroCardBlueprintLibrary.hasAlternateInSet() interrupted: ", exp);
        }
    }

    public LotroCardBlueprint getLotroCardBlueprint(String blueprintId) throws CardNotFoundException {
        blueprintId = BlueprintUtils.stripModifiers(blueprintId);
        LotroCardBlueprint bp = null;

        try {
            collectionReady.acquire();
            if (_blueprints.containsKey(blueprintId)) {
                bp = _blueprints.get(blueprintId);
            }
            collectionReady.release();

            if (bp == null)
                throw new CardNotFoundException(blueprintId);

            return bp;
        } catch (InterruptedException exp) {
            throw new RuntimeException("LotroCardBlueprintLibrary.getLotroCardBlueprint() interrupted: ", exp);
        }
    }

//    private LotroCardBlueprint findJavaBlueprint(String blueprintId) throws CardNotFoundException {
//        if (_blueprintMapping.containsKey(blueprintId))
//            return getLotroCardBlueprint(_blueprintMapping.get(blueprintId));
//
//        String[] blueprintParts = blueprintId.split("_");
//
//        String setNumber = blueprintParts[0];
//        String cardNumber = blueprintParts[1];
//
//        for (String packageName : _packageNames) {
//            LotroCardBlueprint blueprint;
//            try {
//                blueprint = tryLoadingFromPackage(packageName, setNumber, cardNumber);
//            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
//                throw new CardNotFoundException(blueprintId);
//            }
//            if (blueprint != null)
//                return blueprint;
//        }
//
//        throw new CardNotFoundException(blueprintId);
//    }

//    private LotroCardBlueprint tryLoadingFromPackage(String packageName, String setNumber, String cardNumber) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
//        try {
//            Class clazz = Class.forName("com.gempukku.lotro.cards.set" + setNumber + packageName + ".Card" + setNumber + "_" + normalizeId(cardNumber));
//            return (LotroCardBlueprint) clazz.getDeclaredConstructor().newInstance();
//        } catch (ClassNotFoundException | InvocationTargetException e) {
//            // Ignore
//            return null;
//        }
//    }

    private String normalizeId(String blueprintPart) {
        int id = Integer.parseInt(blueprintPart);
        if (id < 10)
            return "00" + id;
        else if (id < 100)
            return "0" + id;
        else
            return String.valueOf(id);
    }

    private void determineNeedsLoadingFlag(JSONObject setDefinition, Set<String> flags) {
        Boolean needsLoading = (Boolean) setDefinition.get("needsLoading");
        if (needsLoading == null)
            needsLoading = true;
        if (needsLoading)
            flags.add("needsLoading");
    }

    private void determineMerchantableFlag(JSONObject setDefinition, Set<String> flags) {
        Boolean merchantable = (Boolean) setDefinition.get("merchantable");
        if (merchantable == null)
            merchantable = true;
        if (merchantable)
            flags.add("merchantable");
    }

    private void determineOriginalSetFlag(JSONObject setDefinition, Set<String> flags) {
        Boolean originalSet = (Boolean) setDefinition.get("originalSet");
        if (originalSet == null)
            originalSet = true;
        if (originalSet)
            flags.add("originalSet");
    }

    private void readSetRarityFile(DefaultSetDefinition rarity, String setNo, String rarityFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(_raritiesFolder, rarityFile)), StandardCharsets.UTF_8));
        try {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String blueprintId = setNo + "_" + line.substring(setNo.length() + 1);
                if (line.endsWith("T")) {
                    if (!line.startsWith(setNo))
                        throw new IllegalStateException("Seems the rarity is for some other set");
                    rarity.addTengwarCard(blueprintId);
                } else {
                    if (!line.startsWith(setNo))
                        throw new IllegalStateException("Seems the rarity is for some other set");
                    String cardRarity = line.substring(setNo.length(), setNo.length() + 1);
                    rarity.addCard(blueprintId, cardRarity);
                }
            }
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
    }
}
