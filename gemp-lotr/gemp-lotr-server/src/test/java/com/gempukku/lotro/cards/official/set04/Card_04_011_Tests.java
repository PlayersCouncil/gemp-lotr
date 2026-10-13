package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("looter", "4_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingLooterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Looter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When this minion wins a skirmish, you may stack him on a site you control.<br><b>Shadow:</b> If stacked on a site you control, play this minion. His twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dunlending Looter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StackedOnASiteYouControlHeIsPlayedInTheShadowPhaseForTwoLessAndStillPaysRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedScenario();

		// Player 2's first turn, so that a fellowship stands at site 2 with both markers off site 1.
		// Player 1 is the Shadow player there, and the Looter is his.
		// The sum is the old at test's (playFromControlledSite).  No game has him stacked this early: he is
		// stacked by winning a skirmish, and his owner's next Shadow phase is at site 3 or later, where he
		// does not roam (Q-0230: keep or strike the roaming half).
		var looter = scn.GetFreepsCard("looter");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));

		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.StackCardsOn(site1, looter);
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertInZone(Zone.STACKED, looter);

		// 3 for the Looter + 2 roaming (his site number is 3, the fellowship is at site 2) - 2 by his own
		// text: 3.  One short of that he is not offered.
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertFalse(scn.FreepsActionAvailable(looter));

		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(looter));

		scn.FreepsUseCardAction(looter);
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
