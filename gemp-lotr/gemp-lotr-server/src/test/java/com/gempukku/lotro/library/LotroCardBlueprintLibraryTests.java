package com.gempukku.lotro.library;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.GameUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LotroCardBlueprintLibraryTests extends AbstractAtTest {
    @Test
    public void LibraryLoadsWithNoDuplicates() throws CardNotFoundException {
        Map<String, String> cardNames = new HashMap<>();
        for (int i = 0; i <= 19; i++) {
            for (int j = 1; j <= 365; j++) {
                String blueprintId = i + "_" + j;
                try {
                    if (blueprintId.equals(_cardLibrary.getBaseBlueprintId(blueprintId))) {
                        try {
                            LotroCardBlueprint cardBlueprint = _cardLibrary.getLotroCardBlueprint(blueprintId);
                            String cardName = GameUtils.getFullName(cardBlueprint);
                            if (cardNames.containsKey(cardName) && cardBlueprint.getCardType() != CardType.SITE)
                                System.out.println("Multiple detected - " + cardName + ": " + cardNames.get(cardName) + " and " + blueprintId);
                            else
                                cardNames.put(cardName, blueprintId);
                        }
                        catch(CardNotFoundException ex) {
                            break;
                        }
                    }
                } catch (IllegalArgumentException exp) {
                    //exp.printStackTrace();
                }
            }
        }

    }

    private void RecordDiff(Set<String> diffs, String message) {
        diffs.add(message);
        System.out.println(message);
    }

    @Test
    public void NewLibraryMapsSameAsOld() {
        var oldLib = _cardLibrary;
        var newLib = new LotroCardBlueprintLibrary2();

        //_blueprints export, should be 3,878 card representations,
        // including base cards, reprints, errata, etc.
        var oldBlueprints = oldLib.getBaseCards();
        var newBlueprints = newLib.getBaseCards();
        assertEquals(oldBlueprints.size(), newBlueprints.size());

        System.out.println("\n========\n");

        //These are unlikely to differ between the two as we are not altering card loading.
        //Thus these are asserts to halt the testing process if we are discovered to have
        // violated that assumption.
        for(var bp : oldBlueprints.keySet()) {
            assertTrue("Blueprint found in the old library but not the new: " + bp,
                    newBlueprints.containsKey(bp));
        }

        for(var bp : newBlueprints.keySet()) {
            assertTrue("Blueprint found in the new library but not the old: " + bp,
                    oldBlueprints.containsKey(bp));
        }

        System.out.println("\n========\n");

        //_blueprintMapping, should be 288 one-way mappings of reprints and
        // AIs, as contained in the blueprintMappings.txt file
        //This might be obsolete, as the point of this field is to represent that file.
        var oldMappings = oldLib.getAllMappings();
        var newMappings = newLib.getAllMappings();
        assertEquals(oldMappings.size(), newMappings.size());

        //These mappings however are likely to fluctuate as development occurs.  Thus
        // we will instead track the differences and display them, rather than asserting on them.
        var mapDiffs = new HashSet<String>();

        for(var bp : oldMappings.keySet()) {
            if(!newMappings.containsKey(bp)) {
                RecordDiff(mapDiffs,
                "Mapping in old lib but not new: " + bp + " > " + oldMappings.get(bp));
            }
        }

        for(var bp : newMappings.keySet()) {
            if(!oldMappings.containsKey(bp)) {
                RecordDiff(mapDiffs,
                "Mapping in new lib but not old: " + bp + " > " + newMappings.get(bp));
            }
        }


        System.out.println("\n========\n");

        //_fullBlueprintMapping, should be 545 bi-directional associations
        // between cards and their reprints/errata/AIs
        var oldFullMapping = oldLib.getFullMappings();
        var newFullMapping = newLib.getFullMappings();
        assertEquals(oldFullMapping.size(), newFullMapping.size());

        //Even if the _mappings is eventually discarded as irrelevant, we will probably
        // always need a way of discovering associations between equivalent cards.
        var fullMapDiffs = new HashSet<String>();

        //First check that no mappings are missing in the new one
        for(var bp : oldFullMapping.keySet()) {
            if(!newFullMapping.containsKey(bp)) {
                RecordDiff(fullMapDiffs,
                        "Full mapping in old lib but not new: " + bp + " > "
                                + String.join(", ", oldFullMapping.get(bp)));
            }
            else {
                var newmaps = newFullMapping.get(bp);
                for(var mappedBP : oldFullMapping.get(bp)) {
                    if(!newmaps.contains(mappedBP)) {
                        RecordDiff(fullMapDiffs,
                        "Full map entry in old lib but not new: " + bp + " > " + mappedBP);
                    }
                }
            }
        }

        //Then check that no additional mappings were added to the new
        for(var bp : newFullMapping.keySet()) {
            if(!oldFullMapping.containsKey(bp)) {
                RecordDiff(fullMapDiffs,
                        "Full mapping in new lib but not old: " + bp + " > "
                                + String.join(", ", newFullMapping.get(bp)));
            }
            else {
                var oldmaps = oldFullMapping.get(bp);
                for(var mappedBP : newFullMapping.get(bp)) {
                    if(!oldmaps.contains(mappedBP)) {
                        RecordDiff(fullMapDiffs,
                        "Full map entry in new lib but not old: " + bp + " > " + mappedBP);
                    }
                }
            }
        }

        System.out.println("\n========\n");

        //_allSets, should be 59 sets as loaded from the setConfig.hjson file
        // (which should really be a folder search).  Unlike the mappings map,
        // this only starts as a representation of that file and gains additional
        // data, including the rarities nonsense.
        var oldSets = oldLib.getSetDefinitions();
        var newSets = newLib.getSetDefinitions();
        assertEquals(oldFullMapping.size(), newFullMapping.size());

        var setDiffs = new HashSet<String>();
        var setContentDiffs = new HashSet<String>();

        for(var setId : oldSets.keySet()) {
            if(!newSets.containsKey(setId)) {
                RecordDiff(setDiffs,
                        "Set in old lib but not new: " + setId );
            }
            else {
                var oldset = oldSets.get(setId);
                var newset = newSets.get(setId);

                var oldcards = oldset.getAllCards();
                var newcards = newset.getAllCards();

                for(var bp : oldcards) {
                    if(!newcards.contains(bp)) {
                        RecordDiff(setContentDiffs,
                                "Card in old set " + setId + " but not new: " + bp);
                    }
                }
            }
        }

        for(var setId : newSets.keySet()) {
            if(!oldSets.containsKey(setId)) {
                RecordDiff(setDiffs,
                        "Set in new lib but not old: " + setId);
            }
            else {
                var oldset = oldSets.get(setId);
                var newset = newSets.get(setId);

                var oldcards = oldset.getAllCards();
                var newcards = newset.getAllCards();

                for(var bp : newcards) {
                    if(!oldcards.contains(bp)) {
                        RecordDiff(setContentDiffs,
                                "Card in new set " + setId + " but not old: " + bp);
                    }
                }
            }
        }


        //Results

        System.out.println("\n\nLibrary Comparison results:\n\n========\n");
        System.out.println(oldBlueprints.size() - newBlueprints.size() + " total differences in card blueprints loaded.");
        System.out.println(mapDiffs.size() + " total differences in the bare mappings.");
        System.out.println(fullMapDiffs.size() + " total differences in the full mappings.");
        System.out.println(setDiffs.size() + " total differences in sets loaded.");
        System.out.println(setContentDiffs.size() + " total differences in set contents.");
        System.out.println("\n========");

        int total = mapDiffs.size() + fullMapDiffs.size() + setDiffs.size() + setContentDiffs.size();
        System.out.println("\nGrand total of differences: " + total);
        assertEquals(0, total);
    }
}
