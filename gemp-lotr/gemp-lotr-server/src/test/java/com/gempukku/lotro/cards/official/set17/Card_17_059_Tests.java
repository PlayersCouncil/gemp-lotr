package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "17_59");

					put("oath", "17_66");       // Wildman's Oath: [men] possession played to the support area; stacks a [men] minion discarded in the maneuver phase (declined); a skirmish ability never used here
					put("haradrim", "11_74");   // Detachment of Haradrim: a [men] minion to lie stacked on a possession
					put("corps", "11_73");      // Corps of Harad: a second [men] minion to lie stacked on a possession
					put("runner", "1_178");     // Goblin Runner: a [moria] minion to lie stacked on a possession, not counted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +2 for each [men] possession with a [men] minion stacked on it.<br><b>Maneuver:</b> Spot X [men] minions stacked on a [men] possession and discard this minion to add (X).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("Sunland Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheNumberOfMinionsToSpotComesFilledInWithAllStackedOnMenPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var oath = scn.GetShadowCard("oath");
		var haradrim = scn.GetShadowCard("haradrim");
		var corps = scn.GetShadowCard("corps");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToSupportArea(oath);
		scn.StackCardsOn(oath, haradrim, corps, runner);
		scn.MoveMinionsToTable(trooper);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(trooper));
		scn.ShadowUseCardAction(trooper);

		// Two [men] minions stacked on a [men] possession; the [moria] Orc beside them is not one.  The box comes
		// filled in with both (Q-0341: A, as F-0561), and the player accepts it.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// The Trooper is a [men] minion discarded in the maneuver phase: the Oath may stack him, and is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, trooper);
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
