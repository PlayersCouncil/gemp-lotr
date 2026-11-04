package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hearth", "17_8");

					// Hunters of two cultures and both sides.
					put("guardian", "17_7");    // Elven Guardian: [elven] Elf, hunter 1; a maneuver heal used only on purpose
					put("legolas", "15_18");    // Legolas, Fleet-footed Hunter: [elven] Elf, hunter 1; a regroup ability used only on purpose
					put("gimli", "15_5");       // Gimli, Eager Hunter: [dwarven] Dwarf, damage +1 and hunter 3, no other text
					put("tracker", "15_171");   // Tracking Uruk: [uruk-hai] minion, hunter 1 is its whole text
					// No hunters.
					put("elf", "1_53");         // Lórien Elf: [elven] Elf companion with no keyword
					put("runner", "1_178");     // Goblin Runner: [moria] minion; its play trigger only

					// Cards that carry culture tokens.
					put("future", "11_26");     // Uncertain Future: [elven] support-area condition; a maneuver ability used only on purpose
					put("strands", "9_22");     // Strands of Elven Hair: [elven] support-area possession; a fellowship ability used only on purpose
					put("notched", "4_52");     // My Axe Is Notched: [dwarven] support-area condition; its texts need Gimli 4_x to win a skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HearthandHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Hearth and Hall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot an [elven] hunter and choose one: draw a card for each hunter you can spot or reinforce an [elven] token for each hunter you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hearth");

		assertEquals("Hearth and Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrawsACardForEachHunterOfAnyCultureOrSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var future = scn.GetFreepsCard("future");
		scn.MoveCompanionsToTable("guardian", "gimli", "elf");
		scn.MoveCardsToSupportArea(future);
		scn.AddTokensToCard(future, 1);
		scn.MoveCardsToHand(hearth);
		assertEquals(1, scn.GetCultureTokensOn(future));
		assertInHand(hearth);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes.
		scn.MoveMinionsToTable("tracker", "runner");
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(hearth));
		scn.FreepsPlayCard(hearth);

		// Both can be done: a card to reinforce, and a deck to draw from.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Draw a card for each hunter"));
		assertTrue(scn.FreepsChoiceAvailable("Reinforce"));
		scn.FreepsChooseOption("Draw a card for each hunter");

		// The Guardian, Gimli and the Tracking Uruk are hunters; Lórien Elf, Frodo and the Goblin Runner are not.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInDiscard(hearth);
		assertEquals(1, scn.GetCultureTokensOn(future));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReinforcesAnElvenTokenForEachHunterOneCardAtATime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var future = scn.GetFreepsCard("future");
		var strands = scn.GetFreepsCard("strands");
		var notched = scn.GetFreepsCard("notched");
		scn.MoveCompanionsToTable("guardian", "legolas", "elf");
		scn.MoveCardsToSupportArea(future, strands, notched);
		scn.AddTokensToCard(future, 1);
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(notched, 1);
		scn.MoveCardsToHand(hearth);
		assertEquals(1, scn.GetCultureTokensOn(future));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(notched));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable("runner");
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(hearth);
		scn.FreepsChooseOption("Reinforce");

		// Two hunters, the Guardian and Legolas: two tokens, each chosen for in turn.
		// A card with an [elven] token, condition or possession; not one with a [dwarven] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(future, strands));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(notched));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(future);
		assertEquals(2, scn.GetCultureTokensOn(future));

		assertTrue(scn.FreepsHasCardChoiceAvailable(future, strands));
		scn.FreepsChooseCard(strands);
		assertEquals(2, scn.GetCultureTokensOn(future));
		assertEquals(3, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(hearth);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElvenHunterCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var legolas = scn.GetFreepsCard("legolas");
		var future = scn.GetFreepsCard("future");
		// An [elven] Elf who is no hunter, and a [dwarven] hunter.
		scn.MoveCompanionsToTable("elf", "gimli");
		scn.MoveCardsToSupportArea(future);
		scn.AddTokensToCard(future, 1);
		scn.MoveCardsToHand(hearth);
		assertEquals(1, scn.GetCultureTokensOn(future));
		assertInHand(hearth);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// A hunter of the Shadow player's, of another culture.
		scn.MoveMinionsToTable("tracker");
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hearth));

		scn.MoveCompanionsToTable(legolas);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(hearth));
	}

	@Test
	public void WithAnEmptyDrawDeckTheTokensAreReinforcedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var future = scn.GetFreepsCard("future");
		scn.MoveCompanionsToTable("guardian");
		scn.MoveCardsToSupportArea(future);
		scn.AddTokensToCard(future, 1);
		scn.MoveCardsToHand(hearth);
		assertEquals(1, scn.GetCultureTokensOn(future));
		assertInHand(hearth);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable("runner");
		scn.SetFreepsDrawDeck();
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		scn.FreepsPlayCard(hearth);

		// The draw cannot be done, so it is no branch; the one token goes on the one card that can take it.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetCultureTokensOn(future));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(hearth);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoElvenTokenToReinforceItDrawsWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var notched = scn.GetFreepsCard("notched");
		scn.MoveCompanionsToTable("guardian", "gimli");
		scn.MoveCardsToSupportArea(notched);
		// A [dwarven] token, which "an [elven] token" does not reach.
		scn.AddTokensToCard(notched, 1);
		scn.MoveCardsToHand(hearth);
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertInHand(hearth);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable("runner");
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(hearth);

		// Nothing to reinforce, so the draw is no choice: the Guardian and Gimli, two cards.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertInDiscard(hearth);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
