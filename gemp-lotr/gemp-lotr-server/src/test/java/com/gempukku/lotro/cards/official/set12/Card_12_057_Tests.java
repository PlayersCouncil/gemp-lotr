package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spy", "12_57");
					// A second copy: each counts the other.
					put("spy2", "12_57");
					// Goblin Patrol Troop: a [moria] minion with no text.
					put("troop", "1_177");
					// Wielding the Ring: a [men] condition, resistance -2, on a companion who is not the Ring-bearer.
					// To play, spot a [men] minion (the Spy).
					put("wielding", "11_105");

					// Aragorn, Ranger of the North: a [gondor] Man companion, resistance 6.  His maneuver ability is
					// never used.
					put("aragorn", "1_89");
					// Guard of the White Tree: an unbound [gondor] Man companion, resistance 5.  His skirmish ability
					// is never used.
					put("guard", "13_67");
					// Galadriel, Bearer of Wisdom: an [elven] Elf companion, resistance 3; her own resistance bonus
					// is only while she bears an artifact or The One Ring, and she bears neither.
					put("galadriel", "9_14");
					// Ottar, Man of Laketown: a [gandalf] Man ally, resistance 0 as every ally's (CR 4.2).  "To
					// play, spot Gandalf" is passed over by the cheat, and his fellowship ability is never used.
					put("ottar", "1_80");
					// Rohan Worker: a follower, no character (CR 4.2 "follower"), lying in the support area.  Its
					// Aid is asked only in a maneuver phase, which no test of the count reaches.
					put("worker", "15_135");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spy", "12_57");
					// Aragorn, Ranger of the North: a [gondor] Man companion, resistance 6.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				// Gimli, Bearer of Grudges, as Ring-bearer: resistance 4, +1 for his damage +1.  His burden
				// trigger is only at the start of his own skirmishes, which no test reaches.
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorruptedSpyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Corrupted Spy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each other character you can spot.<br>While you can spot a companion who has resistance 3 or less, this minion is <b>damage +1</b>.<br>While you can spot a companion who has resistance 0, this minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spy");

		assertEquals("Corrupted Spy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthRisesByOneAsACompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spy);
		scn.FreepsRefreshDecision();

		// Frodo is the one other character: 6 + 1.
		assertEquals(7, scn.GetStrength(spy));

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// Frodo and Aragorn: 6 + 2.
		assertEquals(8, scn.GetStrength(spy));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CountsEveryOtherCompanionAllyAndMinionButNotItselfAFollowerOrACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var spy2 = scn.GetShadowCard("spy2");
		var troop = scn.GetShadowCard("troop");
		var wielding = scn.GetShadowCard("wielding");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ottar = scn.GetFreepsCard("ottar");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToSupportArea(worker);

		scn.StartGame();
		scn.MoveMinionsToTable(spy, spy2, troop);
		scn.AttachCardsTo(aragorn, wielding);
		assertInZone(Zone.SUPPORT, worker);
		assertAttachedTo(wielding, aragorn);

		// Frodo, Aragorn, Ottar, the Troop and the other copy: 6 + 5 for each.  Neither counts itself, and the
		// follower and the condition are no characters.
		assertEquals(11, scn.GetStrength(spy));
		assertEquals(11, scn.GetStrength(spy2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DamagePlus1WhileACompanionHasResistance3AndNotAt4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetFreepsCard("guard");
		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(ottar);

		scn.StartGame();
		scn.MoveMinionsToTable(spy, troop);
		scn.AddBurdens(1);

		// One burden: the Guard 5 - 1 = 4, Frodo 10 - 1 = 9.  Ottar's 0 is an ally's, and no minion has a resistance
		// that counts.
		assertEquals(4, scn.GetResistance(guard));
		assertEquals(9, scn.GetResistance(scn.GetRingBearer()));
		assertEquals(0, scn.GetResistance(ottar));
		assertEquals(0, scn.GetKeywordCount(spy, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(spy, Keyword.FIERCE));

		scn.AddBurdens(1);
		assertEquals(3, scn.GetResistance(guard));
		assertEquals(1, scn.GetKeywordCount(spy, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(spy, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DamagePlus1WhileTheRingBearerHasResistance3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var spy = scn.GetShadowCard("spy");
		var gimli = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spy);
		scn.AddBurdens(1);

		// Gimli 4 + 1 - 1 = 4, Aragorn 6 - 1 = 5.
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetKeywordCount(spy, Keyword.DAMAGE));

		scn.AddBurdens(1);
		// The Ring-bearer alone at 3; Aragorn at 4.
		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(1, scn.GetKeywordCount(spy, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WieldingTheRingPlayedOnACompanionOfResistance5MakesItDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var wielding = scn.GetShadowCard("wielding");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.SetShadowHand(wielding);

		scn.StartGame();
		scn.MoveMinionsToTable(spy);
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(5, scn.GetResistance(guard));
		assertEquals(0, scn.GetKeywordCount(spy, Keyword.DAMAGE));

		scn.ShadowPlayCard(wielding);
		scn.ShadowChooseCard(guard);
		assertAttachedTo(wielding, guard);

		// 5 - 2 = 3.
		assertEquals(3, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(1, scn.GetKeywordCount(spy, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(spy, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FierceWhileACompanionHasResistance0AndNotAt1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetFreepsCard("guard");
		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(ottar);

		scn.StartGame();
		scn.MoveMinionsToTable(spy, troop);
		scn.AddBurdens(4);

		// Four burdens: the Guard 5 - 4 = 1, Frodo 10 - 4 = 6.  Ottar's 0 is an ally's.
		assertEquals(1, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(scn.GetRingBearer()));
		assertEquals(0, scn.GetResistance(ottar));
		assertFalse(scn.HasKeyword(spy, Keyword.FIERCE));

		scn.AddBurdens(1);
		assertEquals(0, scn.GetResistance(guard));
		assertTrue(scn.HasKeyword(spy, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FierceWhileAnElfCompanionHasResistance0() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var galadriel = scn.GetFreepsCard("galadriel");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(galadriel, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spy);
		scn.AddBurdens(2);

		// Galadriel 3 - 2 = 1, Aragorn 6 - 2 = 4.
		assertEquals(1, scn.GetResistance(galadriel));
		assertFalse(scn.HasKeyword(spy, Keyword.FIERCE));

		scn.AddBurdens(1);
		assertEquals(0, scn.GetResistance(galadriel));
		assertEquals(3, scn.GetResistance(aragorn));
		assertTrue(scn.HasKeyword(spy, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WieldingTheRingPlayedOnACompanionOfResistance2MakesItFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spy = scn.GetShadowCard("spy");
		var wielding = scn.GetShadowCard("wielding");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.SetShadowHand(wielding);

		scn.StartGame();
		scn.MoveMinionsToTable(spy);
		scn.AddBurdens(3);
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Three burdens: the Guard 5 - 3 = 2, Frodo 10 - 3 = 7.
		assertEquals(2, scn.GetResistance(guard));
		assertFalse(scn.HasKeyword(spy, Keyword.FIERCE));

		scn.ShadowPlayCard(wielding);
		assertAttachedTo(wielding, guard);

		// 2 - 2 = 0.
		assertEquals(0, scn.GetResistance(guard));
		assertTrue(scn.HasKeyword(spy, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
