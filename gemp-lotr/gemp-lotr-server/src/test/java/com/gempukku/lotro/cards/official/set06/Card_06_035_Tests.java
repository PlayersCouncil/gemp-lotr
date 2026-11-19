package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skinbark", "6_35");
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
					// Goblin Marksman: a [moria] archer and nothing else; each adds 1 to the minion archery total
					put("marksman1", "1_176");
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SkinbarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Skinbark, Fladrif
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 7
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: Skinbark's twilight cost is -1 for each Ent or unbound Hobbit you can spot.<br><b>Response:</b> If an unbound Hobbit is about to take a wound, exert Skinbark to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("skinbark");

		assertEquals("Skinbark", card.getBlueprint().getTitle());
		assertEquals("Fladrif", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CostsOneLessOnceAnUnboundHobbitIsPlayedBesideThoseWhoDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(skinbark, avenger2, merry);

		scn.StartGame();

		// Frodo and Sam are Ring-bound, the Bounder is an ally, and Gandalf is neither Ent nor Hobbit.
		assertEquals(7, scn.GetTwilightCost(skinbark));
		assertEquals(4, scn.GetTwilightCost(avenger2));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// One unbound Hobbit. The Ent Avenger in the same hand costs what it did.
		assertEquals(6, scn.GetTwilightCost(skinbark));
		assertEquals(4, scn.GetTwilightCost(avenger2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachEntCompanionOrAllyAndEachUnboundHobbitTakesOneOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, merry, pippin, sam, gandalf);
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCardsToHand(skinbark);

		scn.StartGame();

		// 7 - 1 for the Ent Avenger - 1 for Treebeard - 1 for Merry - 1 for Pippin; nothing for Frodo, Sam, the
		// Bounder or Gandalf.
		assertEquals(3, scn.GetTwilightCost(skinbark));
	}

	@Test
	public void PlayedFromHandForSevenLessOneForAnEntAndOneForAnUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var avenger = scn.GetFreepsCard("avenger");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(avenger, merry, sam);
		scn.MoveCardsToHand(skinbark);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(skinbark));
		scn.FreepsPlayCard(skinbark);

		// 7 - 1 for the Ent Avenger - 1 for Merry
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, skinbark);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToPreventAnArcheryWoundToAnUnboundHobbitOfEitherCultureButNotToSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(skinbark, merry, pippin, sam);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3");
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetShadowArcheryTotal());
		scn.PassArcheryActions();

		assertEquals(0, scn.GetWoundsOn(skinbark));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsChooseCard(merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		scn.FreepsChooseOptionalTrigger(skinbark);
		assertEquals(1, scn.GetWoundsOn(skinbark));
		assertEquals(0, scn.GetWoundsOn(merry));

		// Pippin, Trollslayer is a [gondor] Hobbit: unbound all the same.
		scn.FreepsChooseCard(pippin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		scn.FreepsChooseOptionalTrigger(skinbark);
		assertEquals(2, scn.GetWoundsOn(skinbark));
		assertEquals(0, scn.GetWoundsOn(pippin));

		// Sam is a Hobbit, but Ring-bound: the last wound is placed with nothing offered, and the archery phase ends.
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(skinbark));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundToACompanionWhoIsNoHobbitOrToTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(skinbark, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetShadowArcheryTotal());
		scn.PassArcheryActions();

		assertEquals(0, scn.GetWoundsOn(skinbark));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Gandalf is no Hobbit: the next wound is asked for straight away.
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		assertTrue(scn.FreepsDecisionAvailable("Choose character to assign archery wound"));

		// Frodo is a Hobbit, but the Ring-bearer is Ring-bound.
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(skinbark));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileSkinbarkIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(skinbark, merry);

		scn.StartGame();
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(skinbark, 3);
		scn.MoveMinionsToTable("marksman1");
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		assertEquals(3, scn.GetWoundsOn(skinbark));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetWoundsOn(skinbark));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundToAHobbitAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skinbark = scn.GetFreepsCard("skinbark");
		var bounder = scn.GetFreepsCard("bounder");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCompanionsToTable(skinbark);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(skinbark));
		assertTrue(scn.ShadowActionAvailable(assassin));

		// The Bounder is the only ally: he is wounded unasked.
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(skinbark));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(skinbark));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
