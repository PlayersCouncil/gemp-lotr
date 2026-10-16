package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_056_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, who has the Frodo signet.  His fellowship ability (exert another
	// companion with the Frodo signet to heal Frodo) is never used here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coming", "101_56");
					put("coming2", "101_56");

					// Merry, Friend to Sam and Pippin, Friend to Frodo: the Frodo signet, twilight cost 1, vitality 4,
					// and nothing that happens when they are played.
					put("merry", "1_302");
					put("pippin", "1_306");
					// Merry, Master Holbytla: a [rohan] Merry with the Aragorn signet.  Pippin, Trollslayer: a [gondor]
					// Pippin with the Gandalf signet.  Both are enduring, which changes their strength and nothing else
					// here; the rest of their text is about [rohan] Men exerting and about skirmishes.
					put("holbytla", "103_85");
					put("trollslayer", "103_31");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, Frodo signet, vitality 3
					put("gimli", "2_121");      // Gimli, Dwarf of the Mountain-race: [dwarven] Dwarf, Frodo signet, vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit with the Aragorn signet
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the Gandalf signet
					put("guard", "1_7");        // Dwarf Guard: no signet, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WereComingTooStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: We're Coming, Too!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Merry and Pippin to heal two companions with the Frodo signet.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("coming");

		assertEquals("We're Coming, Too!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HealsTwoChosenCompanionsWithTheFrodoSignetOnceEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetFreepsCard("coming");
		var frodo = scn.GetRingBearer();
		// The Merry and the Pippin who are spotted are no [shire] cards and do not have the Frodo signet: they let
		// the event be played and cannot themselves be healed by it.
		var holbytla = scn.GetFreepsCard("holbytla");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(holbytla, trollslayer, boromir, gimli, sam, aragorn, guard);
		scn.MoveCardsToHand(coming);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(holbytla, 1);
		scn.AddWoundsToChar(trollslayer, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(guard, 1);

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		assertTrue(scn.FreepsPlayAvailable(coming));

		scn.FreepsPlayCard(coming);

		// The three wounded companions with the Frodo signet, of three cultures, and nobody else: not the Merry
		// and the Sam who have the Aragorn signet, not the Pippin and the Aragorn who have Gandalf's, not the
		// Dwarf Guard with none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(holbytla, trollslayer, sam, aragorn, guard));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(boromir, gimli);
		// One wound comes off each of the two: Boromir had two and keeps one.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(holbytla));
		assertEquals(1, scn.GetWoundsOn(trollslayer));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(coming);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithMerryAndNoPippinUntilPippinIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetFreepsCard("coming");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(coming, merry, pippin);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsPlayAvailable(coming));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInHand(pippin);
		assertFalse(scn.FreepsPlayAvailable(coming));

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.FreepsPlayAvailable(coming));
		// 1 for Merry + 1 for Pippin
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Frodo and Gimli are the only two who are wounded, so there is nothing to choose.
		scn.FreepsPlayCard(coming);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(coming);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithPippinAndNoMerryUntilMerryIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetFreepsCard("coming");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(coming, merry, pippin);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertInHand(merry);
		assertFalse(scn.FreepsPlayAvailable(coming));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.FreepsPlayAvailable(coming));
		assertInHand(coming);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealsTheOnlyWoundedCompanionWithTheSignetUnaskedAndCanStillBePlayedWithNobodyToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coming = scn.GetFreepsCard("coming");
		var coming2 = scn.GetFreepsCard("coming2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, pippin, sam);
		scn.MoveCardsToHand(coming, coming2);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsPlayAvailable(coming));

		// One companion with the Frodo signet is wounded where the event asks for two: it heals the one.  Sam,
		// with the Aragorn signet, does not make up the number.
		scn.FreepsPlayCard(coming);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(coming);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Nobody with the Frodo signet is wounded now.  Spotting Merry and Pippin is all the event asks for,
		// so the second copy can be played all the same, and heals nobody.
		assertInHand(coming2);
		assertTrue(scn.FreepsPlayAvailable(coming2));

		scn.FreepsPlayCard(coming2);
		assertInDiscard(coming2);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
