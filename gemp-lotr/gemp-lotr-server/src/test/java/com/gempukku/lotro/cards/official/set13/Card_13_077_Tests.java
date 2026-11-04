package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tradesman", "13_77");
					// Noble Leaders and Garrison of Osgiliath: unique [gondor] support-area conditions that take
					// [gondor] tokens as they are played (here they are cheated in, and the tokens added); their
					// skirmish abilities are never used.
					put("leaders", "7_112");
					put("garrison", "6_52");
					// Heirs of Gondor: a [gondor] support-area condition that takes a token as it is played; cheated
					// in, it has none.
					put("heirs", "13_69");
					// Stout and Strong: a [dwarven] support-area condition that takes [dwarven] tokens as Dwarves
					// win; its skirmish ability is never used.
					put("stout", "4_57");
					put("runner", "1_178");       // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TradesmanFromLebenninStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Tradesman From Lebennin
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: At the start of each fellowship phase, you may add (2) to reinforce a [gondor] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tradesman");

		assertEquals("Tradesman From Lebennin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseAdds2ToReinforceAGondorToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tradesman = scn.GetFreepsCard("tradesman");
		var leaders = scn.GetFreepsCard("leaders");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(tradesman);
		scn.MoveCardsToSupportArea(leaders, garrison, heirs, stout);
		// Two [gondor] cards with a [gondor] token each, one with none, and a [dwarven] card with a [dwarven] token.
		scn.AddTokensToCard(leaders, 1);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(stout, 1);
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tradesman));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaders, garrison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heirs, stout));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(leaders);

		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningAddsNothingAndReinforcesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tradesman = scn.GetFreepsCard("tradesman");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(tradesman);
		scn.MoveCardsToSupportArea(leaders);
		scn.AddTokensToCard(leaders, 1);
		assertEquals(1, scn.GetCultureTokensOn(leaders));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tradesman));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGondorTokenInPlayTheTwoIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tradesman = scn.GetFreepsCard("tradesman");
		var heirs = scn.GetFreepsCard("heirs");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(tradesman);
		scn.MoveCardsToSupportArea(heirs, stout);
		scn.AddTokensToCard(stout, 1);
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		scn.StartGame();

		// The (2) is a cost the player can always pay, so the offer is made with nothing to reinforce.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tradesman));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tradesman = scn.GetFreepsCard("tradesman");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(tradesman);
		scn.MoveCardsToSupportArea(leaders);
		scn.AddTokensToCard(leaders, 1);
		assertEquals(1, scn.GetCultureTokensOn(leaders));

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(leaders));
	}

	@Test
	public void OfferedAgainAtTheStartOfItsOwnersNextFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tradesman = scn.GetFreepsCard("tradesman");
		var leaders = scn.GetFreepsCard("leaders");

		scn.StartGame();
		// Laid after the first fellowship phase has begun, so that its first offer is the one on turn 3.
		scn.MoveCompanionsToTable(tradesman);
		scn.MoveCardsToSupportArea(leaders);
		scn.AddTokensToCard(leaders, 1);
		assertEquals(1, scn.GetCultureTokensOn(leaders));

		// The first player's next turn, at site 2: the second player's turn between has no offer for him.
		scn.SkipToSite(2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tradesman));
		scn.FreepsAcceptOptionalTrigger();

		// Leaders is the only card with a [gondor] token: it is chosen with nothing asked.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
