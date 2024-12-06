package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_007_Tests
{
	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("there", "101_7");
                    put("arwen", "1_30");
                    put("aragorn", "1_89");
                    put("tale", "1_66");
                    put("saga", "1_114");

                    put("orc1", "1_191");
                }},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void IWasThereStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: I Was There
		 * Side: Free Peoples
		 * Culture: elven
		 * Twilight Cost: 1
		 * Type: event
		 * Subtype: Skirmish
		 * Game Text: Spot a minion skirmishing an Elf and discard an [elven] tale to wound that minion.
		 */

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl there = scn.GetFreepsCard("there");

		assertFalse(there.getBlueprint().isUnique());
		assertTrue(scn.hasKeyword(there, Keyword.TALE)); // test for keywords as needed
		assertEquals(1, there.getBlueprint().getTwilightCost());
		assertEquals(CardType.EVENT, there.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(there, Timeword.SKIRMISH));
		assertEquals(Culture.ELVEN, there.getBlueprint().getCulture());
		assertEquals(Side.FREE_PEOPLE, there.getBlueprint().getSide());
	}

	@Test
	public void IWasThereDoesNotTriggerIfNoSkirmishingElves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl there = scn.GetFreepsCard("there");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl tale = scn.GetFreepsCard("tale");
		PhysicalCardImpl saga = scn.GetFreepsCard("saga");
		scn.FreepsMoveCardToHand(there, arwen, aragorn, tale, saga);

		PhysicalCardImpl orc1 = scn.GetShadowCard("orc1");
		scn.ShadowMoveCharToTable(orc1);

		scn.StartGame();
		scn.FreepsPlayCard(arwen);
		scn.FreepsPlayCard(aragorn);
		scn.FreepsPlayCard(saga);
		scn.FreepsPlayCard(tale);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, orc1);
		assertEquals(Zone.HAND, there.getZone());
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.FreepsActionAvailable("I Was There"));
	}

	@Test
	public void IWasThereDoesNotTriggerIfNoElvenTales() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl there = scn.GetFreepsCard("there");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl saga = scn.GetFreepsCard("saga");
		scn.FreepsMoveCardToHand(there, arwen, aragorn, saga);

		PhysicalCardImpl orc1 = scn.GetShadowCard("orc1");
		scn.ShadowMoveCharToTable(orc1);

		scn.StartGame();
		scn.FreepsPlayCard(arwen);
		scn.FreepsPlayCard(aragorn);
		scn.FreepsPlayCard(saga);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(arwen, orc1);
		assertEquals(Zone.HAND, there.getZone());
		scn.FreepsResolveSkirmish(arwen);
		assertFalse(scn.FreepsActionAvailable("I Was There"));
	}

	@Test
	public void IWasThereTriggersIfElvenTaleAndElfSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl there = scn.GetFreepsCard("there");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl tale = scn.GetFreepsCard("tale");
		scn.FreepsMoveCardToHand(there, arwen, tale);

		PhysicalCardImpl orc1 = scn.GetShadowCard("orc1");
		scn.ShadowMoveCharToTable(orc1);

		scn.StartGame();
		scn.FreepsPlayCard(arwen);
		scn.FreepsPlayCard(tale);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(arwen, orc1);
		assertEquals(Zone.HAND, there.getZone());
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.FreepsActionAvailable("I Was There"));
		assertEquals(0, scn.GetWoundsOn(orc1));
		scn.FreepsPlayCard(there);
		// Tale of Gil-galad should now be in the discard pile as a cost
		assertEquals(Zone.DISCARD, tale.getZone());
		assertEquals(1, scn.GetWoundsOn(orc1));
	}
}
