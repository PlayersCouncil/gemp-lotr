package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("where", "7_78");
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
					put("where", "7_78");
					// Sméagol, Always Helps: strength 3, vitality 4.  His ability is an assignment one, not
					// used here.
					put("smeagol", "7_71");
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
	public void WhereShallWeGoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Where Shall We Go
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If you have initiative, discard 2 cards from hand to wound a minion Sméagol is skirmishing twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("where");

		assertEquals("Where Shall We Go", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Comprehensive Rules 4.2, "playing a card": "You cannot count The Card for initiative. ...  If you are the
	// Free Peoples player and initiative is a requirement for playing The Card, you must have four cards in your
	// hand not counting The Card" (F-0464; Ketura, 2026-10-11).
	@Test
	public void WithFourCardsInHandItselfAmongThemItCannotBePlayedAndWithFourBesideItItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInitiativeScenario();

		var where = scn.GetFreepsCard("where");
		var smeagol = scn.GetFreepsCard("smeagol");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(where, card1, card2, card3);
		assertInHand(where, card1, card2, card3);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage));

		// Four cards in hand, the event one of them.  He has initiative, and three cards beside the event: it
		// cannot be played.
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, where);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(where));

		// With four cards beside it, it can.
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(where));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsPlayCard(where);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(card1, card2, card3, card4));
		scn.FreepsChooseCards(card1, card2);

		assertInZone(Zone.DISCARD, card1, card2, where);
		assertEquals(2, scn.GetFreepsHandCount());
		// Vitality 3: the Savage lives through both wounds.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("where", "7_78");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Sméagol, strength 3, vitality 4; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Sméagol; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so that one wound kills it; its text is about being played (cheated in)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					// Cards for the hand, one of each type: none of them is played
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("pride", "7_13");       // Reckless Pride: a condition
					put("where2", "7_78");      // a second copy of this event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DiscardsTwoCardsToWoundAMinionSmeagolIsSkirmishingTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var where = scn.GetFreepsCard("where");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var pride = scn.GetFreepsCard("pride");
		var where2 = scn.GetFreepsCard("where2");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(where, legolas, armor, pride, where2);
		assertInHand(where, legolas, armor, pride, where2);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, savage}, new PhysicalCardImpl[]{aragorn, troop});
		// The Runner, left over, goes to Sméagol as well.
		scn.ShadowAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, runner));
		// The Troop is assigned to Aragorn, whose skirmish has not begun.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(where));
		scn.FreepsPlayCard(where);

		// The cost: any 2 cards from hand, whatever their type.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, armor, pride, where2));
		scn.FreepsChooseCards(legolas, pride);
		assertInDiscard(legolas, pride);
		assertInHand(armor, where2);

		// Either minion Sméagol is skirmishing, the Runner at one vitality too (a wound, not an exertion); not the
		// Troop, assigned to another companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// Vitality 3: the Savage lives through both wounds.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(where);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedInAnotherCompanionsSkirmishItWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var where = scn.GetFreepsCard("where");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var pride = scn.GetFreepsCard("pride");
		var where2 = scn.GetFreepsCard("where2");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(where, legolas, armor, pride, where2);
		assertInHand(where, legolas, armor, pride, where2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, savage}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(aragorn);

		// The Troop is skirmishing Aragorn, who is no Sméagol; the Savage is assigned to Sméagol, whose skirmish
		// has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssignedAgainst(smeagol, savage));
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The event's target is no requirement: it is played, and paid for, for nothing.
		assertTrue(scn.FreepsPlayAvailable(where));
		scn.FreepsPlayCard(where);
		scn.FreepsChooseCards(legolas, pride);

		assertInDiscard(where, legolas, pride);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionTheShireSmeagolIsSkirmishingIsWoundedTwiceToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var where = scn.GetFreepsCard("where");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var pride = scn.GetFreepsCard("pride");
		var where2 = scn.GetFreepsCard("where2");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(where, legolas, armor, pride, where2);
		assertInHand(where, legolas, armor, pride, where2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(smeagol, savage);

		// This Smeagol is [shire]: the text names only the title.
		assertEquals(Culture.SHIRE, smeagol.getBlueprint().getCulture());
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(where));
		scn.FreepsPlayCard(where);
		scn.FreepsChooseCards(legolas, pride);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertInDiscard(where, legolas, pride);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
