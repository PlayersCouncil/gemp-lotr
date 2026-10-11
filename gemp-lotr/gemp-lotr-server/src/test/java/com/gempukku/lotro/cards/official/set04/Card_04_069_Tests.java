package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_69");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Legolas who is no archer, and an Uruk Savage he beats without killing it.
	protected VirtualTableScenario GetLegolasScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("count", "4_69");
					// Legolas, Companion of the Ring: strength 6; stronger only at a battleground or forest site
					put("legolas", "11_21");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalCountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Final Count
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each time Legolas wins a skirmish, you may place an [elven] token on this card.<br>While you can spot X [elven] tokens on this card and the same number of [dwarven] tokens on My Axe Is Notched, Legolas is strength +X (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Final Count", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "Each time Legolas wins a skirmish, you may place an [elven] token on this card": a "you may" on a trigger
	// is asked and may be declined (Q-0021; Q-0220: G).  The definition placed the token unasked.  (My Axe Is
	// Notched 4_52 has no "may" and places its token unasked.)
	// F-0460.
	@Test
	public void PlacingTheTokenIsOfferedAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLegolasScenario();

		var count = scn.GetFreepsCard("count");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(count);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		// Legolas has won: the offer is made, and declined.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(count));
		assertEquals(0, scn.GetCultureTokensOn(count));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(count));

		// A second move, and Legolas beats the same minion again: the offer is made again, and taken.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(count));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FinalCountTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
