package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hollowing", "3_54");
					put("hollowing2", "3_54");
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, cost 2, site 4; a Shadow ability used only on purpose
					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc of another title, cost 4; a regroup ability only
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, no Orc; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HollowingofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hollowing of Isengard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] Orc. Plays to your support area.
		 * 	<b>Shadow:</b> Discard this condition to add (3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hollowing");

		assertEquals("Hollowing of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnIsengardOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hollowing, forger);

		scn.StartGame();
		// An [isengard] minion who is no Orc, and an Orc of another culture.
		scn.MoveMinionsToTable(savage, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hollowing));

		scn.ShadowPlayCard(forger);
		assertTrue(scn.ShadowPlayAvailable(hollowing));
		scn.ShadowPlayCard(hollowing);
		assertInZone(Zone.SUPPORT, hollowing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAnIsengardOrcOfAnotherTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var retainer = scn.GetShadowCard("retainer");
		scn.MoveCardsToHand(hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(retainer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hollowing));
		scn.ShadowPlayCard(hollowing);
		assertInZone(Zone.SUPPORT, hollowing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsThisCopyToAdd3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var hollowing2 = scn.GetShadowCard("hollowing2");
		scn.MoveCardsToSupportArea(hollowing, hollowing2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, hollowing, hollowing2);
		scn.FreepsPass();

		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(hollowing));
		assertTrue(scn.ShadowActionAvailable(hollowing2));

		scn.ShadowUseCardAction(hollowing);
		assertInDiscard(hollowing);
		assertInZone(Zone.SUPPORT, hollowing2);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(hollowing2));
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollowing = scn.GetShadowCard("hollowing");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hollowing);

		scn.StartGame();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, hollowing);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(hollowing));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hollowing));
		assertInZone(Zone.SUPPORT, hollowing);
	}
}
