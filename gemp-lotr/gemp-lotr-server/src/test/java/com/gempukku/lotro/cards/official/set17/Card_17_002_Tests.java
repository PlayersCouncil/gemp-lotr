package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_2");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOrcCardsInTheDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balin", "17_2");
					put("guard1", "1_7");           // Dwarf Guard: a Dwarf companion who is not unique; no text in play
					put("guard2", "1_7");

					// Prowling Orc: an [orc] Orc, cost 3, site 4; lurker, and a skirmish ability
					put("prowler", "11_136");
					// [orc] cards of two types for the discard pile, where neither does anything
					put("underling", "11_125");     // Isengard Underling: minion
					put("demoralized", "11_114");   // Demoralized: condition
					// Goblin Runner: an Orc of the [moria] culture, cost 1; its "may add (2)" comes without asking
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BalinAvengedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Balin Avenged
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, you may add a [dwarven] token here. While you can spot 4 [dwarven] tokens and 2 Dwarves, each [orc] Orc gains this text "To play, remove an [orc] card from your discard pile from the game."<br><b>Skirmish:</b> Discard this condition to exert a minion for each Dwarf you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Balin Avenged", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFourTokensAndTwoDwarvesAnOrcIsPlayedOnlyByRemovingAnOrcCardInItsOwnersDiscardPileFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOrcCardsInTheDiscardPileScenario();

		var balin = scn.GetFreepsCard("balin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(balin);
		scn.AddTokensToCard(balin, 4);

		var prowler = scn.GetShadowCard("prowler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(prowler);
		scn.MoveCardsToDiscard(underling, demoralized, runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetCultureTokensOn(balin));
		assertTrue(scn.ShadowPlayAvailable(prowler));
		int twilight = scn.GetTwilight();

		// One of the two [orc] cards in his discard pile is to be removed; the Orc of another culture is no
		// candidate.
		scn.ShadowPlayCard(prowler);
		assertTrue(scn.ShadowHasCardChoiceAvailable(underling, demoralized));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(demoralized);
		assertInZone(Zone.REMOVED, demoralized);
		assertInDiscard(underling, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, prowler);
		// 3 to play + 2 roaming (a site 4 minion at site 2)
		assertEquals(twilight - 5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcCardInItsOwnersDiscardPileAnOrcCannotBePlayedAndAnOrcOfAnotherCultureCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOrcCardsInTheDiscardPileScenario();

		var balin = scn.GetFreepsCard("balin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(balin);
		scn.AddTokensToCard(balin, 4);

		var prowler = scn.GetShadowCard("prowler");
		var runnerInHand = scn.GetShadowCard("runner1");
		var runnerInDiscard = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(prowler, runnerInHand);
		// A [moria] Orc is the only card in his discard pile: nothing there pays for an [orc] Orc.
		scn.MoveCardsToDiscard(runnerInDiscard);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(prowler));
		// The pool would pay for either; the [moria] Orc has no such text to pay for.
		assertTrue(scn.ShadowPlayAvailable(runnerInHand));

		scn.ShadowPlayCard(runnerInHand);
		assertInZone(Zone.SHADOW_CHARACTERS, runnerInHand);
		assertInDiscard(runnerInDiscard);
		assertInHand(prowler);
		assertFalse(scn.ShadowPlayAvailable(prowler));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
