package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_192_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pinned", "1_192");

					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, cost 3, site 4; Archer is its whole text
					put("bowman", "2_60");      // Goblin Bowman: [moria] Orc, strength 4; Archer is its whole text
					put("runner", "1_178");     // Goblin Runner: a [moria] minion who is no archer; its play trigger never fires, as it is cheated in
					put("crossbow", "4_182");   // Uruk Crossbow Troop: an [isengard] archer; its play trigger never fires, as it is cheated in

					// Two archer companions, so that the fellowship archery total is 2 and -1 is told from a total of 0.
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: Archer; stronger only against a Nazgul
					put("garrison", "8_36");    // Garrison of Gondor: Knight, Archer; defender +1 only with a fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PinnedDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pinned Down
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>While you can spot a [moria] archer, the fellowship archery total is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pinned");

		assertEquals("Pinned Down", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipArcheryTotalIsOneLessOnceAMoriaArcherIsPlayedAndNotForOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pinned = scn.GetShadowCard("pinned");
		var marksman = scn.GetShadowCard("marksman");
		var runner = scn.GetShadowCard("runner");
		var crossbow = scn.GetShadowCard("crossbow");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("garrison"));
		scn.MoveCardsToHand(marksman);

		scn.StartGame();
		scn.MoveCardsToSupportArea(pinned);
		scn.MoveMinionsToTable(runner, crossbow);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, pinned);
		assertInZone(Zone.SHADOW_CHARACTERS, runner, crossbow);
		scn.FreepsPass();

		// A [moria] minion who is no archer and an archer of another culture: the two archers' total stands.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.ShadowPlayCard(marksman);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		// The minion archery total (the Marksman and the Crossbow Troop) is not touched.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnotherMoriaArcherCountsAndASecondDoesNotCountAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pinned = scn.GetShadowCard("pinned");
		var bowman = scn.GetShadowCard("bowman");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("garrison"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(pinned);
		assertInZone(Zone.SUPPORT, pinned);
		scn.FreepsPass();

		// No [moria] archer at all.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Goblin Bowman is a [moria] archer too.
		scn.MoveMinionsToTable(bowman);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// A second [moria] archer does not make it -2.
		scn.MoveMinionsToTable(marksman);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
