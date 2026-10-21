package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("closer", "8_109");

					// Unbound Hobbits of two cultures
					put("merry", "1_303");       // Merry, From O'er the Brandywine: [shire]; +2 only while he bears a weapon
					put("pippin", "103_31");     // Pippin, Trollslayer: a [gondor] Hobbit; a skirmish ability used only on purpose
					// and those who are not
					put("sam", "1_311");         // Sam, Son of Hamfast: a Ring-bound Hobbit; abilities used only on purpose
					put("bounder", "1_286");     // Bounder: a Hobbit ally; a skirmish ability
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: unbound, no Hobbit; a maneuver ability

					// Cards for the hand: Free Peoples cards of three types, and Shadow cards
					put("gimli", "1_13");        // Gimli, Son of Gloin: a companion
					put("sword", "1_299");       // Hobbit Sword: a possession with no other text
					put("intuition", "1_296");   // Hobbit Intuition: a Skirmish event, not playable in the regroup phase
					put("runner", "1_178");      // Goblin Runner: a Shadow card the Free Peoples player cannot play
					put("scout", "1_191");       // Moria Scout: another
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CloserandCloserHeBentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Closer and Closer He Bent
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Regroup:</b> Exert an unbound Hobbit and add a burden to reveal your hand. Place each Free Peoples card revealed this way beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("closer");

		assertEquals("Closer and Closer He Bent", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsAnUnboundHobbitAndAddsABurdenToPlaceTheFreePeoplesCardsOfTheHandBeneathTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var closer = scn.GetFreepsCard("closer");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var intuition = scn.GetFreepsCard("intuition");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(closer);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetFreepsHand(gimli, sword, intuition, runner);
		assertInHand(gimli, sword, intuition, runner);
		assertEquals(4, scn.GetFreepsHandCount());
		var deckSize = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertInZone(Zone.SUPPORT, closer);

		assertTrue(scn.FreepsActionAvailable(closer));
		scn.FreepsUseCardAction(closer);

		// Unbound Hobbits of two cultures; not a Ring-bound Hobbit, a Hobbit ally, or an unbound companion who is
		// no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetBurdens());

		// The whole hand is shown to the Shadow player, and nothing to its owner.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// The three Free Peoples cards are placed one at a time; the Shadow card is not among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, sword, intuition));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(sword);

		assertEquals(intuition, scn.GetFreepsBottomOfDeck());
		assertEquals(sword, scn.GetFromBottomOfFreepsDeck(2));
		assertEquals(gimli, scn.GetFromBottomOfFreepsDeck(3));
		assertEquals(deckSize + 3, scn.GetFreepsDeckCount());
		assertInHand(runner);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, closer);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheOnlyUnboundHobbitIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var closer = scn.GetFreepsCard("closer");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(merry, sam, aragorn);
		scn.MoveCardsToSupportArea(closer);

		scn.StartGame();
		// Vitality 4: three wounds leave Merry exhausted.  Sam and Aragorn could exert, but neither is an unbound
		// Hobbit.
		scn.AddWoundsToChar(merry, 3);
		scn.MoveCardsToHand(gimli);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(gimli);
		assertInZone(Zone.SUPPORT, closer);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(closer));
	}

	@Test
	public void WithNoFreePeoplesCardInHandTheCostsArePaidAndNothingIsPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var closer = scn.GetFreepsCard("closer");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetFreepsCard("runner");
		var scout = scn.GetFreepsCard("scout");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(closer);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetFreepsHand(runner, scout);
		assertInHand(runner, scout);
		var deckSize = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(merry));

		assertTrue(scn.FreepsActionAvailable(closer));
		scn.FreepsUseCardAction(closer);

		// Merry is the one unbound Hobbit, and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertInHand(runner, scout);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deckSize, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
