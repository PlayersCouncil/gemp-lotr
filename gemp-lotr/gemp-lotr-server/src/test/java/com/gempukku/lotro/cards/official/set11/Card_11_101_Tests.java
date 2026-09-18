package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breelander", "11_101");
					put("lost", "11_212");      // Lost in the Woods: support-area condition with no bearer definition
					put("alliance1", "1_49");   // The Last Alliance of Elves and Men: bearer must be a [gondor] Man
					put("alliance2", "1_49");
					put("boromir", "1_97");
					put("aragorn", "1_89");
					put("faramir", "4_117");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwarthyBreelanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Swarthy Bree-lander
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At the start of the maneuver phase, you may remove (2) to transfer a condition borne by a character to another eligible bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breelander");

		assertEquals("Swarthy Bree-lander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverTriggerTransfersAConditionToAnotherEligibleBearerButNotOneWithNoBearerDefinition() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1025: Lost in the Woods (played to the support area, only ever attached by its own
		// skirmish-phase transfer) has no "eligible bearer" at all, so it must not be selectable here. Previously it
		// was selectable and the null bearer filter crashed the game when a destination was evaluated.
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var alliance1 = scn.GetFreepsCard("alliance1");
		var alliance2 = scn.GetFreepsCard("alliance2");
		scn.MoveCompanionsToTable(boromir, aragorn, faramir);
		scn.AttachCardsTo(boromir, alliance1);
		scn.AttachCardsTo(aragorn, alliance2);

		var breelander = scn.GetShadowCard("breelander");
		var lost = scn.GetShadowCard("lost");
		scn.MoveMinionsToTable(breelander);
		scn.AttachCardsTo(faramir, lost);

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(10);
		scn.ShadowPassCurrentPhaseAction();          // start of maneuver

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(8, scn.GetTwilight());

		assertTrue(scn.ShadowHasCardChoiceAvailable(alliance1, alliance2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lost));
		scn.ShadowChooseCard(alliance1);

		// Boromir is the current bearer and Aragorn already bears one, so Faramir is the only eligible bearer
		assertEquals(faramir, alliance1.getAttachedTo());
		assertEquals(aragorn, alliance2.getAttachedTo());
		assertEquals(faramir, lost.getAttachedTo());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
