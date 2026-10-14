package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glamdring", "1_75");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: cost 4, vitality 4; a strength bonus only
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is not Gandalf, vitality 4

					// Orcs of four cultures
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc
					put("creeper", "8_75");       // Morgul Creeper: [wraith] Orc
					put("scurrying", "11_137");   // Scurrying Goblin: [orc] Orc
					put("runner", "1_178");       // Goblin Runner: [moria] Orc
					put("hound", "7_192");        // Morgul Hound: [wraith] Orc

					// Cards that are no Orcs
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, a minion who is no Orc
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc
					put("scimitar", "1_180");     // Goblin Scimitar: a [moria] possession
					put("fear", "1_278");         // Strength Born of Fear: a [sauron] condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlamdringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Glamdring
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Gandalf.
		 * He is damage +1.
		 * Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand. Remove (1) for each Orc revealed.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glamdring");

		assertEquals("Glamdring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnGandalfAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(gandalf, glamdring);

		scn.StartGame();

		// Frodo and a [gandalf] companion of another title: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(glamdring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// Gandalf is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsGandalfToRevealTheShadowHandAndRemovesOneForEachOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(gandalf, glamdring);
		// An Orc in the Free Peoples player's own hand, which is not an opponent's.
		var ownHound = scn.GetFreepsCard("hound");
		scn.MoveCardsToHand(ownHound);

		// Four Orcs of four cultures, and four cards that are no Orcs.
		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");
		var creeper = scn.GetShadowCard("creeper");
		var scurrying = scn.GetShadowCard("scurrying");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		var scimitar = scn.GetShadowCard("scimitar");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(troop, chieftain, creeper, scurrying, savage, halftroll, scimitar, fear);
		// Stays in the Shadow draw deck.
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SetTwilight(6);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsActionAvailable(glamdring));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(glamdring);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// The whole hand is shown to the Free Peoples player; its owner is shown nothing.
		assertEquals(8, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(creeper, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scurrying, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(halftroll, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(ownHound, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(troop, chieftain, creeper, scurrying, savage, halftroll, scimitar, fear));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner));

		scn.FreepsDismissRevealedCards();
		// 4 Orcs revealed: 6 - 4.
		assertEquals(2, scn.GetTwilight());
		assertEquals(8, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionRemovesOneForTheOneOrcRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(runner, savage);

		scn.StartGame();
		// With no minion played the turn goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsActionAvailable(glamdring));

		scn.FreepsUseCardAction(glamdring);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));

		scn.FreepsDismissRevealedCards();
		// 1 Orc revealed: 3 - 1.
		assertEquals(2, scn.GetTwilight());
		// The regroup phase goes on, the Shadow player next to act.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoOrcRevealedNothingIsRemovedAndGandalfStillExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(savage, scimitar);

		scn.StartGame();
		scn.SetTwilight(2);

		scn.FreepsUseCardAction(glamdring);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(gandalf, glamdring);
		// Vitality 4 with 3 wounds; the Ent Avenger beside him could exert.
		scn.AddWoundsToChar(gandalf, 3);

		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(glamdring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
