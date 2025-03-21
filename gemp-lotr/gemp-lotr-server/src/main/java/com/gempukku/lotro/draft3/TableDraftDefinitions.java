package com.gempukku.lotro.draft3;

import com.gempukku.lotro.common.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.Semaphore;

public class TableDraftDefinitions {
    private static final Logger logger = LogManager.getLogger(TableDraftDefinitions.class);

    private final Map<String, TableDraftDefinition> draftTypes = new HashMap<>();
    private final Semaphore collectionReady = new Semaphore(1);

    public TableDraftDefinitions() {
        reloadDraftsFromFile();
    }

    public TableDraftDefinition getTableDraftDefinition(String draftType) {
        try {
            collectionReady.acquire();
            TableDraftDefinition data = draftTypes.get(draftType);
            collectionReady.release();
            return data;
        } catch (InterruptedException e) {
            throw new RuntimeException("TableDraftDefinitions.getTableDraftDefinition() interrupted: ", e);
        }
    }

    public Collection<TableDraftDefinition> getAllTableDrafts() {
        try {
            collectionReady.acquire();
            List<TableDraftDefinition> tbr = new ArrayList<>(draftTypes.values());
            tbr.sort((o1, o2) -> new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    int setComparison = compareSet(s1, s2);
                    if (setComparison != 0) return setComparison;

                    return compareCategory(s1, s2);
                }

                private int compareSet(String s1, String s2) {
                    return Boolean.compare(s2.contains("fotr"), s1.contains("fotr")) != 0 ?
                            Boolean.compare(s2.contains("fotr"), s1.contains("fotr")) :
                            Boolean.compare(s2.contains("ttt"), s1.contains("ttt"));
                }

                private int compareCategory(String s1, String s2) {
                    return Integer.compare(getCategoryValue(s2), getCategoryValue(s1));
                }

                private int getCategoryValue(String s) {
                    if (s.contains("power")) return 3;
                    if (s.contains("fusion")) return 2;
                    return 1;
                }
            }.compare(o1.getCode(), o2.getCode()));
            collectionReady.release();
            return tbr;
        } catch (InterruptedException exp) {
            throw new RuntimeException("TableDraftDefinitions.getAllTableDrafts() interrupted: ", exp);
        }
    }

    public void reloadDraftsFromFile() {
        try {
            collectionReady.acquire();
            draftTypes.clear();
            loadDrafts(AppConfig.getTableDraftPath());
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDrafts(File path) {
        if (path.isFile()) {
            TableDraftDefinition tableDraftDefinition = TableDraftBuilder.build(path);
            logger.debug("Loaded table draft definition: " + path);

            if(draftTypes.containsKey(tableDraftDefinition.getCode()))
                logger.error("Duplicate draft loaded: " + tableDraftDefinition.getCode());

            draftTypes.put(tableDraftDefinition.getCode(), tableDraftDefinition);
        }
        else if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                loadDrafts(file);
            }
        }
    }
}
