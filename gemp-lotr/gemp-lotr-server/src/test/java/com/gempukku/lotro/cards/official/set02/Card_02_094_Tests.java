package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("verily", "2_94");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("band", "1_270");       // Orc Scouting Band: [sauron] Orc tracker; a roaming discount and a skirmish ability
					put("sentinel", "10_83");   // Cirith Ungol Sentinel: [sauron] Uruk-hai, no Orc; a cost discount only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its trigger is on being played
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTriggerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("verily", "2_94");
					// Elrond, Lord of Rivendell: an ally, vitality 4.  "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					// Barliman Butterbur: an ally.  "Fellowship: Exert Barliman Butterbur to take a [gandalf] event
					// into hand from your discard pile."
					put("barliman", "1_70");
					put("wizard", "1_78");      // Mysterious Wizard: a [gandalf] skirmish event, for the discard pile
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf played by Rivendell Valley's own text
					// Seeing Stone of Orthanc: [gondor] support-area artifact (to play, spot a [gondor] Man: cheated in).
					// "Regroup: Add a threat or discard this artifact to remove (2) or to draw a card."
					put("stone", "9_38");
					// Drawing His Eye: [gondor] Fellowship event.  "For each [gondor] companion you spot, reveal 1 card
					// at random from a Shadow player's hand. Choose a revealed Shadow card to be discarded, then its owner
					// draws a card."
					put("eye", "10_29");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] companion it spots
					put("runner", "1_178");     // Goblin Runner: the Shadow card in the Shadow player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("verily", "2_94");
					// Elrond, Lord of Rivendell: an ally.  "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					put("sword", "1_299");      // Hobbit Sword: the card on top of the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VerilyIComeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Verily I Come
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, exert a [sauron] Orc. Plays on the Ring-bearer.<br>Each time the Free Peoples player draws a card (or takes a card into hand) during the fellowship phase, add a burden.
		*/

		var scn = GetScenario();

		var verily = scn.GetFreepsCard("verily");

		assertEquals("Verily I Come", verily.getBlueprint().getTitle());
		assertNull(verily.getBlueprint().getSubtitle());
		assertFalse(verily.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, verily.getBlueprint().getSide());
		assertEquals(Culture.SAURON, verily.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, verily.getBlueprint().getCardType());
		assertEquals(0, verily.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsASauronOrcOfThePlayersChoiceAndItGoesOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var verily = scn.GetShadowCard("verily");
		var soldier = scn.GetShadowCard("soldier");
		var band = scn.GetShadowCard("band");
		var sentinel = scn.GetShadowCard("sentinel");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(verily);
		assertInHand(verily);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, band, sentinel, runner);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(verily));
		scn.ShadowPlayCard(verily);

		// Either [sauron] Orc; not the [sauron] Uruk-hai, nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, band));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentinel, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(runner));
		// The Ring-bearer is the one bearer it can have, and Sam, Ring-bound as he is, is not chosen.
		assertAttachedTo(verily, frodo);
		assertNotAttachedTo(verily, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var verily = scn.GetShadowCard("verily");
		var soldier = scn.GetShadowCard("soldier");
		var sentinel = scn.GetShadowCard("sentinel");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(verily);
		assertInHand(verily);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentinel, runner);
		// Vitality 2 with 1 wound: the Orc Soldier is exhausted; the others could exert but are no [sauron] Orcs.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(verily));
		assertInHand(verily);
	}

	@Test
	public void AddsABurdenWhenTheFreePeoplesPlayerDrawsACardInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var verily = scn.GetShadowCard("verily");
		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AttachCardsTo(frodo, verily);
		scn.MoveCardsToTopOfDeck(sword);
		assertInZone(Zone.DECK, sword);
		assertEquals(sword, scn.GetFreepsTopOfDeck());
		scn.FreepsRefreshDecision();

		assertAttachedTo(verily, frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInZone(Zone.HAND, sword);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCardDrawnAddsAnotherBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var frodo = scn.GetRingBearer();
		var verily = scn.GetShadowCard("verily");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AttachCardsTo(frodo, verily);
		scn.FreepsRefreshDecision();
		assertAttachedTo(verily, frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardTakenIntoHandFromTheDiscardPileAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var frodo = scn.GetRingBearer();
		var verily = scn.GetShadowCard("verily");
		var barliman = scn.GetFreepsCard("barliman");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(wizard);
		assertInDiscard(wizard);

		scn.StartGame();
		scn.AttachCardsTo(frodo, verily);
		scn.FreepsRefreshDecision();
		assertAttachedTo(verily, frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(barliman);
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertInHand(wizard);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardDrawnByTheSitesOwnTextAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var frodo = scn.GetRingBearer();
		var verily = scn.GetShadowCard("verily");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.SkipToSite(3);
		var valley = scn.GetCurrentSite();
		scn.SetFreepsHand(legolas);
		scn.AttachCardsTo(frodo, verily);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();
		assertAttachedTo(verily, frodo);
		assertInHand(legolas);
		assertEquals(1, scn.GetFreepsHandCount());
		int burdens = scn.GetBurdens();
		int deck = scn.GetFreepsDeckCount();

		// Rivendell Valley: "Fellowship: Play an Elf to draw a card."  A site is no card of the Free Peoples side.
		assertTrue(scn.FreepsActionAvailable(valley));
		scn.FreepsUseCardAction(valley);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		// Legolas left the hand and the top card of the deck came into it.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(burdens + 1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardDrawnInTheRegroupPhaseAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var frodo = scn.GetRingBearer();
		var verily = scn.GetShadowCard("verily");
		var stone = scn.GetFreepsCard("stone");
		scn.MoveCardsToSupportArea(stone);
		assertInZone(Zone.SUPPORT, stone);

		scn.StartGame();
		scn.AttachCardsTo(frodo, verily);
		assertAttachedTo(verily, frodo);
		assertEquals(0, scn.GetBurdens());

		// No minion: the Shadow phase is followed by the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsActionAvailable(stone));

		scn.FreepsUseCardAction(stone);
		scn.FreepsChoose("Discard");
		scn.FreepsChoose("Draw");

		assertInDiscard(stone);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerDrawingInTheFellowshipPhaseAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var frodo = scn.GetRingBearer();
		var verily = scn.GetShadowCard("verily");
		var eye = scn.GetFreepsCard("eye");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetFreepsHand(eye);
		scn.SetShadowHand(runner);
		scn.AttachCardsTo(frodo, verily);
		scn.FreepsRefreshDecision();
		assertAttachedTo(verily, frodo);
		assertInHand(eye, runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetBurdens());
		int shadowDeck = scn.GetShadowDeckCount();

		// Drawing His Eye is the Free Peoples player's card, and the Shadow player has no choice in his draw.
		// Q-0334 asks whether that makes it the Free Peoples player's draw; as built it is not.
		assertTrue(scn.FreepsPlayAvailable(eye));
		scn.FreepsPlayCard(eye);
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		// Aragorn spotted: one card revealed, the Goblin Runner, the only card there.
		scn.FreepsDismissRevealedCards();

		// The Runner is discarded and the Shadow player draws in its place: a draw, in the fellowship phase, by
		// a player who is not the Free Peoples player.
		assertInDiscard(runner, eye);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
