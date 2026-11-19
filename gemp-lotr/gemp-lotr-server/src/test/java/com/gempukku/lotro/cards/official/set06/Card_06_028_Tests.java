package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horde", "6_28");
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

					// Minions of two cultures, with no text that reaches a test here
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1; adds (2) when played
					put("runner2", "1_178");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EntHordeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Ent Horde
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 15
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 10
		 * Vitality: 5
		 * Resistance: 6
		 * Game Text: To play, spot 2 Ent companions.<br>Ent Horde's twilight cost is -2 for each Ent or unbound Hobbit you can spot.<br>While you can spot more minions than companions, Ent Horde is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horde");

		assertEquals("Ent Horde", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(15, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilASecondEntCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetFreepsCard("horde");
		var avenger = scn.GetFreepsCard("avenger");
		var guardian = scn.GetFreepsCard("guardian");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(horde, guardian);

		scn.StartGame();

		// One Ent companion: Treebeard is an Ent but an ally, and Gandalf a companion but no Ent.
		assertInHand(horde);
		assertFalse(scn.FreepsPlayAvailable(horde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(guardian);
		assertInZone(Zone.FREE_CHARACTERS, guardian);

		// The Ent Avenger and the Forest Guardian: two Ent companions of two titles.
		assertTrue(scn.FreepsPlayAvailable(horde));
		scn.FreepsPlayCard(horde);
		assertInZone(Zone.FREE_CHARACTERS, horde);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CostsFifteenWithNothingToCountAndThirteenOnceAnUnboundHobbitIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetFreepsCard("horde");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(horde, avenger2, merry);

		scn.StartGame();

		// Frodo and Sam are Ring-bound, the Bounder is an ally, and Gandalf is neither Ent nor Hobbit.
		assertEquals(15, scn.GetTwilightCost(horde));
		assertEquals(4, scn.GetTwilightCost(avenger2));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// One unbound Hobbit: 15 - 2. The Ent Avenger in the same hand costs what it did.
		assertEquals(13, scn.GetTwilightCost(horde));
		assertEquals(4, scn.GetTwilightCost(avenger2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachEntCompanionOrAllyAndEachUnboundHobbitTakesTwoOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetFreepsCard("horde");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, merry, pippin, sam, gandalf);
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCardsToHand(horde);

		scn.StartGame();

		// 15 - 2 for the Ent Avenger - 2 for Treebeard - 2 for Merry - 2 for Pippin; nothing for Frodo, Sam, the
		// Bounder or Gandalf.
		assertEquals(7, scn.GetTwilightCost(horde));
	}

	@Test
	public void PlayedFromHandForFifteenLessTwoForEachOfTwoEntCompanionsAndAnUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetFreepsCard("horde");
		var avenger = scn.GetFreepsCard("avenger");
		var guardian = scn.GetFreepsCard("guardian");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(avenger, guardian, merry);
		scn.MoveCardsToHand(horde);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(horde);

		// 15 - 2 for the Ent Avenger - 2 for the Forest Guardian - 2 for Merry. Ent Horde does not count itself.
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, horde);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsDefenderPlusOneOnlyOnceMoreMinionsThanCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetFreepsCard("horde");
		var treebeard = scn.GetFreepsCard("treebeard");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(horde);
		// An ally, who is no companion: counted among the companions, he would make 3 against 3 below.
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(runner2);

		scn.StartGame();

		// Two companions (Frodo and Ent Horde) against two minions: not more.
		scn.MoveMinionsToTable(runner, brigand);
		assertFalse(scn.HasKeyword(horde, Keyword.DEFENDER));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(horde, Keyword.DEFENDER));

		// A third minion: three against two.
		scn.ShadowPlayCard(runner2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);

		assertTrue(scn.HasKeyword(horde, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(frodo, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
