package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_035_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Everything happens on turn 1, at The Troll Hoard 30_50 (site 2); the minions are cheated in.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rock", "33_35");
					// A second copy, on another bearer.
					put("rock2", "33_35");
					// Trolls of two cultures, a Giant, and a [moria] minion who is neither.
					put("troll", "32_31");       // Demolition Troll: [moria] Troll; a Skirmish ability, not used
					put("bert", "31_56");        // Bert, Troll of Ettenmoors: [troll] Troll, cheated in; his other texts not used
					put("giant", "33_42");       // Granite Giant: [moria] Giant; Shadow and Archery abilities, not used
					put("footman", "30_42");     // Goblin Footman: [moria] Orc; a "When you play" trigger, cheated in
					// For the hand revealed: Shadow cards of three kinds, and a Free Peoples card.
					put("footman2", "30_42");    // Goblin Footman: a Shadow minion
					put("knife", "31_60");       // Troll Knife: a Shadow possession
					put("sack", "31_57");        // Caught In A Sack: a Shadow condition
					put("balin", "30_2");        // Balin, Brother of Dwalin: a Free Peoples companion
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BoulderRockStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Boulder Rock
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Vitality: +1
		 * Game Text: Bearer must be a Troll or a Giant.<br><b>Archery:</b> Discard this possession to reveal up to 3 cards at random from your hand. For each Shadow card revealed, the minion archery total is +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rock");

		assertEquals("Boulder Rock", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnATrollOfEitherCultureOrAGiantAndNotOnAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var rock2 = scn.GetShadowCard("rock2");
		var troll = scn.GetShadowCard("troll");
		var bert = scn.GetShadowCard("bert");
		var giant = scn.GetShadowCard("giant");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(rock, rock2);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, bert, giant, footman);
		assertInZone(Zone.SHADOW_CHARACTERS, troll, bert, giant, footman);
		assertInHand(rock, rock2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(rock));
		scn.ShadowPlayCard(rock);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, bert, giant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(giant);
		assertAttachedTo(rock, giant);

		// The other alternative: a Troll.
		scn.ShadowPlayCard(rock2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, bert, giant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footman));
		scn.ShadowChooseCard(troll);
		assertAttachedTo(rock2, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArcheryDiscardsThisCopyToRevealUpTo3CardsAndAddsOneForEachShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var rock2 = scn.GetShadowCard("rock2");
		var troll = scn.GetShadowCard("troll");
		var bert = scn.GetShadowCard("bert");
		var footman = scn.GetShadowCard("footman");
		var footman2 = scn.GetShadowCard("footman2");
		var knife = scn.GetShadowCard("knife");
		var sack = scn.GetShadowCard("sack");
		// The Shadow player's own copy of a Free Peoples card, in his hand.
		var balin = scn.GetShadowCard("balin");

		scn.StartGame();
		scn.MoveMinionsToTable(troll, bert);
		scn.AttachCardsTo(troll, rock);
		scn.AttachCardsTo(bert, rock2);
		assertAttachedTo(rock, troll);
		assertAttachedTo(rock2, bert);

		scn.SkipToPhase(Phase.ARCHERY);
		// Three cards in his hand, all of them revealed: a Shadow minion, a Shadow possession and a Free Peoples card.
		scn.SetShadowHand(footman2, knife, balin);
		assertInHand(footman2, knife, balin);
		assertEquals(3, scn.GetShadowHandCount());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// No minion here is an archer.
		assertEquals(0, scn.GetShadowArcheryTotal());

		assertTrue(scn.ShadowActionAvailable(rock));
		scn.ShadowUseCardAction(rock);
		assertInZone(Zone.DISCARD, rock);
		assertAttachedTo(rock2, bert);
		// "Up to 3": a number from 0 to 3.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");
		// The cards are shown to the Free Peoples player, listed and not to be chosen; their owner is shown nothing.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(footman2, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knife, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		// Two Shadow cards revealed.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertInHand(footman2, knife, balin);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Four Shadow cards in hand, a condition among them: no more than 3 are revealed, so +3 and not +4.
		scn.SetShadowHand(footman, footman2, knife, sack);
		assertEquals(4, scn.GetShadowHandCount());
		scn.FreepsPass();
		scn.ShadowUseCardAction(rock2);
		assertInZone(Zone.DISCARD, rock2);
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		// 2 + 3
		assertEquals(5, scn.GetShadowArcheryTotal());
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void FewerThan3MayBeChosenAndTheBonusIsGoneOnceTheArcheryPhaseIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var troll = scn.GetShadowCard("troll");
		var footman2 = scn.GetShadowCard("footman2");
		var knife = scn.GetShadowCard("knife");
		var sack = scn.GetShadowCard("sack");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, rock);
		assertAttachedTo(rock, troll);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetShadowHand(footman2, knife, sack);
		assertEquals(3, scn.GetShadowHandCount());
		scn.FreepsPass();
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowUseCardAction(rock);
		// Two of his three Shadow cards are revealed: +2, not +3.
		scn.ShadowChoose("2");
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.GetShadowArcheryTotal());

		// Fired at Bilbo, the only companion; gone in the assignment phase.
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		scn.PassArcheryActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(2, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhaseAndAddsNothingWithNoShadowCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rock = scn.GetShadowCard("rock");
		var rock2 = scn.GetShadowCard("rock2");
		var troll = scn.GetShadowCard("troll");
		var bert = scn.GetShadowCard("bert");
		var balin = scn.GetShadowCard("balin");

		scn.StartGame();
		scn.MoveMinionsToTable(troll, bert);
		scn.AttachCardsTo(troll, rock);
		scn.AttachCardsTo(bert, rock2);
		assertAttachedTo(rock, troll);
		assertAttachedTo(rock2, bert);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rock));

		scn.ShadowPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		// A Free Peoples card alone in his hand: revealed, and nothing added.
		scn.SetShadowHand(balin);
		assertInHand(balin);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(rock));
		scn.ShadowUseCardAction(rock);
		scn.ShadowChoose("3");
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DISCARD, rock);
		assertEquals(0, scn.GetShadowArcheryTotal());

		// His hand empty: the second copy is offered, discarded, and reveals and adds nothing.
		scn.SetShadowHand();
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(rock2));
		scn.ShadowUseCardAction(rock2);
		assertInZone(Zone.DISCARD, rock2);
		scn.ShadowChoose("3");
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
