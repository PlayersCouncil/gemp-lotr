package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erkenbrand", "102_47");

					// Non-unique [rohan] companions, each of another name
					put("guard", "5_83");       // Household Guard: valiant, and Valiant is its whole text
					put("elite", "4_265");      // Elite Rider: strength +2 while skirmishing a wounded minion
					put("rider", "4_286");      // Rider of Rohan: a skirmish ability while mounted
					put("rohirrim", "7_246");   // Rohirrim Guard: a skirmish ability
					put("veteran", "7_257");    // Veteran Horseman: a skirmish ability; cost 2
					put("warrior", "12_113");   // Eored Warrior: stronger at a battleground or plains
					put("scout", "5_90");       // Rohirrim Scout: valiant; a regroup ability

					put("eowyn", "5_122");      // Eowyn, Daughter of Eomund: a unique [rohan] companion
					put("dwarf", "1_7");        // Dwarf Guard: a non-unique [dwarven] companion, vitality 2
					put("herdsman", "7_247");   // Rohirrim Herdsman: a non-unique [rohan] ally, vitality 2

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; Fellowship: exert Sam to remove a burden

					put("runner", "1_178");     // Goblin Runner: a minion of vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErkenbrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Erkenbrand, Kinsman of the House of Eorl
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: When you play Erkenbrand, remove up to 3 non-unique [rohan] companions in the dead pile from the game.
		 * 	Each exhausted companion gains <b>valiant</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erkenbrand");

		assertEquals("Erkenbrand", card.getBlueprint().getTitle());
		assertEquals("Kinsman of the House of Eorl", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
		// Valiant is something he gives the exhausted, himself included, and not something he has.
		assertFalse(scn.HasKeyword(card, Keyword.VALIANT));
	}

	@Test
	public void PlayedAndAnExhaustedCompanionIsValiantOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(erkenbrand);

		scn.StartGame();

		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		assertTrue(scn.IsExhausted(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.FreepsPlayAvailable(erkenbrand));

		scn.FreepsPlayCard(erkenbrand);

		assertInZone(Zone.FREE_CHARACTERS, erkenbrand);
		assertTrue(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(erkenbrand, Keyword.VALIANT));
		// Nobody is in the dead pile, so nothing is asked.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovesThreeOfFourNonUniqueRohanCompanionsInTheDeadPileFromTheGameAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var guard = scn.GetFreepsCard("guard");
		var elite = scn.GetFreepsCard("elite");
		var rider = scn.GetFreepsCard("rider");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var eowyn = scn.GetFreepsCard("eowyn");
		var dwarf = scn.GetFreepsCard("dwarf");
		var herdsman = scn.GetFreepsCard("herdsman");
		var veteran = scn.GetFreepsCard("veteran");
		var warrior = scn.GetFreepsCard("warrior");
		var theirScout = scn.GetShadowCard("scout");

		scn.MoveCardsToDeadPile(guard, elite, rider, rohirrim);
		// In the dead pile and not non-unique, not [rohan], not a companion:
		scn.MoveCardsToDeadPile(eowyn, dwarf, herdsman);
		// Non-unique [rohan] companions that are not in the dead pile, or not in this player's:
		scn.MoveCardsToDiscard(veteran);
		scn.MoveCompanionsToTable(warrior);
		scn.MoveCardsToDeadPile(theirScout);
		scn.MoveCardsToHand(erkenbrand);

		scn.StartGame();

		assertEquals(7, scn.GetFreepsDeadCount());
		assertInZone(Zone.DEAD, guard, elite, rider, rohirrim, eowyn, dwarf, herdsman, theirScout);

		scn.FreepsPlayCard(erkenbrand);

		assertInZone(Zone.FREE_CHARACTERS, erkenbrand);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, elite, rider, rohirrim));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eowyn, dwarf, herdsman, veteran, warrior, theirScout));
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(guard, elite, rider);

		assertInZone(Zone.REMOVED, guard, elite, rider);
		assertInZone(Zone.DEAD, rohirrim, eowyn, dwarf, herdsman, theirScout);
		assertEquals(4, scn.GetFreepsDeadCount());
		assertInDiscard(veteran);
		assertInZone(Zone.FREE_CHARACTERS, warrior);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayRemoveNobodyFromTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var guard = scn.GetFreepsCard("guard");
		var elite = scn.GetFreepsCard("elite");
		scn.MoveCardsToDeadPile(guard, elite);
		scn.MoveCardsToHand(erkenbrand);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsDeadCount());

		scn.FreepsPlayCard(erkenbrand);

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, elite));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DEAD, guard, elite);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertInZone(Zone.FREE_CHARACTERS, erkenbrand);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAnotherRohanCompanionBesideErkenbrandRemovesNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var guard = scn.GetFreepsCard("guard");
		var elite = scn.GetFreepsCard("elite");
		var veteran = scn.GetFreepsCard("veteran");
		scn.MoveCompanionsToTable(erkenbrand);
		scn.MoveCardsToDeadPile(guard, elite);
		scn.MoveCardsToHand(veteran);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(veteran);

		// 2 for the Veteran Horseman
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, veteran);
		assertInZone(Zone.DEAD, guard, elite);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachExhaustedCompanionOfAnyCultureIsValiantAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		var elite = scn.GetFreepsCard("elite");
		var rider = scn.GetFreepsCard("rider");
		var sam = scn.GetFreepsCard("sam");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(erkenbrand, aragorn, dwarf, elite, rider, sam);
		scn.MoveAlliesToTable(herdsman);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Exhausted: one vitality left.
		scn.AddWoundsToChar(erkenbrand, 2);   // vitality 3
		scn.AddWoundsToChar(aragorn, 3);      // vitality 4
		scn.AddWoundsToChar(dwarf, 1);        // vitality 2
		scn.AddWoundsToChar(elite, 2);        // vitality 3
		scn.AddWoundsToChar(frodo, 3);        // vitality 4
		// Not exhausted: Sam has vitality 4 and is one wound short, the Rider of Rohan is unwounded.
		scn.AddWoundsToChar(sam, 2);
		// Exhausted and no companion: an ally of vitality 2, and a minion of vitality 1.
		scn.AddWoundsToChar(herdsman, 1);

		assertTrue(scn.IsExhausted(erkenbrand));
		assertTrue(scn.IsExhausted(aragorn));
		assertTrue(scn.IsExhausted(dwarf));
		assertTrue(scn.IsExhausted(elite));
		assertTrue(scn.IsExhausted(frodo));
		assertTrue(scn.HasKeyword(erkenbrand, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(dwarf, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(elite, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(frodo, Keyword.VALIANT));

		assertFalse(scn.IsExhausted(sam));
		assertFalse(scn.HasKeyword(sam, Keyword.VALIANT));
		assertFalse(scn.IsExhausted(rider));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));

		assertTrue(scn.IsExhausted(herdsman));
		assertFalse(scn.HasKeyword(herdsman, Keyword.VALIANT));
		assertTrue(scn.IsExhausted(runner));
		assertFalse(scn.HasKeyword(runner, Keyword.VALIANT));

		// Sam exerts for his own ability and is exhausted from then on.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);

		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.IsExhausted(sam));
		assertTrue(scn.HasKeyword(sam, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
