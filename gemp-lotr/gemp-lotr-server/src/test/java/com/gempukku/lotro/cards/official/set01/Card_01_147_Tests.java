package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_147");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_147");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Assignment:</b> Exert this minion and spot a companion to prevent the opponent from assigning that companion to this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Uruk Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// "Prevent the opponent from assigning that companion to this minion": the opponent of the Guard's owner is
	// the Free Peoples player alone.  The Shadow player may still assign his own Guard to the companion spotted, among
	// the minions left over once the Free Peoples player has assigned (F-0490; Ketura, 2026-10-12).
	@Test
	public void TheShadowPlayerMayAssignTheGuardToTheCompanionSpottedThoughTheFreePeoplesPlayerMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Aragorn is spotted.
		scn.ShadowUseCardAction(guard);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsPass();
		scn.ShadowPass();

		// The Free Peoples player may not assign Aragorn to the Guard; Frodo he may.
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, guard));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, guard));
		scn.FreepsDeclineAssignments();

		// The Guard is left over, and the Shadow player assigns it to Aragorn.
		assertTrue(scn.ShadowCanAssignAgainst(aragorn, guard));
		scn.ShadowAssignToMinions(aragorn, guard);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, guard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
