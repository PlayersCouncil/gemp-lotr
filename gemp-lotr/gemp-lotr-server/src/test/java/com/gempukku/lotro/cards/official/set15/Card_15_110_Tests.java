package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marauder", "15_110");
					put("marauder2", "15_110");
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc minion; strength +2 only at an underground site
					put("halftroll", "14_12");    // Half-troll of Far Harad: [orc] minion who is no Orc; a start-of-assignment trigger never reached
					put("goblin", "11_137");      // Scurrying Goblin: [orc] Orc, cost 1; adds (2) only when played at an underground site
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc minion; his text answers only a weapon played on him
					put("beckoning", "12_83");    // The Beckoning Shadow: [orc] support-area condition, no minion; its text needs 5 burdens
					put("elf", "1_53");           // Lorien Elf: a Free Peoples companion, here only as a card in hand
					put("forearmed", "6_16");     // Forearmed: a Free Peoples skirmish event, here only as a card in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardMarauderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Isengard Marauder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: To play, discard 3 cards from your hand.<br>While you can spot 2 other [orc] minions, this minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marauder");

		assertEquals("Isengard Marauder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToPlayThreeCardsOfAnyKindAreDiscardedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var chieftain = scn.GetShadowCard("chieftain");
		var beckoning = scn.GetShadowCard("beckoning");
		var elf = scn.GetShadowCard("elf");
		var forearmed = scn.GetShadowCard("forearmed");

		scn.StartGame();
		// A minion, a Shadow condition, a Free Peoples companion and a Free Peoples event beside the Marauder.
		scn.MoveCardsToHand(marauder, chieftain, beckoning, elf, forearmed);
		assertInHand(marauder, chieftain, beckoning, elf, forearmed);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(marauder));
		scn.ShadowPlayCard(marauder);

		// Every other card in hand may pay; the Marauder itself is being played and is no longer there.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, beckoning, elf, forearmed));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(marauder));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(chieftain, beckoning, forearmed);

		assertInDiscard(chieftain, beckoning, forearmed);
		assertEquals(3, scn.GetShadowDiscardCount());
		assertInHand(elf);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, marauder);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyTwoOtherCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var chieftain = scn.GetShadowCard("chieftain");
		var elf = scn.GetShadowCard("elf");

		scn.StartGame();
		scn.MoveCardsToHand(marauder, chieftain, elf);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The pool can pay for the Marauder; the hand holds two cards beside it.
		assertTrue(scn.GetTwilight() >= 4);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(marauder));
		assertTrue(scn.ShadowPlayAvailable(chieftain));
	}

	@Test
	public void WithTwoOtherOrcMinionsOfAnyRaceThisMinionIsFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		// An [orc] Orc and an [orc] Half-troll; laid in the fellowship phase to read the standing text (Q-0228).
		scn.MoveMinionsToTable(marauder, denizen, halftroll);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(marauder, Keyword.FIERCE));
		// The others, of the Marauder's kind, are not made fierce.
		assertFalse(scn.HasKeyword(denizen, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(halftroll, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneOtherOrcMinionItIsNotFierceUntilASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var denizen = scn.GetShadowCard("denizen");
		var chieftain = scn.GetShadowCard("chieftain");
		var beckoning = scn.GetShadowCard("beckoning");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		// Beside the one [orc] minion, a [sauron] Orc and an [orc] card that is no minion; the Marauder itself is
		// not counted.
		scn.MoveMinionsToTable(marauder, denizen, chieftain);
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(goblin);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(marauder, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(goblin));

		// A second [orc] Orc played by legal play.
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertTrue(scn.HasKeyword(marauder, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoMaraudersAndAnotherOrcMinionAreEachFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var marauder2 = scn.GetShadowCard("marauder2");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		// Each copy spots the other copy and the Denizen.
		scn.MoveMinionsToTable(marauder, marauder2, denizen);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(marauder, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(marauder2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(denizen, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
