package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("feet", "1_36");
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, vitality 3
					put("lorien", "1_53");        // Lorien Elf: Elf companion, cost 1, vitality 2; to play, spot an Elf
					put("orophin", "1_56");       // Orophin, Lorien Bowman: Elf ally, vitality 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, vitality 4

					put("runner1", "1_178");      // Goblin Runner: [moria] Orc
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc
					put("creeper", "8_75");       // Morgul Creeper: [wraith] Orc
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, a minion who is no Orc
					put("scimitar", "1_180");     // Goblin Scimitar: a [moria] possession
					put("fear", "1_278");         // Strength Born of Fear: a [sauron] condition
					put("chaff1", "1_3");         // Axe Strike: a Free Peoples event
					put("chaff2", "1_3");
					put("chaff3", "1_3");
					put("chaff4", "1_3");
					// No Business of Ours: "The Free Peoples player may not look at or reveal cards in any Shadow
					// player's hand."
					put("business", "2_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CurseTheirFoulFeetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Curse Their Foul Feet!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Fellowship: Exert an Elf to reveal an opponent's hand. That player discards a card from hand for each Orc revealed.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("feet");

		assertEquals("Curse Their Foul Feet!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FoulFeetExertsAnElfToRevealShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Aragorn could exert, and is no Elf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(feet, arwen);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		var chaff4 = scn.GetShadowCard("chaff4");
		scn.MoveCardsToShadowHand("runner1","runner2","runner3","runner4",
				"chaff1","chaff2","chaff3","chaff4" );

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(feet));
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(feet));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetTwilight());

		// Arwen is the only Elf: she exerts without a choice.
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The event costs 0.
		assertEquals(2, scn.GetTwilight());
		assertEquals(8, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();

		// 4 Orcs revealed: the owner of the hand discards 4 cards, and which is for him to say.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2, runner3, runner4, chaff1, chaff2, chaff3, chaff4));
		assertEquals(8, scn.ShadowGetCardChoiceCount());
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowChooseCards(chaff1, chaff2, chaff3, chaff4);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, chaff1, chaff2, chaff3, chaff4);
		assertInZone(Zone.HAND, runner1, runner2, runner3, runner4);
		assertInZone(Zone.DISCARD, feet);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfOfThePlayersChoiceExertsAndTheHandIsShownToThePlayerOfTheEventAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		// The Free Peoples player holds an Orc of his own, which is not in "an opponent's hand".
		var ownRunner = scn.GetFreepsCard("runner1");
		// The Shadow player's hand: one Orc and four cards that are none.
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		var fear = scn.GetShadowCard("fear");
		var chaff = scn.GetShadowCard("chaff1");
		// Stays in his draw deck, with the other Orcs of his deck.
		var chieftain = scn.GetShadowCard("chieftain");
		// An Elf companion and an Elf ally, and Aragorn, a companion who could exert and is no Elf.
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(feet, ownRunner);
		scn.MoveCardsToHand(runner, savage, scimitar, fear, chaff);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(feet));

		scn.MarkGameLog();
		scn.FreepsPlayCard(feet);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(orophin);
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());

		// The whole hand is shown to the player of the event; its owner is told by the game log.
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(chaff, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(chieftain, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner, savage, scimitar, fear, chaff));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(chieftain));
		assertEquals(5, scn.GetShadowHandCount());

		// 1 Orc, the Goblin Runner: one card, any card of that hand, and the Shadow player picks it.
		scn.FreepsDismissRevealedCards();
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, savage, scimitar, fear, chaff));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ownRunner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(scimitar);
		assertEquals(4, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, scimitar);
		assertInZone(Zone.HAND, runner, savage, fear, chaff);
		assertInZone(Zone.HAND, ownRunner);
		assertInZone(Zone.DISCARD, feet);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OrcsOfEveryCultureAreCountedAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		// Three Orcs of three cultures; a minion who is no Orc; a [moria] card and a [sauron] card that are no Orcs.
		var runner = scn.GetShadowCard("runner1");
		var chieftain = scn.GetShadowCard("chieftain");
		var creeper = scn.GetShadowCard("creeper");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(runner, chieftain, creeper, savage, scimitar, fear);

		scn.StartGame();
		scn.FreepsPlayCard(feet);
		assertEquals(6, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, chieftain, creeper, savage, scimitar, fear));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(6, scn.GetShadowHandCount());

		// He may keep his Orcs.
		scn.ShadowChooseCards(savage, scimitar, fear);
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, savage, scimitar, fear);
		assertInZone(Zone.HAND, runner, chieftain, creeper);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoOrcRevealedNobodyDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(savage, scimitar, fear);

		scn.StartGame();
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, feet);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(arwen, aragorn);
		// Arwen, vitality 3, is exhausted; a healthy Man stands beside her.
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveCardsToHand(feet, lorien);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(feet));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(lorien);
		assertTrue(scn.FreepsPlayAvailable(feet));
		assertEquals(1, scn.GetTwilight());

		// The Lorien Elf is the only one who can exert.
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());

		// The Goblin Runner is his whole hand: it goes without a choice.
		scn.FreepsDismissRevealedCards();
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AgainstAnEmptyHandItMayStillBePlayedAndOnlyItsCostIsPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(feet));

		scn.MarkGameLog();
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, feet);
		assertEquals(0, scn.GetShadowDiscardCount());
		// Nobody is shown anything or asked anything, and the game log says that there was nothing to reveal.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
	}

	@Test
	public void WhileTheHandMayNotBeRevealedNoOrcIsRevealedAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var business = scn.GetShadowCard("business");
		var runner = scn.GetShadowCard("runner1");
		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToSupportArea(business);
		scn.MoveCardsToHand(runner, chieftain, savage);

		scn.StartGame();

		// The event may be played whatever it will do; the reveal is its effect, and that is what is barred.
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(feet));

		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.DISCARD, feet);
		// Two Orcs are in that hand, and neither was revealed.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, runner, chieftain, savage);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
