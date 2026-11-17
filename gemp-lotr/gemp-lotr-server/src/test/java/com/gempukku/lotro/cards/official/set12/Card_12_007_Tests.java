package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "12_7");
					put("warrior2", "12_7");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, cost 1; to play, spot a Dwarf
					put("smeagol", "5_28");     // Smeagol, Old Noser: [gollum] companion; a regroup ability used only on purpose
					put("wehates", "7_77");     // We Hates Them: on Smeagol; Regroup: discard it to discard a minion and play a companion from the discard pile
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, no text that reaches these tests once in play
					put("troop", "1_177");      // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Dwarven Warrior
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: When you play this companion during the fellowship phase, choose a Shadow player who must discard the top 2 cards of his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Dwarven Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedInTheFellowshipPhaseTheShadowPlayerDiscardsTheTopTwoCardsOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		scn.MoveCardsToHand(warrior);

		var top = scn.GetShadowCard("runner");
		var second = scn.GetShadowCard("troop");
		var third = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SetShadowDrawDeck(top, second, third);
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(top, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(warrior);

		assertInZone(Zone.FREE_CHARACTERS, warrior);
		// Two players: the one Shadow player is chosen unasked, and his top two go.
		assertInZone(Zone.DISCARD, top, second);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(third, scn.GetShadowTopOfDeck());
		// The Free Peoples player's own deck is not touched.
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherDwarfPlayedAfterItDiscardsNothingAndASecondCopyDiscardsOnlyItsOwnTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(warrior, warrior2, guard);

		var s1 = scn.GetShadowCard("runner");
		var s2 = scn.GetShadowCard("troop");
		var s3 = scn.GetShadowCard("guard");
		var s4 = scn.GetShadowCard("warrior");
		var s5 = scn.GetShadowCard("smeagol");
		var s6 = scn.GetShadowCard("wehates");

		scn.StartGame();
		scn.SetShadowDrawDeck(s1, s2, s3, s4, s5, s6);
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard(warrior);
		assertInZone(Zone.DISCARD, s1, s2);
		assertEquals(4, scn.GetShadowDeckCount());

		// Another Dwarf companion's play is not this companion's.
		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(s3, scn.GetShadowTopOfDeck());

		// The second copy answers its own play only: two more, not four (the first copy does not answer it).
		scn.FreepsPlayCard(warrior2);
		assertInZone(Zone.FREE_CHARACTERS, warrior2);
		assertInZone(Zone.DISCARD, s3, s4);
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(s5, scn.GetShadowTopOfDeck());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedInTheRegroupPhaseItDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var smeagol = scn.GetFreepsCard("smeagol");
		var wehates = scn.GetFreepsCard("wehates");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, wehates);
		scn.MoveCardsToDiscard(warrior);

		var runner = scn.GetShadowCard("runner");
		var s1 = scn.GetShadowCard("troop");
		var s2 = scn.GetShadowCard("guard");
		var s3 = scn.GetShadowCard("warrior");

		scn.StartGame();
		assertInZone(Zone.ATTACHED, wehates);
		assertInZone(Zone.DISCARD, warrior);
		scn.SkipToPhase(Phase.REGROUP);
		// A minion that lived through the skirmishes, for We Hates Them to discard.
		scn.MoveMinionsToTable(runner);
		scn.SetShadowDrawDeck(s1, s2, s3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// Played from the discard pile by We Hates Them in the regroup phase: the Runner is the one minion and the
		// Warrior the one companion there, so both are taken unasked.
		scn.FreepsUseCardAction(wehates);

		assertInZone(Zone.FREE_CHARACTERS, warrior);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(s1, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, s1, s2, s3);
		// The Runner alone is in the Shadow discard pile.
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void PlayedInTheStartingFellowshipItDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");

		// The table rests at player 1's starting fellowship, before turn 1 and its fellowship phase.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.DECK, warrior);

		scn.FreepsChooseCard(warrior);

		assertInZone(Zone.FREE_CHARACTERS, warrior);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		// The second Warrior still fits under the total of 4, so player 1 is still choosing.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
	}
}
