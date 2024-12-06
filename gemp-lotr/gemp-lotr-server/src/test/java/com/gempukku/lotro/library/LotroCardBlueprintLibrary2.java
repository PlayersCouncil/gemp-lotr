package com.gempukku.lotro.library;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.packs.DefaultSetDefinition;
import com.gempukku.lotro.game.packs.SetDefinition;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.util.JsonUtils;
import com.google.common.reflect.ClassPath;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hjson.JsonValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class LotroCardBlueprintLibrary2 {
    private static final Logger logger = LogManager.getLogger(LotroCardBlueprintLibrary2.class);

    private final String[] _packageNames =
            new String[]{
                    "", ".dwarven", ".dunland", ".elven", ".fallenRealms", ".gandalf", ".gollum", ".gondor", ".isengard", ".men", ".orc",
                    ".raider", ".rohan", ".moria", ".wraith", ".sauron", ".shire", ".site", ".uruk_hai",

                    //Additional Hobbit Draft packages
                    ".esgaroth", ".gundabad", ".smaug", ".spider", ".troll"
            };
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

    private final List<Runnable> _refreshCallbacks = new ArrayList<>();

    public LotroCardBlueprintLibrary2() {
        this(AppConfig.getCardsPath(), AppConfig.getMappingsPath(), AppConfig.getSetDefinitionsPath(), AppConfig.getResourceFile("rarities"));
    }

    public LotroCardBlueprintLibrary2(File cardsPath, File mappingsPath, File setDefinitionPath, File raritiesFolder) {
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
        cacheAllJavaBlueprints();
        collectionReady.release();
    }

    public boolean SubscribeToRefreshes(Runnable callback) {
        return _refreshCallbacks.add(callback);
    }

    public boolean UnsubscribeFromRefreshes(Runnable callback) {
        if(!_refreshCallbacks.contains(callback))
            return false;

        _refreshCallbacks.remove(callback);

        return true;
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

        for(var callback : _refreshCallbacks) {
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
        }
        else if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                loadCards(file, initial);
            }
        }
    }

    private void loadCardsFromFile(File file, boolean validateNew) {
        if (!JsonUtils.IsValidHjsonFile(file))
            return;

        JSONParser parser = new JSONParser();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            //This will read both json and hjson, producing standard json
            String json = JsonValue.readHjson(reader).toString();
            final JSONObject cardsFile = (JSONObject) parser.parse(json);
            final Set<Map.Entry<String, JSONObject>> cardsInFile = cardsFile.entrySet();
            for (Map.Entry<String, JSONObject> cardEntry : cardsInFile) {
                String blueprintId = cardEntry.getKey();
                if (validateNew && _blueprints.containsKey(blueprintId)) {
                    logger.error(blueprintId + " from " +
                            file.getAbsolutePath() + " - Replacing existing card definition!");
                }
                final JSONObject cardDefinition = cardEntry.getValue();
                try {
                    final var lotroCardBlueprint = cardBlueprintBuilder.buildFromJson(blueprintId, cardDefinition);
                    _blueprints.put(blueprintId, lotroCardBlueprint);
                } catch (InvalidCardDefinitionException exp) {
                    logger.error("Unable to load card " + blueprintId +
                            " from " + file.getAbsolutePath(), exp);
                }
            }
        } catch (FileNotFoundException exp) {
            logger.error("Failed to find file " + file.getAbsolutePath(), exp);
        } catch (IOException exp) {
            logger.error("Error while loading file " + file.getAbsolutePath(), exp);
        } catch (ParseException exp) {
            logger.error("Failed to parse file " + file.getAbsolutePath(), exp);
        }
        catch (Exception exp) {
            logger.error("Unexpected error while parsing file " + file.getAbsolutePath(), exp);
        }
        logger.debug("Loaded JSON card file " + file.getName());
    }

    //com.gempukku.lotro.cards.set10.dwarven.Card10_001$1
    private final Pattern cardClassPattern = Pattern.compile("com\\.gempukku\\.lotro\\.cards\\.set\\d+\\.(\\w+\\.)?Card(\\d+)_(\\d+)");

    private void cacheAllJavaBlueprints() {
        try {
            logger.debug("Loading remaining Java cards...");
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();

            for(var info : ClassPath.from(loader).getTopLevelClasses()) {
                if(info.getName().contains("Test") || !info.getName().contains("cards.set"))
                    continue;

                var clazz = info.load();

                var match = cardClassPattern.matcher(clazz.getName());
                if(!match.matches())
                    continue;

                String bpId = match.group(2) + "_" + Integer.parseInt(match.group(3));

                try {
                    var blueprint = (LotroCardBlueprint) clazz.getDeclaredConstructor().newInstance();
                    blueprint.setId(bpId);
                    _blueprints.put(bpId, blueprint);
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    // Ignore
                }
            }

            logger.debug("Java card loading complete.");
        } catch (IOException e) {
            throw new RuntimeException("Unable to start the server: failure while searching for Java cards.", e);
        }
    }

    public String getBaseBlueprintId(String blueprintId) {
        blueprintId = stripBlueprintModifiers(blueprintId);
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
            if(errataMappings == null) {
                collectionReady.acquire();
                errataMappings = new HashMap<>();
                for (String id : _blueprints.keySet()) {
                    var parts = id.split("_");
                    int setID = Integer.parseInt(parts[0]);
                    String cardID = parts[1];
                    JSONDefs.ErrataInfo card = null;
                    String base = id;
                    if(setID >= 50 && setID <= 69) {
                        base = "" + (setID - 50) + "_" + cardID;
                    }
                    else if(setID >= 70 && setID <= 89) {
                        base = "" + (setID - 70) + "_" + cardID;
                    }
                    else if(setID >= 150 && setID <= 199) {
                        base = "" + (setID - 50) + "_" + cardID;
                    }
                    else
                        continue;

                    if(errataMappings.containsKey(base)) {
                        card = errataMappings.get(base);
                    }
                    else {
                        var basecard = _blueprints.get(base);

                        //This should only really happen when errata IDs are made
                        //that do not line up with their official counterparts, such
                        //as when making multiple errata candidates.
                        if(basecard == null)
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
        blueprintId = stripBlueprintModifiers(blueprintId);
        LotroCardBlueprint bp = null;
        
        try {
            collectionReady.acquire();
            if (_blueprints.containsKey(blueprintId)) {
                bp = _blueprints.get(blueprintId);
            }
            collectionReady.release();

            if(bp == null)
                throw new CardNotFoundException(blueprintId);

            return bp;
        } catch (InterruptedException exp) {
            throw new RuntimeException("LotroCardBlueprintLibrary.getLotroCardBlueprint() interrupted: ", exp);
        }
    }

    public String stripBlueprintModifiers(String blueprintId) {
        if (blueprintId.endsWith("*"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        if (blueprintId.endsWith("T"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        return blueprintId;
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

    // The below taken from here: https://stackoverflow.com/a/520344/888539

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                if(file.getAbsolutePath().contains("test"))
                    continue;
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
