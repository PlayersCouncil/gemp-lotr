package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_13_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_63");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetReinforceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brego", "13_63");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion to bear Brego
					// Two [gondor] conditions of the support area that carry [gondor] tokens; each has a
					// skirmish ability, never used.
					put("leaders", "7_112");    // Noble Leaders
					put("heirs", "13_69");      // Heirs of Gondor
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BregoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Brego, Loyal Steed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [gondor] companion.<br>When you play Brego, you may reinforce a [gondor] token.<br><b>Maneuver:</b> Exert bearer and remove 2 [gondor] tokens to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Brego", card.getBlueprint().getTitle());
		assertEquals("Loyal Steed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action that has no cost and would do nothing is not offered.  Brego's
	// reinforcing has no cost, and a token on a hindered card cannot be reinforced (CR 5.0 "hinder"), so with the
	// only [gondor] token on a hindered card there is nothing for it to do.  The old at/ test
	// (HinderTests.CannotReinforceCultureTokenOnHinderedCard) took the offer on this board and read the token
	// count unchanged; the definition was corrected for this test (F-0359).  The offer made, with tokens that
	// can be reinforced beside the hindered one, is read in
	// mechanics/TokensOnAHinderedCardTests.NoCultureTokenIsReinforcedOnAHinderedCard.
	@Test
	public void ReinforcingIsNotOfferedWhenTheOnlyGondorTokenIsOnAHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReinforceScenario();

		var brego = scn.GetFreepsCard("brego");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leaders = scn.GetFreepsCard("leaders");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(leaders, heirs);
		scn.MoveCardsToHand(brego);

		scn.StartGame();
		// Heirs of Gondor has no token on it; Noble Leaders has the only one, and is hindered.
		scn.AddTokensToCard(leaders, 1);
		scn.HinderCard(leaders);

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(brego);
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(brego, aragorn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brego));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
	}
}
