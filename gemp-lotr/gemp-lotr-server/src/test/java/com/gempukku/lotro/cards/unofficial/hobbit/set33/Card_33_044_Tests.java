package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_044_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Its mountains are Erebor 30_55 (site 7, also a river) and Smaug's Den 30_56 (site 8, also underground);
	// Esgaroth 30_54 (site 6) is a river and a sanctuary and no mountain.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("giant", "33_44");
					// A second copy: its own play from the discard pile, its own exertion.
					put("giant2", "33_44");
					// Shadow cards of three kinds, and a Free Peoples card, for the hands.
					put("footman", "30_42");     // Goblin Footman: [moria] minion
					put("knife", "31_60");       // Troll Knife: [troll] possession
					put("sack", "31_57");        // Caught In A Sack: [troll] condition
					put("balin", "30_2");        // Balin, Brother of Dwalin: [dwarven] companion
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void LavastoneGiantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Lavastone Giant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Giant
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> At a mountain, remove (2) to play this minion from your discard pile.<br><b>Archery:</b> Exert this minion to reveal a random card from your opponent's hand. If it is a Shadow card, the minion archery total is +2. Otherwise, exert this minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("giant");

		assertEquals("Lavastone Giant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.GIANT, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtEreborRemoving2PlaysThisCopyFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var giant2 = scn.GetShadowCard("giant2");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToDiscard(giant, giant2);
		assertInZone(Zone.DISCARD, giant, giant2);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, giant, giant2);
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();

		// 2 removed + 7 to play: the giant's site is 4, so it is not roaming at site 7.
		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);
		assertInZone(Zone.SHADOW_CHARACTERS, giant);
		assertInZone(Zone.DISCARD, giant2);
		// 9 - 2 removed - 7 to play
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtEreborItIsNotOfferedWithOneTooFewForTheRemovalAndThePlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToDiscard(giant);
		assertInZone(Zone.DISCARD, giant);
		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, giant);
		// 2 removed + 7 to play - 1
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(giant));
	}

	@Test
	public void AtSmaugsDenItIsPlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(giant);
		assertInZone(Zone.DISCARD, giant);
		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.DISCARD, giant);
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);
		assertInZone(Zone.SHADOW_CHARACTERS, giant);
		// 9 - 2 removed - 7 to play
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtEsgarothARiverThatIsNoMountainItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToDiscard(giant);
		assertInZone(Zone.DISCARD, giant);
		scn.FreepsPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, giant);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(giant));
		assertInZone(Zone.DISCARD, giant);
	}

	@Test
	public void ArcheryRevealsAShadowCardOfEachKindFromTheFreePeoplesHandToAdd2EachTimeUntilExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var giant2 = scn.GetShadowCard("giant2");
		// The Free Peoples player's own copies of Shadow cards, in his hand.
		var footman = scn.GetFreepsCard("footman");
		var knife = scn.GetFreepsCard("knife");
		var sack = scn.GetFreepsCard("sack");

		scn.StartGame();
		scn.MoveMinionsToTable(giant, giant2);
		assertEquals(0, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));

		scn.SkipToPhase(Phase.ARCHERY);
		// One card in his hand: the card revealed at random is that one.
		scn.SetFreepsHand(footman);
		assertInHand(footman);
		assertEquals(1, scn.GetFreepsHandCount());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// No minion here is an archer.
		assertEquals(0, scn.GetShadowArcheryTotal());

		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);
		// The exertion is this copy's own.
		assertEquals(1, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));
		// The card is shown to the Shadow player, listed and not to be chosen; its owner is shown nothing.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		// A Shadow minion: +2, and no second exertion.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetWoundsOn(giant));
		assertInHand(footman);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// A Shadow possession.
		scn.SetFreepsHand(knife);
		assertInHand(knife);
		scn.FreepsPass();
		scn.ShadowUseCardAction(giant);
		scn.ShadowDismissRevealedCards();
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(2, scn.GetWoundsOn(giant));

		// A Shadow condition.
		scn.SetFreepsHand(sack);
		assertInHand(sack);
		scn.FreepsPass();
		scn.ShadowUseCardAction(giant);
		scn.ShadowDismissRevealedCards();
		assertEquals(6, scn.GetShadowArcheryTotal());
		// Vitality 4 with 3 wounds: exhausted, so it can no longer pay; the other copy can.
		assertEquals(3, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(giant));
		assertTrue(scn.ShadowActionAvailable(giant2));
	}

	@Test
	public void AFreePeoplesCardRevealedAddsNothingAndThisCopyExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var giant2 = scn.GetShadowCard("giant2");
		var balin = scn.GetFreepsCard("balin");
		// The Shadow player's own hand holds Shadow cards: it is not the hand revealed.
		var footman = scn.GetShadowCard("footman");
		var knife = scn.GetShadowCard("knife");

		scn.StartGame();
		scn.MoveMinionsToTable(giant, giant2);
		assertEquals(0, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetFreepsHand(balin);
		scn.SetShadowHand(footman, knife);
		assertInHand(balin, footman, knife);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		scn.FreepsPass();
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowUseCardAction(giant);
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(balin, false));
		scn.ShadowDismissRevealedCards();
		// A Free Peoples companion: nothing added, and this copy exerts a second time.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(2, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetWoundsOn(giant2));
		assertInHand(balin, footman, knife);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ExhaustedByItsCostItDoesNotExertAgainNorTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var balin = scn.GetFreepsCard("balin");

		scn.StartGame();
		scn.MoveMinionsToTable(giant);
		scn.AddWoundsToChar(giant, 2);
		assertEquals(2, scn.GetWoundsOn(giant));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetFreepsHand(balin);
		assertInHand(balin);
		scn.FreepsPass();

		scn.ShadowUseCardAction(giant);
		scn.ShadowDismissRevealedCards();
		// The cost leaves it exhausted (3 wounds of vitality 4): it cannot exert again, and a wound in place of the
		// exertion would kill it.
		assertEquals(3, scn.GetWoundsOn(giant));
		assertInZone(Zone.SHADOW_CHARACTERS, giant);
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesHandEmptyTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");

		scn.StartGame();
		scn.MoveMinionsToTable(giant);
		assertEquals(0, scn.GetWoundsOn(giant));

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetFreepsHand();
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(giant));
		scn.ShadowUseCardAction(giant);
		// Nothing to reveal: the exertion paid, nothing added, and no second exertion.
		assertEquals(1, scn.GetWoundsOn(giant));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheAdded2IsFiredAndGoneOnceTheArcheryPhaseIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var giant = scn.GetShadowCard("giant");
		var footman = scn.GetFreepsCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(giant);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetFreepsHand(footman);
		assertInHand(footman);
		scn.FreepsPass();
		scn.ShadowUseCardAction(giant);
		scn.ShadowDismissRevealedCards();
		assertEquals(2, scn.GetShadowArcheryTotal());

		// Fired at Bilbo, the only companion.
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		scn.PassArcheryActions();
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(2, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
