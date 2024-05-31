package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_028_Tests
{
	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("wielder", "2_28");
                    put("gandalf", "1_364");

                    put("runner", "1_178");

                }}
		);
	}

	@Test
	public void WielderoftheFlameStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Wielder of the Flame
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver

		 * Game Text: <b>Spell</b>.<br><b>Maneuver:</b> Spot Gandalf to make a companion <b>defender +1</b> until the regroup phase. Any Shadow player may remove (3) to prevent this.
		*/

		var scn = GetScenario();

        var card = scn.GetFreepsCard("wielder");

		assertEquals("Wielder of the Flame", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.hasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void WielderoftheFlameTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

    @Test
    public void WielderAbilityCanBePrevented() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var wielder = scn.GetFreepsCard("wielder");
        var gandalf = scn.GetFreepsCard("gandalf");
        scn.FreepsMoveCharToTable(gandalf);
        scn.FreepsMoveCardToHand(wielder);

        PhysicalCardImpl runner = scn.GetShadowCard("runner");
        scn.ShadowMoveCharToTable(runner);

        scn.StartGame();
        scn.FreepsPassCurrentPhaseAction();

        scn.SkipToPhase(Phase.MANEUVER);


        scn.FreepsPlayCard(wielder);
        assertEquals(4, scn.GetTwilight());
        scn.ShadowAcceptOptionalTrigger();
        assertEquals(1, scn.GetTwilight());
        assertFalse(scn.FreepsAnyActionsAvailable());

    }



}
