package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.common.Phase;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class GameRequestHandlerAutoPassTest {
    @Test
    public void readsThePhasesThePageWrote() {
        assertEquals(EnumSet.of(Phase.SHADOW, Phase.SKIRMISH), GameRequestHandler.parseAutoPassPhases("SHADOW0SKIRMISH"));
    }

    @Test
    public void anEmptyValueMeansNoPhase() {
        assertEquals(EnumSet.noneOf(Phase.class), GameRequestHandler.parseAutoPassPhases(""));
    }

    @Test
    public void skipsNamesThatAreNotPhases() {
        assertEquals(EnumSet.of(Phase.REGROUP), GameRequestHandler.parseAutoPassPhases("NOPE0REGROUP"));
    }
}
