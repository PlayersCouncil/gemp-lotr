package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_012_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("three", "101_12");

					// The three rings of set 3.  Each makes its bearer vitality +1.
					// Vilya: [elven] ring, bearer must be Elrond.  A maneuver ability, never used here.
					put("vilya", "3_27");
					// Nenya: [elven] ring, bearer must be Galadriel.  An optional trigger at the start of the
					// regroup phase, which no test reaches with Nenya in play.
					put("nenya", "3_23");
					// Narya: [gandalf] ring, bearer must be Gandalf.  An optional trigger at the start of each
					// turn, so it is put down or played after the game has started.
					put("narya", "3_34");
					// Ring of Barahir: [gondor] ring, bearer must be Aragorn.  An optional trigger at the start of
					// each turn, so it too is put down after the game has started.
					put("barahir", "6_55");
					// Phial of Galadriel, Star-glass: [elven] artifact that is not a ring, bearer must be a
					// Hobbit.  A skirmish ability, never used here.
					put("phial", "10_13");
					// Gandalf's Staff, Focus of Power: [gandalf] artifact that is not a ring.  Gandalf is
					// strength +1 while there are 4 twilight tokens.
					put("staff", "7_38");

					// Elrond, Lord of Rivendell: [elven] Elf ally.  Heals site 3 allies at the start of the turn
					// and has a fellowship ability, neither of which comes up.
					put("elrond", "1_40");
					// Galadriel, Lady of Light: [elven] Elf ally.  Heals site 6 allies at the start of the turn
					// and has a fellowship ability, neither of which comes up.
					put("galadriel", "1_45");
					// Gandalf, The Grey Wizard: [gandalf] Wizard companion, vitality 4.  A fellowship ability,
					// never used here.
					put("gandalf", "1_364");
					// Aragorn, Ranger of the North: [gondor] Man companion, vitality 4.  A maneuver ability.
					put("aragorn", "1_89");

					// Cards to hold, to draw and to leave lying about.
					put("guard", "1_7");        // Dwarf Guard: a Free Peoples card for the hand
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow card for the hand, no text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: put on top of the draw deck
					put("marksman", "1_176");   // Goblin Marksman: second from the top, or in the discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThreeForTheElvenKingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Three for the Elven-Kings
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Tale.
		 * Each time the fellowship moves, you may spot an [elven] or [gandalf] ring to place a card from hand
		 * beneath your draw deck and draw a card (and heal a companion if you can spot 3 [elven] or [gandalf]
		 * rings).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("three");

		assertEquals("Three for the Elven-Kings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		scn.MoveCardsToHand(three);

		scn.StartGame();

		assertInHand(three);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		// Nothing has to be spotted to play it: there is no ring but The One Ring.
		assertTrue(scn.FreepsPlayAvailable(three));

		scn.FreepsPlayCard(three);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, three);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingWithAnElvenRingPlacesAChosenCardFromHandBeneathTheDrawDeckAndDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveCardsToSupportArea(three);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(vilya, guard, troop);
		scn.MoveCardsToDiscard(marksman);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(raiders);

		// The ring is played, not cheated in: it is what the trigger spots.
		scn.FreepsPlayCard(vilya);
		assertAttachedTo(vilya, elrond);

		int deckCount = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertSame(raiders, scn.GetFreepsTopOfDeck());
		assertNotSame(guard, scn.GetFreepsBottomOfDeck());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		scn.FreepsAcceptOptionalTrigger();
		// Any card in hand, of either side.  The top card of the draw deck is not in hand yet: the card is
		// placed first and drawn second.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(raiders, marksman));

		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DECK, guard);
		assertSame(guard, scn.GetFreepsBottomOfDeck());
		assertInHand(raiders, troop);
		// One out and one in, both in the hand and in the deck.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertInDiscard(marksman);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AGandalfRingIsSpottedTooAndTheTriggerComesAgainOnASecondMoveInTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var gandalf = scn.GetFreepsCard("gandalf");
		var narya = scn.GetFreepsCard("narya");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		var marksman = scn.GetFreepsCard("marksman");
		scn.MoveCardsToSupportArea(three);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(guard, troop);

		scn.StartGame();

		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToTopOfDeck(marksman);
		scn.MoveCardsToTopOfDeck(raiders);

		int deckCount = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertSame(raiders, scn.GetFromTopOfFreepsDeck(1));
		assertSame(marksman, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(1, scn.GetCurrentSiteNumber());

		// The move from site 1 to site 2.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, troop));
		scn.FreepsChooseCard(guard);
		assertSame(guard, scn.GetFreepsBottomOfDeck());
		assertInHand(troop, raiders);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minions, so the turn goes on to the regroup phase and the choice of a second move.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		// The move from site 2 to site 3.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, raiders));
		scn.FreepsChooseCard(troop);
		// The second card goes beneath the first.
		assertSame(troop, scn.GetFromBottomOfFreepsDeck(1));
		assertSame(guard, scn.GetFromBottomOfFreepsDeck(2));
		assertInHand(raiders, marksman);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deckCount, scn.GetFreepsDeckCount());

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyRingsAreOfOtherCulturesAndTheOnlyElvenAndGandalfArtifactsAreNotRings() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		var phial = scn.GetFreepsCard("phial");
		var staff = scn.GetFreepsCard("staff");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToSupportArea(three);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(frodo, phial);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(guard, troop);

		scn.StartGame();

		// A [gondor] ring, The One Ring, an [elven] artifact and a [gandalf] artifact: each is half of what
		// the text asks for.
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveCardsToTopOfDeck(raiders);

		assertAttachedTo(scn.GetRing(), frodo);
		int deckCount = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.FreepsPassCurrentPhaseAction();
		// The fellowship has moved and nothing was offered.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInHand(guard, troop);
		assertEquals(2, scn.GetFreepsHandCount());
		assertSame(raiders, scn.GetFreepsTopOfDeck());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheHandAndTheDrawDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToSupportArea(three);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToHand(guard, troop);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(raiders);

		int deckCount = scn.GetFreepsDeckCount();
		var bottom = scn.GetFreepsBottomOfDeck();
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		scn.FreepsDeclineOptionalTrigger();
		assertInHand(guard, troop);
		assertEquals(2, scn.GetFreepsHandCount());
		assertSame(raiders, scn.GetFreepsTopOfDeck());
		assertSame(bottom, scn.GetFreepsBottomOfDeck());
		assertEquals(deckCount, scn.GetFreepsDeckCount());

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCardInHandTheTriggerIsStillOfferedAndStillDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToSupportArea(three);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(raiders);

		int deckCount = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());

		// Placing a card is part of what the trigger does, not a cost of it, so having no card to place does
		// not stop the rest: as much is done as can be.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		scn.FreepsAcceptOptionalTrigger();
		assertInHand(raiders);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deckCount - 1, scn.GetFreepsDeckCount());

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeElvenOrGandalfRingsAChosenCompanionIsHealedAndTheTriggerIsThenOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var vilya = scn.GetFreepsCard("vilya");
		var nenya = scn.GetFreepsCard("nenya");
		var narya = scn.GetFreepsCard("narya");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToSupportArea(three);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToHand(narya, guard, troop);

		scn.StartGame();

		// Galadriel goes down after the start of the turn: with her and Elrond both in play then, the game
		// would stop to ask which of their start-of-turn heals comes first.
		scn.MoveAlliesToTable(galadriel);
		scn.AttachCardsTo(galadriel, nenya);

		// Two wounded companions of different cultures and races, a wounded ally, and Frodo unhurt.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(elrond, 1);
		scn.MoveCardsToTopOfDeck(raiders);

		// The third ring is played, not cheated in.  Two [elven] rings and a [gandalf] ring make three.
		scn.FreepsPlayCard(narya);
		assertAttachedTo(narya, gandalf);

		int deckCount = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The heal is offered first, as a choice of a companion that may be passed: one or none.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(elrond));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, troop));
		scn.FreepsChooseCard(guard);
		assertSame(guard, scn.GetFreepsBottomOfDeck());
		assertInHand(troop, raiders);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		// Still only the one wound healed.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoElvenOrGandalfRingsAndAThirdRingOfAnotherCultureNoOneIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var narya = scn.GetFreepsCard("narya");
		var barahir = scn.GetFreepsCard("barahir");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToSupportArea(three);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToHand(guard, troop);

		scn.StartGame();

		// Four rings in play, with The One Ring, but only two of them [elven] or [gandalf].
		scn.AttachCardsTo(gandalf, narya);
		scn.AttachCardsTo(aragorn, barahir);
		assertAttachedTo(scn.GetRing(), frodo);
		// The only wounded companion: a heal would not need to ask whom.
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToTopOfDeck(raiders);

		int deckCount = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, troop));
		scn.FreepsChooseCard(guard);
		assertSame(guard, scn.GetFreepsBottomOfDeck());
		assertInHand(troop, raiders);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeRingsTheHealMayBePassedAndTheTriggerIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var three = scn.GetFreepsCard("three");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var vilya = scn.GetFreepsCard("vilya");
		var nenya = scn.GetFreepsCard("nenya");
		var narya = scn.GetFreepsCard("narya");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetFreepsCard("troop");
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToSupportArea(three);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToHand(guard, troop);

		scn.StartGame();

		// Galadriel goes down after the start of the turn: with her and Elrond both in play then, the game
		// would stop to ask which of their start-of-turn heals comes first.
		scn.MoveAlliesToTable(galadriel);
		scn.AttachCardsTo(galadriel, nenya);
		scn.AttachCardsTo(gandalf, narya);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToTopOfDeck(raiders);

		int deckCount = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The heal is a "may" of its own: a companion is clicked, or the choice is passed.  It is not asked as
		// a yes or no.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Passing the heal does not pass the rest: the card may still be placed and another drawn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("place a card from hand beneath your draw deck"));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, troop));
		scn.FreepsChooseCard(guard);
		assertSame(guard, scn.GetFreepsBottomOfDeck());
		assertInHand(troop, raiders);
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
