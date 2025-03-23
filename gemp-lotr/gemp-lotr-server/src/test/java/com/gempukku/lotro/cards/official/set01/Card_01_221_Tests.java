package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_221_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("paleblade", "1_221");
					put("twk", "1_237");

					put("gimli", "1_13");
					put("condition1", "1_16");
					put("condition2", "1_21");
					put("condition3", "2_1");
					put("condition4", "2_9");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ThePaleBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Pale Blade
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be The Witch-king.<br>He is <b>damage +1</b>.<br><b>Response:</b> If The Witch-king wins a skirmish, exert him to discard a Free Peoples condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("paleblade");

		assertEquals("The Pale Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void ThePaleBladeCanBeTriggeredMultipleTimesFromSameVictory() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var twk = scn.GetShadowCard("twk");
		var paleblade = scn.GetShadowCard("paleblade");
		scn.ShadowMoveCharToTable(twk);
		scn.AttachCardsTo(twk, paleblade);

		var gimli = scn.GetFreepsCard("gimli");
		var condition1 = scn.GetFreepsCard("condition1");
		var condition2 = scn.GetFreepsCard("condition2");
		var condition3 = scn.GetFreepsCard("condition3");
		var condition4 = scn.GetFreepsCard("condition4");
		scn.FreepsMoveCharToTable(gimli);
		scn.FreepsMoveCardToSupportArea(condition1, condition2, condition3, condition4);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, twk);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(Zone.SUPPORT, condition1.getZone());
		assertEquals(Zone.SUPPORT, condition2.getZone());
		assertEquals(Zone.SUPPORT, condition3.getZone());
		assertEquals(Zone.SUPPORT, condition4.getZone());
		assertEquals(0, scn.GetWoundsOn(twk));

		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(twk));
		scn.ShadowChooseCard(condition1);
		assertEquals(Zone.DISCARD, condition1.getZone());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(twk));
		scn.ShadowChooseCard(condition2);
		assertEquals(Zone.DISCARD, condition2.getZone());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(twk));
		scn.ShadowChooseCard(condition3);
		assertEquals(Zone.DISCARD, condition3.getZone());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());

	}
}
