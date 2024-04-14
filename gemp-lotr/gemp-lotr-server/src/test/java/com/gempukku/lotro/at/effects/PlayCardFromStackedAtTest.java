package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayCardFromStackedAtTest extends AbstractAtTest {
    @Test
    public void playFromGoblinSwarms() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl goblinSwarms = createCard(P2, "1_183");
        _game.getGameState().addCardToZone(_game, goblinSwarms, Zone.SUPPORT);

        final PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().stackCard(_game, goblinRunner, goblinSwarms);

        _game.getGameState().setTwilight(20);

        // Fellowship phase
        playerDecided(P1, "");

        playerDecided(P2, getCardActionId(P2, "Use Goblin Swarms"));

        assertEquals(Zone.SHADOW_CHARACTERS, goblinRunner.getZone());
    }

    @Test
    public void playFromControlledSite() throws Exception {
        initializeSimplestGame();
        skipMulligans();

        final PhysicalCardImpl dunlendingLooter = createCard(P2, "4_11");

        PhysicalCard firstSite = _game.getGameState().getSite(1);
        _game.getGameState().takeControlOfCard(P2, _game, firstSite, Zone.SUPPORT);
        _game.getGameState().stackCard(_game, dunlendingLooter, firstSite);

        _game.getGameState().setTwilight(1);

        // Fellowship phase
        playerDecided(P1, "");

        // We need 3 (card cost) + 2 (roaming penalty) - 2 (discount from stacked)
        assertEquals(3, _game.getGameState().getTwilightPool());

        playerDecided(P2, getCardActionId(P2, "Use Dunlending"));

        assertEquals(Zone.SHADOW_CHARACTERS, dunlendingLooter.getZone());
        assertEquals(0, _game.getGameState().getTwilightPool());
    }
}
