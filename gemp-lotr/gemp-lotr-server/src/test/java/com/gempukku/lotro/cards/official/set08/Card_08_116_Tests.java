package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("meet", "8_116");
					// Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4; +2 only while he bears a weapon
					put("merry", "1_303");
					// Pippin, Trollslayer: an unbound [gondor] Hobbit, vitality 4; his skirmish ability is used only on purpose
					put("gondorpippin", "103_31");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit; his abilities are used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally; his skirmish ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is no Hobbit

					// Shadow cards of four types, laid in the Free Peoples player's own discard pile
					put("soldier", "1_271");    // Orc Soldier: a minion
					put("wreathed", "1_238");   // Wreathed in Shadow: a condition
					put("hunt", "1_126");       // Hunt Them Down!: an event
					put("sword", "1_160");      // Uruk-hai Sword: a possession
					put("escape", "4_300");     // Escape: a Free Peoples condition, laid in the same pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeShallMeetAgainSoonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: We Shall Meet Again Soon
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the fellowship moves during the regroup phase, exert an unbound Hobbit to place a Shadow card from your discard pile on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("meet");

		assertEquals("We Shall Meet Again Soon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveExertsAnUnboundHobbitToPlaceAShadowCardFromTheDiscardPileOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meet = scn.GetFreepsCard("meet");
		var merry = scn.GetFreepsCard("merry");
		var soldier = scn.GetFreepsCard("soldier");
		var wreathed = scn.GetFreepsCard("wreathed");
		var hunt = scn.GetFreepsCard("hunt");
		var sword = scn.GetFreepsCard("sword");
		var escape = scn.GetFreepsCard("escape");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(meet);
		scn.MoveCardsToDiscard(soldier, wreathed, hunt, sword, escape);

		scn.StartGame();
		assertInHand(meet);
		assertInDiscard(soldier, wreathed, hunt, sword, escape);

		// The move at the end of the fellowship phase does not offer it.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(meet));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertNotSame(hunt, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(meet));
		// Merry is the one unbound Hobbit, so he exerts unasked.
		scn.FreepsChooseOptionalTrigger(meet);
		assertEquals(1, scn.GetWoundsOn(merry));

		// A Shadow card of each type in the pile; not the Free Peoples card there.
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, wreathed, hunt, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(escape));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(hunt);

		assertSame(hunt, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, hunt);
		assertInDiscard(soldier, wreathed, sword, escape);
		assertInDiscard(meet);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnyUnboundHobbitMayExertAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meet = scn.GetFreepsCard("meet");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("gondorpippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var soldier = scn.GetFreepsCard("soldier");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(meet);
		scn.MoveCardsToDiscard(soldier);

		scn.StartGame();
		assertInDiscard(soldier);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.FreepsChooseOptionalTrigger(meet);

		// The unbound Hobbits of two cultures.  Not the Ring-bound Sam or Ring-bearer, the Hobbit ally, or an
		// unbound companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, bounder, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		// The one Shadow card in the pile goes unasked.
		assertSame(soldier, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoShadowCardInTheDiscardPileTheHobbitExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meet = scn.GetFreepsCard("meet");
		var merry = scn.GetFreepsCard("merry");
		var escape = scn.GetFreepsCard("escape");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(meet);
		scn.MoveCardsToDiscard(escape);

		scn.StartGame();
		assertInDiscard(escape);
		assertInHand(meet);
		scn.SkipToMovementDecision();
		var top = scn.GetFreepsTopOfDeck();
		scn.FreepsChooseToMove();

		// An event's target is no requirement: it is played, and its cost paid, for nothing.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(meet));
		scn.FreepsChooseOptionalTrigger(meet);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInDiscard(escape);
		assertSame(top, scn.GetFreepsTopOfDeck());
		assertInDiscard(meet);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileNoUnboundHobbitCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meet = scn.GetFreepsCard("meet");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var soldier = scn.GetFreepsCard("soldier");
		scn.MoveCompanionsToTable(merry, sam);
		scn.MoveCardsToHand(meet);
		scn.MoveCardsToDiscard(soldier);
		// Vitality 4 with 3 wounds: the one unbound Hobbit is exhausted.  Sam, a Ring-bound Hobbit, can exert.
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInDiscard(soldier);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(meet));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInDiscard(soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
