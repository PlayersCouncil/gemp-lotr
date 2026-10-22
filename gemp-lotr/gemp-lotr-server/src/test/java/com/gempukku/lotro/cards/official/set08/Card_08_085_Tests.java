package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charged", "8_85");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetInitiativeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charged", "8_85");
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6, vitality 3; valiant and nothing else
					// Rider's Spear: [rohan] hand weapon, cost 1, strength +2; damage +1 only on a mounted bearer
					put("spear", "4_288");
					// Heavy Chain: [rohan] armor, cost 1; its bearer is not overwhelmed unless his strength is tripled
					put("chain", "4_278");
					// Rider's Mount: [rohan] mount, cost 1; its text answers the start of a skirmish
					put("mount", "4_287");
					put("armor", "1_8");        // Dwarven Armor: a possession that is not [rohan]
					// Rohirrim Bow: a second [rohan] possession in the discard pile, so that the choice is asked
					put("bow", "5_88");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1
					// The Free Peoples player's own copies of these are cards to hold in hand: he can play none
					// of them.
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner", "1_178");     // Goblin Runner: its text is about being played
					put("marksman", "1_176");   // Goblin Marksman: archer and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBoardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charged", "8_85");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					// Rohirrim Scout: [rohan] Man companion, strength 5, vitality 3; a regroup ability used only on purpose
					put("scout", "5_90");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is its whole text
					// Rider's Spear: [rohan] hand weapon, strength +2; damage +1 only on a mounted bearer
					put("spear", "4_288");
					// Rohirrim Shield: [rohan] shield; minion archery total -1, and a regroup ability used only on purpose
					put("shield", "5_91");
					// Rohirrim Bow: [rohan] ranged weapon, strength +1; an archery ability used only on purpose
					put("bow", "5_88");
					// Rohirrim Helm: [rohan] helm; a skirmish ability used only on purpose
					put("helm", "5_89");
					// Sword of Gondor: a [gondor] possession on a [gondor] Man; damage +1 against a Man or a roaming minion
					put("ssword", "4_134");
					// Gondorian Sword: a [gondor] possession; a skirmish ability that needs it in play
					put("gsword", "7_98");
					// The Red Arrow: a [rohan] ARTIFACT in the support area, no possession; an assignment ability used
					// only on purpose
					put("arrow", "9_46");
					// Armory: a [rohan] condition, no possession; a maneuver ability that needs it in play
					put("armory", "5_79");
					// Sword of Rohan: a [rohan] hand weapon, laid only in the Shadow player's discard pile
					put("rsword", "4_291");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1
					// The Free Peoples player's own copies of these are cards to hold in hand: he can play none
					// of them.
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner", "1_178");     // Goblin Runner: its text is about being played
					put("marksman", "1_176");   // Goblin Marksman: archer and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChargedHeadlongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Charged Headlong
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If you have initiative, discard a [rohan] possession to play a [rohan] possession from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("charged");

		assertEquals("Charged Headlong", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Comprehensive Rules 4.2, "playing a card": "You cannot count The Card for initiative. ...  If you are the
	// Free Peoples player and initiative is a requirement for playing The Card, you must have four cards in your
	// hand not counting The Card" (F-0464; Ketura, 2026-10-11).
	@Test
	public void WithFourCardsInHandItselfAmongThemItCannotBePlayedAndWithFourBesideItItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInitiativeScenario();

		var charged = scn.GetFreepsCard("charged");
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		var chain = scn.GetFreepsCard("chain");
		var mount = scn.GetFreepsCard("mount");
		var armor = scn.GetFreepsCard("armor");
		var bow = scn.GetFreepsCard("bow");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, spear, chain);
		scn.MoveCardsToDiscard(mount, armor, bow);
		scn.MoveCardsToHand(charged, card1, card2, card3);
		assertInZone(Zone.HAND, charged);
		assertAttachedTo(spear, guard);
		assertAttachedTo(chain, guard);
		assertInZone(Zone.DISCARD, mount, armor, bow);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		// Four cards in hand, the event one of them.  He has initiative, and three cards beside the event: it
		// cannot be played, though there is a possession to discard and one to play.
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, charged);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(charged));

		// With four cards beside it, it can.
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(charged));

		scn.FreepsPlayCard(charged);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, chain));
		scn.FreepsChooseCard(spear);
		assertInZone(Zone.DISCARD, spear);

		assertTrue(scn.FreepsHasCardChoiceAvailable(mount, bow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor));
		scn.FreepsChooseCards(mount);

		assertAttachedTo(mount, guard);
		assertAttachedTo(chain, guard);
		assertInZone(Zone.DISCARD, charged, armor, bow);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DiscardsARohanPossessionFromPlayToPlayARohanPossessionFromTheDiscardPileOnARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var charged = scn.GetFreepsCard("charged");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var knight = scn.GetFreepsCard("knight");
		var spear = scn.GetFreepsCard("spear");
		var shield = scn.GetFreepsCard("shield");
		var bow = scn.GetFreepsCard("bow");
		var helm = scn.GetFreepsCard("helm");
		var ssword = scn.GetFreepsCard("ssword");
		var gsword = scn.GetFreepsCard("gsword");
		var arrow = scn.GetFreepsCard("arrow");
		var armory = scn.GetFreepsCard("armory");
		scn.MoveCompanionsToTable(guard, scout, knight);
		scn.AttachCardsTo(guard, spear);
		scn.AttachCardsTo(scout, shield);
		scn.AttachCardsTo(knight, ssword);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToDiscard(bow, helm, gsword, armory);
		scn.MoveCardsToHand(charged, scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"),
				scn.GetFreepsCard("marksman"), scn.GetFreepsCard("brigand"));

		var savage = scn.GetShadowCard("savage");
		// The Shadow player's own [rohan] possession, in his own discard pile.
		var rsword = scn.GetShadowCard("rsword");
		scn.MoveCardsToDiscard(rsword);

		scn.StartGame();
		assertAttachedTo(spear, guard);
		assertAttachedTo(shield, scout);
		assertAttachedTo(ssword, knight);
		assertInZone(Zone.SUPPORT, arrow);
		assertInDiscard(bow, helm, gsword, armory, rsword);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(charged));
		scn.FreepsPlayCard(charged);

		// The cost: a [rohan] possession of either bearer; not a [gondor] possession, nor a [rohan] artifact.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, shield));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ssword, arrow));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(shield);
		assertInDiscard(shield);
		assertAttachedTo(spear, guard);

		// From the discard pile: a [rohan] possession of either class; not a [gondor] possession, nor a [rohan]
		// condition, nor the Shadow player's [rohan] possession in his own pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, helm));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gsword, armory, rsword));
		// The play is no "may" (CR 4.2 "effect": a card an action plays as its effect must be played).
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(bow);

		// Played on a [rohan] Man of the player's choice, not on the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(scout);

		assertAttachedTo(bow, scout);
		assertInDiscard(charged, helm, gsword, armory, shield, rsword);
		assertAttachedTo(spear, guard);
		assertAttachedTo(ssword, knight);
		assertInZone(Zone.SUPPORT, arrow);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	// Comprehensive Rules 4.2, "effect": "If an action plays a card from your hand (or discard pile) as part of its
	// effect, then that card must be in your hand (or discard pile) before you can begin to perform that action"
	// (the They Are Coming ruling; Ketura, Q-0192, Q-0320 and Q-0331, for a cost of any kind).
	@Test
	public void ThePossessionDiscardedAsTheCostIsNotThePossessionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var charged = scn.GetFreepsCard("charged");
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		var bow = scn.GetFreepsCard("bow");
		var helm = scn.GetFreepsCard("helm");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, spear);
		scn.MoveCardsToDiscard(bow, helm);
		scn.MoveCardsToHand(charged, scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"),
				scn.GetFreepsCard("marksman"), scn.GetFreepsCard("brigand"));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(spear, guard);
		assertInDiscard(bow, helm);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		assertTrue(scn.FreepsPlayAvailable(charged));
		scn.FreepsPlayCard(charged);
		// The Spear is the one [rohan] possession in play, and goes with nothing asked.
		assertInDiscard(spear);

		// The Bow and the Helm, there before the event was played, are on offer; the Spear, discarded to pay for
		// it, is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, helm));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spear));
		scn.FreepsChooseCards(bow);

		assertAttachedTo(bow, guard);
		assertInDiscard(spear, helm, charged);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoRohanPossessionInPlayToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var charged = scn.GetFreepsCard("charged");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var spear = scn.GetFreepsCard("spear");
		var bow = scn.GetFreepsCard("bow");
		var ssword = scn.GetFreepsCard("ssword");
		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCompanionsToTable(guard, knight);
		scn.AttachCardsTo(knight, ssword);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToDiscard(bow);
		scn.MoveCardsToHand(charged, scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"),
				scn.GetFreepsCard("marksman"), scn.GetFreepsCard("brigand"));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(ssword, knight);
		assertInZone(Zone.SUPPORT, arrow);
		assertInDiscard(bow);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		// A [gondor] possession and a [rohan] artifact in play, a [rohan] possession to play: not playable.
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(charged));

		// A [rohan] possession on the Guard, and it is.
		scn.AttachCardsTo(guard, spear);
		scn.FreepsRefreshDecision();
		assertAttachedTo(spear, guard);
		assertTrue(scn.FreepsPlayAvailable(charged));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// Playing a card from the discard pile as an effect is a cost (Ketura, F-0046, Q-0062).
	@Test
	public void CannotBePlayedWithNoRohanPossessionInTheDiscardPileToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var charged = scn.GetFreepsCard("charged");
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		var bow = scn.GetFreepsCard("bow");
		var gsword = scn.GetFreepsCard("gsword");
		var armory = scn.GetFreepsCard("armory");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, spear);
		scn.MoveCardsToDiscard(gsword, armory);
		scn.MoveCardsToHand(charged, scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"),
				scn.GetFreepsCard("marksman"), scn.GetFreepsCard("brigand"));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(spear, guard);
		assertInDiscard(gsword, armory);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		// A [rohan] possession to discard, and in the discard pile only a [gondor] possession and a [rohan]
		// condition: not playable.
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(charged));

		// A [rohan] possession in the discard pile, and it is.
		scn.MoveCardsToDiscard(bow);
		assertInDiscard(bow);
		assertTrue(scn.FreepsPlayAvailable(charged));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
