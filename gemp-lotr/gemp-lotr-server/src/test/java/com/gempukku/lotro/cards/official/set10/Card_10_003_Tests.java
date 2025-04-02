package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_003_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("more", "10_3");
					put("gimli", "1_12");
					put("runner", "1_178");
					put("savage", "1_151");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void MoreYettoComeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: More Yet to Come
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a Dwarf kills a minion in a skirmish and that minion did not take all wounds caused by that Dwarf's damage bonus, assign those remaining wounds to minions not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("more");

		assertEquals("More Yet to Come", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	//Migrated from legacy AT
	@Test
	public void MoreYetToComeAppliesExtraDamageToMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var more = scn.GetFreepsCard("more");
		scn.FreepsMoveCharToTable(gimli);
		scn.FreepsMoveCardToHand(more);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.ShadowMoveCharToTable(runner, savage);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassCurrentPhaseActions();

		assertEquals(Zone.DISCARD, runner.getZone());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(savage));
	}
}
