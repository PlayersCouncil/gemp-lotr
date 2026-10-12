package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_038_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "53_38");
				}},
				// Site 2 is Ettenmoors (Shadow number 2), site 3 Rivendell Valley (Shadow number 0).
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Aragorn, Heir to the White City
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Ranger.
		 * Each time the fellowship moves during the fellowship phase, remove (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Heir to the White City", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void EachFellowshipPhaseMoveRemovesOneTwilightBeforeTheMoveAddsItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPass();

		// 3 in the pool - 1 for Aragorn + 2 for Ettenmoors + 2 for Frodo and Aragorn
		assertEquals(6, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.SetTwilight(2);

		scn.FreepsPass();

		// The second time: 2 in the pool - 1 for Aragorn + 0 for Rivendell Valley + 2 for Frodo and Aragorn
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoveInTheRegroupPhaseRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.SetTwilight(3);

		scn.FreepsChooseToMove();

		// 3 in the pool + 0 for Rivendell Valley + 2 for Frodo and Aragorn, and nothing removed
		assertEquals(5, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsRemovedFromAnEmptyPoolSinceTheMoveAddsItsTwilightAfterward() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		// 2 for Ettenmoors + 2 for Frodo and Aragorn; the (1) was due before any of it was added
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
