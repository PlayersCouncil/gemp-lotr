package com.gempukku.lotro.draft2;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.draft2.builder.*;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SoloDraftDefinitions {
    private static final Logger _logger = LogManager.getLogger(SoloDraftDefinitions.class);
    private Map<String, SoloDraft> _draftTypes = new HashMap<>();
    private final DraftChoiceBuilder _draftChoiceBuilder;
    private final File _draftDefinitionPath;
    private final Semaphore collectionReady = new Semaphore(1);

    public SoloDraftDefinitions(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                LotroFormatLibrary formatLibrary) {
        this(collectionsManager, cardLibrary, formatLibrary, AppConfig.getDraftPath());
    }

    public SoloDraftDefinitions(CollectionsManager collectionsManager, LotroCardBlueprintLibrary cardLibrary,
                                LotroFormatLibrary formatLibrary, File draftDefinitionPath) {
        _draftChoiceBuilder = new DraftChoiceBuilder(collectionsManager, cardLibrary, formatLibrary);
        _draftDefinitionPath = draftDefinitionPath;
        ReloadDraftsFromFile();
    }

    public void ReloadDraftsFromFile() {
        try {
            collectionReady.acquire();
            _draftTypes.clear();
            loadDrafts(_draftDefinitionPath);
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDrafts(File path) {
        if (path.isFile()) {
            loadDraft(path);
        }
        else if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                loadDrafts(file);
            }
        }
    }

    private void loadDraft(File file) {
        try {
            final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            try {
                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(reader);
                String format = (String) object.get("format");
                String code = (String) object.get("code");

                CardCollectionProducer cardCollectionProducer = null;
                JSONObject startingPool = (JSONObject) object.get("startingPool");
                if (startingPool != null)
                    cardCollectionProducer = StartingPoolBuilder.buildCardCollectionProducer(startingPool);

                DraftPoolProducer draftPoolProducer = null;
                JSONArray draftPoolComponents = (JSONArray) object.get("draftPool");
                if (draftPoolComponents != null)
                    draftPoolProducer = DraftPoolBuilder.buildDraftPoolProducer(draftPoolComponents);

                List<DraftChoiceDefinition> draftChoiceDefinitions = new ArrayList<>();
                JSONArray choices = (JSONArray) object.get("choices");
                for (JSONObject choice : (Iterable<JSONObject>) choices) {
                    DraftChoiceDefinition draftChoiceDefinition = _draftChoiceBuilder.buildDraftChoiceDefinition(choice);
                    int repeatCount = ((Number) choice.get("repeat")).intValue();
                    for (int i = 0; i < repeatCount; i++)
                        draftChoiceDefinitions.add(draftChoiceDefinition);
                }

                _logger.debug("Loaded draft definition: " + file);
                var result = new DefaultSoloDraft(code, format, cardCollectionProducer, draftChoiceDefinitions, draftPoolProducer);

                if(_draftTypes.containsKey(code))
                    System.out.println("Duplicate draft loaded: " + code);

                _draftTypes.put(code, result);

            } catch (ParseException exp) {
                throw new RuntimeException("Problem loading solo draft " + file, exp);
            }
        } catch (IOException exp) {
            throw new RuntimeException("Problem loading solo draft " + file, exp);
        }
    }

    public SoloDraft getSoloDraft(String draftType) {
        try {
            collectionReady.acquire();
            var data = _draftTypes.get(draftType);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("SoloDraftDefinitions.getSoloDraft() interrupted: ", exp);
        }
    }

    public Map<String, SoloDraft> getAllSoloDrafts() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_draftTypes);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("SoloDraftDefinitions.getAllSoloDrafts() interrupted: ", exp);
        }
    }
}
