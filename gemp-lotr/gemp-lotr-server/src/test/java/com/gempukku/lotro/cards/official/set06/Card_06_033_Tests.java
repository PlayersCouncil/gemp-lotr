package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quickbeam", "6_33");
					// Ent Avenger: Ent companion, cost 4, not unique; strength +2 only with 3 Ents. One for the
					// table and one for the hand, the other Ent whose cost nothing changes
					put("avenger", "6_27");
					put("avenger2", "6_27");
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: the Ent who is an ally
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound [shire] Hobbit, cost 1
					put("pippin", "103_31");    // Pippin, Trollslayer: an unbound Hobbit of [gondor]
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit who is Ring-bound
					put("bounder", "1_286");    // Bounder: a Hobbit ally, and only a companion is unbound
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf], unbound, no Ent and no Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBloomOfHealthScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quickbeam", "6_33");
					// Merry and Pippin, In the Bloom of Health: unbound Hobbits, each "Add 1 to the number of Ents
					// you can spot"
					put("merry", "17_107");
					put("pippin", "17_109");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void QuickbeamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Quickbeam, Bregalad
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Quickbeam's twilight cost is -1 for each Ent or unbound Hobbit you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quickbeam");

		assertEquals("Quickbeam", card.getBlueprint().getTitle());
		assertEquals("Bregalad", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CostsOneLessOnceAnUnboundHobbitIsPlayedBesideThoseWhoDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(quickbeam, avenger2, merry);

		scn.StartGame();

		// Frodo and Sam are Ring-bound, the Bounder is an ally, and Gandalf is neither Ent nor Hobbit.
		assertEquals(4, scn.GetTwilightCost(quickbeam));
		assertEquals(4, scn.GetTwilightCost(avenger2));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// One unbound Hobbit. The Ent Avenger in the same hand costs what it did.
		assertEquals(3, scn.GetTwilightCost(quickbeam));
		assertEquals(4, scn.GetTwilightCost(avenger2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachEntCompanionOrAllyAndEachUnboundHobbitTakesOneOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, pippin, sam, gandalf);
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCardsToHand(quickbeam);

		scn.StartGame();

		// 4 - 1 for the Ent Avenger - 1 for Treebeard - 1 for Pippin; nothing for Frodo, Sam, the Bounder or
		// Gandalf.
		assertEquals(1, scn.GetTwilightCost(quickbeam));
	}

	@Test
	public void PlayedFromHandForFourLessOneForAnEntAndOneForAnUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var avenger = scn.GetFreepsCard("avenger");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(avenger, merry, sam);
		scn.MoveCardsToHand(quickbeam);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(quickbeam));
		scn.FreepsPlayCard(quickbeam);

		// 4 - 1 for the Ent Avenger - 1 for Merry
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, quickbeam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBothBloomOfHealthHobbitsHeIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBloomOfHealthScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(quickbeam);
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();

		// 4 - 2 for the two unbound Hobbits - 2 for the Ents their texts add to the number spotted. With no
		// discount the pool would read 4 after the play, and with the Hobbits alone 2.
		assertEquals(0, scn.GetTwilightCost(quickbeam));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(quickbeam);

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, quickbeam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
