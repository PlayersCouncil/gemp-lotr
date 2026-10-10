package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_316_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_316");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");
					// Desperate Defense of the Ring: [sauron] condition borne by a companion.  "Each time bearer is
					// assigned to a skirmish, the Free Peoples player chooses to either discard 3 cards from hand or
					// add a burden."
					put("defense", "1_244");
					put("runner", "1_178");     // Goblin Runner: the minion the bearer is assigned to
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: he bears Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand."
					put("glamdring", "1_75");
					// Eye of Barad-dûr: [sauron] event.  "Response: If a Free Peoples player reveals this card from
					// your hand, discard this card to add 2 burdens."  A Shadow card that adds two burdens at once.
					put("eye", "5_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Sam, Samwise the Brave
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bound</b>.<br><b>Response:</b> If a Shadow card is about to add any number of burdens, spot Frodo and exert Sam to prevent this.<br><b>Response:</b> If Frodo is killed, make Sam the <b>Ring-bearer (resistance 5)</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Samwise the Brave", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void PreventsTheBurdenOfAShadowCardThatTheFreePeoplesPlayerChoseToAdd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		// Four cards in hand, his own copies of cards that stay out of play here: "discard 3 cards from hand"
		// can be done, so the choice between it and the burden is really put to the player.
		scn.MoveCardsToFreepsHand("gandalf", "glamdring", "runner", "eye");

		scn.StartGame();
		scn.AttachCardsTo(frodo, defense);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsAssignToMinions(frodo, runner);

		assertEquals(List.of("Discard 3 cards from hand", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a burden");

		// The Free Peoples player picked the burden, but the card that adds it is the Shadow player's: Sam may
		// answer it (CR 4.2 "effect": "The source of an effect is the card on which that effect is printed").
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		// The burden that was chosen and prevented is not paid for with the discard after all.
		assertEquals(4, scn.GetFreepsHandCount());
		assertAttachedTo(defense, frodo);
		// The assignment stands and the game goes on with it.
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithTheResponseDeclinedTheBurdenOfTheShadowCardIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToFreepsHand("gandalf", "glamdring", "runner", "eye");

		scn.StartGame();
		scn.AttachCardsTo(frodo, defense);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(frodo, runner);
		assertEquals(List.of("Discard 3 cards from hand", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a burden");

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		// The same board with Sam holding back: the burden he could have stopped is there.
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void OneExertionPreventsBothBurdensOfAShadowCardThatAddsTwoAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		// A second card in the Shadow hand, so that the Eye is one of the cards revealed and not the hand.
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, runner);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowPlayAvailable(eye));
		scn.ShadowPlayCard(eye);

		// "Any number of burdens": the Eye is about to add 2.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		// One exertion, and neither burden is added (the Eye's own file reads the 2 it adds unanswered).
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(eye);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
