package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "6_32");
					// Ent Avenger: Ent companion, cost 4, not unique; strength +2 only with 3 Ents. One for the
					// table and one for the hand, the other Ent whose cost nothing changes
					put("avenger", "6_27");
					put("avenger2", "6_27");
					// Forest Guardian: Ent companion of another title, cost 2; unhasty, and an assignment ability
					// used only on purpose
					put("guardian", "5_17");
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

	@Test
	public void HostofFangornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Host of Fangorn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 13
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 10
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 2 Ent companions.<br>Host of Fangorn's twilight cost is -2 for each Ent or unbound Hobbit you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Host of Fangorn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(13, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilASecondEntCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var avenger = scn.GetFreepsCard("avenger");
		var guardian = scn.GetFreepsCard("guardian");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(host, guardian);

		scn.StartGame();

		// One Ent companion: Treebeard is an Ent but an ally, and Gandalf a companion but no Ent.
		assertInHand(host);
		assertFalse(scn.FreepsPlayAvailable(host));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(guardian);
		assertInZone(Zone.FREE_CHARACTERS, guardian);

		// The Ent Avenger and the Forest Guardian: two Ent companions of two titles.
		assertTrue(scn.FreepsPlayAvailable(host));
		scn.FreepsPlayCard(host);
		assertInZone(Zone.FREE_CHARACTERS, host);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CostsThirteenWithNothingToCountAndElevenOnceAnUnboundHobbitIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(host, avenger2, merry);

		scn.StartGame();

		// Frodo and Sam are Ring-bound, the Bounder is an ally, and Gandalf is neither Ent nor Hobbit.
		assertEquals(13, scn.GetTwilightCost(host));
		assertEquals(4, scn.GetTwilightCost(avenger2));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// One unbound Hobbit: 13 - 2. The Ent Avenger in the same hand costs what it did.
		assertEquals(11, scn.GetTwilightCost(host));
		assertEquals(4, scn.GetTwilightCost(avenger2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachEntCompanionOrAllyAndEachUnboundHobbitTakesTwoOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, merry, pippin, sam, gandalf);
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCardsToHand(host);

		scn.StartGame();

		// 13 - 2 for the Ent Avenger - 2 for Treebeard - 2 for Merry - 2 for Pippin; nothing for Frodo, Sam, the
		// Bounder or Gandalf.
		assertEquals(5, scn.GetTwilightCost(host));
	}

	@Test
	public void PlayedFromHandForThirteenLessTwoForEachOfTwoEntCompanionsAndAnUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var avenger = scn.GetFreepsCard("avenger");
		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(avenger, guardian, merry);
		scn.MoveCardsToHand(host);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(host);

		// 13 - 2 for the Ent Avenger - 2 for the Forest Guardian - 2 for Merry. He does not count himself.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, host);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
