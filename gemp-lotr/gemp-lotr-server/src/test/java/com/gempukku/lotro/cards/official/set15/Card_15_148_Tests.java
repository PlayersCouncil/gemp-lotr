package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flower", "15_148");

					// Hobbits.  The Ring-bearer is Frodo, Son of Drogo: strength 3 (and The Ruling Ring's +1), resistance 10.
					put("sam", "1_311");            // Sam, Son of Hamfast: strength 3, vitality 4, resistance 5; Fellowship: exert Sam to remove a burden
					put("merry", "1_302");          // Merry, Friend to Sam: strength 3, vitality 4, resistance 6; a skirmish ability used only on purpose
					put("pippin", "103_31");        // Pippin, Trollslayer: a [gondor] Hobbit, strength 3, vitality 4, resistance 6; enduring (kept unwounded)
					put("bilbo", "1_284");          // Bilbo, Retired Adventurer: Hobbit ally, vitality 3; a fellowship ability only
					put("gaffer", "1_291");         // The Gaffer, Sam's Father: Hobbit ally, strength 2; Fellowship: exert to heal Frodo or Sam
					put("ted", "18_75");            // Ted Sandyman: a [men] minion who is a Hobbit, strength 3, vitality 4; maneuver and regroup abilities only
					// Not Hobbits.
					put("goldberry", "9_51");       // Goldberry: a [shire] ally of no race, strength 4; her texts need a river
					put("smeagol", "103_116");      // Smeagol, Once Like Us: a [shire] companion of no race, strength 3, resistance 6; an assignment ability only
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4, resistance 6

					put("sword1", "1_299");         // Hobbit Sword: a [shire] hand weapon, strength +2, bearer must be a Hobbit; no other text
					put("sword2", "1_299");
					put("pipe", "1_292");           // The Gaffer's Pipe: a possession a Hobbit bears that is no weapon; a fellowship ability not used
					put("dagger", "18_69");         // Henchman's Dagger: a [men] hand weapon, strength +1, bearer must be a [men] minion
					// Followers with no printed resistance.  Rohan Worker: its texts answer a transfer or a skirmish
					// won (cheated on).  Elendil's Army: strength +2; Aid: exert a [gondor] companion.
					put("worker1", "15_135");
					put("worker2", "15_135");
					put("worker3", "15_135");
					put("worker4", "15_135");
					put("army", "18_47");

					put("savage", "1_151");         // Uruk Savage: damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetVitalityScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flower", "15_148");
					// Hobbits of vitality 4, as the Ring-bearer is.  Sam, Son of Hamfast: his fellowship ability
					// is not used.  Merry, Friend to Sam: a skirmish ability only.
					put("sam", "1_311");
					put("merry", "1_302");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a Man of vitality 4
					put("sword", "1_299");          // Hobbit Sword: a hand weapon, strength +2, on a Hobbit; no other text
					put("ranger", "1_112");         // Ranger's Sword: a hand weapon on Aragorn, strength +2 and damage +1
					// Ted Sandyman: a [men] minion who is a Hobbit, vitality 4; maneuver and regroup abilities only
					put("ted", "18_75");
					// Long Battle Bow: a ranged weapon on a [men] minion, strength +1, and bearer is an archer.
					// Its when-played text is not used: it is cheated on.
					put("bow", "11_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LittleGoldenFlowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Little Golden Flower
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each Hobbit bearing a hand weapon is vitality +1.<br>Each Hobbit bearing a follower is resistance +1.<br>Each unwounded Hobbit is strength +1.
		*/

		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");

		assertEquals("Little Golden Flower", flower.getBlueprint().getTitle());
		assertNull(flower.getBlueprint().getSubtitle());
		assertTrue(flower.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, flower.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, flower.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, flower.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(flower, Keyword.SUPPORT_AREA));
		assertEquals(3, flower.getBlueprint().getTwilightCost());
	}

	@Test
	public void AHobbitBearingAHandWeaponIsVitalityPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetVitalityScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(sam, merry, aragorn);
		scn.MoveCardsToSupportArea(flower);
		scn.AttachCardsTo(aragorn, ranger);
		scn.MoveCardsToHand(sword);

		var ted = scn.GetShadowCard("ted");
		var bow = scn.GetShadowCard("bow");

		scn.StartGame();
		// A Hobbit who bears a weapon that is no hand weapon: the only one there is sits on the other side.
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, bow);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetVitality(sam));
		assertEquals(4, scn.GetVitality(merry));
		assertEquals(4, scn.GetVitality(frodo));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(4, scn.GetVitality(ted));

		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry));
		scn.FreepsChooseCard(sam);
		assertAttachedTo(sword, sam);

		// The Hobbit with the hand weapon and nobody else: not the Hobbits without one, not the Man with
		// one, not the Hobbit with a bow.
		assertEquals(5, scn.GetVitality(sam));
		assertEquals(4, scn.GetVitality(merry));
		assertEquals(4, scn.GetVitality(frodo));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(4, scn.GetVitality(ted));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The extra point is real: with three wounds on him, Sam takes a fourth from Ted's bow and lives.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(2, scn.GetVitality(sam));
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(sam);
		assertEquals(4, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetVitality(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AGondorHobbitAHobbitAllyAndTheOpponentsHobbitMinionBearingHandWeaponsAreVitalityPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var pipe = scn.GetFreepsCard("pipe");
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(pippin, sword1);
		scn.AttachCardsTo(bilbo, sword2);
		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(flower);
		scn.MoveCardsToHand(dagger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, flower);
		assertAttachedTo(sword1, pippin);
		assertAttachedTo(sword2, bilbo);
		assertAttachedTo(pipe, frodo);
		assertInHand(dagger);

		// A [gondor] Hobbit companion and a Hobbit ally with hand weapons; the Ring-bearer with a pipe, no weapon.
		assertEquals(5, scn.GetVitality(pippin));
		assertEquals(4, scn.GetVitality(bilbo));
		assertEquals(4, scn.GetVitality(frodo));

		scn.MoveMinionsToTable(ted);
		assertEquals(4, scn.GetVitality(ted));

		// The Shadow player plays a [men] hand weapon on his Hobbit, the one [men] minion.
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(dagger));
		scn.ShadowPlayCard(dagger);
		assertAttachedTo(dagger, ted);
		assertEquals(5, scn.GetVitality(ted));
		assertEquals(4, scn.GetVitality(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachHobbitBearingAFollowerIsResistancePlus1WhateverTheirNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var worker1 = scn.GetFreepsCard("worker1");
		var worker2 = scn.GetFreepsCard("worker2");
		var worker3 = scn.GetFreepsCard("worker3");
		var worker4 = scn.GetFreepsCard("worker4");
		var army = scn.GetFreepsCard("army");
		var sword1 = scn.GetFreepsCard("sword1");
		scn.MoveCompanionsToTable(sam, merry, pippin, smeagol);
		// A follower on the Ring-bearer, on a [gondor] Hobbit and on a [shire] companion who is no Hobbit; two
		// on Sam.  Merry bears a possession and no follower.
		scn.AttachCardsTo(frodo, worker1);
		scn.AttachCardsTo(sam, worker2, worker3);
		scn.AttachCardsTo(pippin, worker4);
		scn.AttachCardsTo(smeagol, army);
		scn.AttachCardsTo(merry, sword1);

		scn.StartGame();
		assertAttachedTo(worker1, frodo);
		assertAttachedTo(worker2, sam);
		assertAttachedTo(worker3, sam);
		assertAttachedTo(worker4, pippin);
		assertAttachedTo(army, smeagol);
		assertAttachedTo(sword1, merry);

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(smeagol));

		scn.MoveCardsToSupportArea(flower);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, flower);

		assertEquals(11, scn.GetResistance(frodo));
		// One more, whatever the number of followers.
		assertEquals(6, scn.GetResistance(sam));
		assertEquals(7, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(smeagol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerTransferredToAHobbitByItsAidMakesHimResistancePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var army = scn.GetFreepsCard("army");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(flower, army);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, flower, army);
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		// Aid: exert a [gondor] companion to transfer Elendil's Army to a companion.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(army));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(army, sam);

		assertEquals(6, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachUnwoundedHobbitIsStrengthPlus1AndLosesItWhileWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		var goldberry = scn.GetFreepsCard("goldberry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(gaffer, goldberry);
		scn.AddWoundsToChar(merry, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(ted);

		// The Ruling Ring makes its bearer strength +1.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(2, scn.GetStrength(gaffer));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(goldberry));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.MoveCardsToSupportArea(flower);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, flower);
		// Unwounded Hobbits: the Ring-bearer, a [shire] companion, a [gondor] companion, an ally, and the Shadow
		// player's Hobbit minion.
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(gaffer));
		assertEquals(4, scn.GetStrength(ted));
		// A wounded Hobbit, not exhausted; an unwounded [shire] ally and an unwounded [shire] companion who are
		// no Hobbits; an unwounded Man.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(goldberry));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));

		// Sam exerts to remove a burden (there is none): he is wounded now.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetStrength(sam));

		// The Gaffer exerts to heal Sam, the only one of Frodo and Sam with a wound.
		scn.FreepsUseCardAction(gaffer);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(2, scn.GetStrength(gaffer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
