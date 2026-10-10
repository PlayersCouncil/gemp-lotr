package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_178_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Runner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Goblin Runner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "When you play this minion, you may add (2)": the game adds the 2 without asking, and is meant to (F-0009).
	// Read here as the first of the old `at` tests read it in passing (dwarvenAxeDoesNotFreeze, 2011), with the
	// Runner roaming.
	@Test
	public void PlayedWhileRoamingItPaysItsCostAndThePenaltyAndAddsItsTwoWithoutBeingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("card");
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.FreepsPass();

		// Site 2, and the Runner's site number is 4.  The move left 2 for the site and 1 for the Ring-bearer.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(runner));

		scn.ShadowPlayCard(runner);

		// 3 - 1 to play - 2 roaming + the 2 it adds
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
