package com.gempukku.lotro.game.formats;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/** The set ranges and anchor ids the Play popup's format (i) and Help › Format Definitions show. */
public class FormatSummaryTest {
    @Test
    public void contiguousSetsCollapseToARange() {
        assertEquals("Cards from sets 1-3", FormatSummary.describeSets(List.of("1", "2", "3")));
        assertEquals("Cards from sets 1-10", FormatSummary.describeSets(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")));
    }

    @Test
    public void supplementsAreVSetsAndNeverJoinTheNumberedRun() {
        assertEquals("Cards from sets 1-3, V1", FormatSummary.describeSets(List.of("1", "2", "3", "101")));
        assertEquals("Cards from sets 1-10, V1-V3",
                FormatSummary.describeSets(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "101", "102", "103")));
        assertEquals("Cards from sets 0-19, V0-V3", FormatSummary.describeSets(List.of(
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "100", "101", "102", "103")));
    }

    @Test
    public void gapsPairsAndOrder() {
        assertEquals("Cards from sets 1, 2, 7-9", FormatSummary.describeSets(List.of("9", "8", "7", "2", "1")));
        assertEquals("Cards from sets 4-6, 11-19", FormatSummary.describeSets(List.of(
                "4", "5", "6", "11", "12", "13", "14", "15", "16", "17", "18", "19")));
        assertEquals("duplicates count once", "Cards from sets 1-3", FormatSummary.describeSets(List.of("1", "2", "2", "3")));
        assertEquals("Cards from sets 1-3, V1, 151", FormatSummary.describeSets(List.of("1", "2", "3", "101", "151")));
    }

    @Test
    public void oneSetNoneOrJunk() {
        assertEquals("Cards from set 1", FormatSummary.describeSets(List.of("1")));
        assertEquals("Cards from set V0", FormatSummary.describeSets(List.of("100")));
        assertNull(FormatSummary.describeSets(List.of()));
        assertNull(FormatSummary.describeSets(null));
        assertEquals("Cards from sets 1, 2", FormatSummary.describeSets(List.of("1", " 2 ", "x")));
    }

    @Test
    public void setLabelsWriteSupplementsAsVSets() {
        assertEquals("1", FormatSummary.setLabel(1));
        assertEquals("V0", FormatSummary.setLabel(100));
        assertEquals("V3", FormatSummary.setLabel(103));
    }

    @Test
    public void anchorIdsAreSafeFragments() {
        assertEquals("format-pc_fotr_block", FormatSummary.anchorId("pc_fotr_block"));
        assertEquals("format-pre-hunters_expanded", FormatSummary.anchorId("pre-hunters_expanded"));
        assertEquals("format-a-b--c", FormatSummary.anchorId("a b\"<c"));
    }
}
