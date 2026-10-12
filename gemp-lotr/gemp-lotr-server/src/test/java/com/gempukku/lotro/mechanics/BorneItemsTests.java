package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * What a character may bear, and what a borne card's printed numbers do for its bearer.
 *
 * Comprehensive Rules 4.2, "bearer": "Each character may bear one possession or artifact of each class at one time.
 * ...  A character may bear only one hand weapon, only one ranged weapon, only one armor, only one cloak, and only
 * one staff.  Some possessions and artifacts do not have a class.  There is no limit to the number of cards without
 * a class that a character may bear.  When you play a card on a bearer, put it beneath the bearer with the left edge
 * of the card visible, for its card title and any attribute bonuses it has."  "Attribute bonus": "a positive
 * modifier for a character's strength, vitality, and/or resistance, written with a plus sign".  "Mounted": "A
 * mounted character means one who is 'bearing a possession with the class of mount.'"  "Artifact": "Though
 * artifacts are played and used much like possessions, they are a different card type."
 *
 * This class is what lets a card's own tests take its printed class and its printed bonuses as read (Ketura on
 * Q-0279, 2026-10-12): an item whose text says nothing about them is not played in its own file to show the class
 * barring a second, or the bonus reaching the bearer.  StatModifiersRule adds every printed strength, vitality and
 * resistance of the unhindered cards a character bears to his own, and every printed site number to a minion's:
 * The Ruling Ring's +1 is one of them (Frodo here is 3 + 1), and so is a printed minus (SkirmishStrengthTests lays
 * three on Sam).
 *
 * A site number printed on a card that is not a minion is found on five printed cards only, all of them [gondor]
 * Fortification conditions that the Free Peoples player moves from his support area onto a minion: Second Level
 * 7_118 (+2), Third Level 7_125 (+4), Portico 15_67 (strength -2, +2), Soldier's Cache 17_35 and 18_58 (+3).  No
 * printed possession or artifact has one.  Second Level is the one shown here.
 *
 * What is in another class and is not gone over again here:
 *   a hindered item, which gives no bonus and still bars another of its class: HinderedBorneCardTests;
 *   a bearer killed when a vitality bonus leaves him: KilledAtZeroVitalityTests;
 *   what a transfer costs, and an item that cannot be transferred: TransferCostTests,
 *     CardThatCannotBeTransferredTests;
 *   what a bearer's leaving play does to what he bears: LeavingPlayTests;
 *   a bonus in a skirmish's total: SkirmishStrengthTests; a resistance bonus against burdens: ResistanceTests.
 *
 * The table is the Fellowship block's, with Frodo, Son of Drogo as the Ring-bearer.
 */
public class BorneItemsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a Hobbit companion; his text is about tales
					put("merry", "1_302");      // Merry, Friend to Sam: a Hobbit companion; a skirmish ability, never used
					put("sword1", "1_299");     // Hobbit Sword: hand weapon, strength +2; "Bearer must be a Hobbit." is its whole text
					put("sword2", "1_299");
					// Salt from the Shire: a box, resistance +2.  "Bearer must be a Hobbit."  Its fellowship ability
					// discards it to remove a burden; its skirmish ability is never used.
					put("salt", "11_171");
					// Sting, Elven Long Knife: hand weapon, strength +1, vitality +1, resistance +1.  "Bearer must be
					// Bilbo or Frodo."  Its regroup ability is never used.
					put("sting", "18_113");
					// Mithril-coat: an ARTIFACT, armor.  "Bearer must be Frodo."  The rest is about archery and skirmishes.
					put("mithril", "2_105");
					// Orc Armor: a POSSESSION, armor.  "Bearer must be a Hobbit."  The rest is about skirmishes.
					put("orcarmor", "10_113");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability, never used
					put("rangersword", "1_112");// Ranger's Sword: a POSSESSION, hand weapon.  "Bearer must be Aragorn.  He is damage +1."
					// Andúril, King's Blade: an ARTIFACT, hand weapon.  "Bearer must be Aragorn.  If you cannot spot a
					// threat, Aragorn is defender +1."
					put("anduril", "7_80");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man; "Knight." is his whole text
					// Athelas: a possession with NO class.  "Bearer must be a [gondor] Man."  Its fellowship ability,
					// which discards it, is never used.
					put("athelas1", "1_94");
					put("athelas2", "1_94");
					put("athelas3", "1_94");

					// Valiant Elves for Naith Armaments.  Ordulus, Young Warrior 54_80: "To play, spot an Elf.  The
					// twilight cost of each ranged weapon played on Ordulus is -1."  Lorien Guardian 54_76: a regroup
					// ability, never used.  Lorien Elf 1_53 is an Elf who is not valiant: "To play, spot an Elf."
					put("ordulus", "54_80");
					put("guardian", "54_76");
					put("lorienelf", "1_53");
					// Naith Armaments: a hand weapon AND a ranged weapon.  "Bearer must be a valiant Elf."  Its other
					// text answers a skirmish won.
					put("naith1", "102_10");
					put("naith2", "102_10");
					put("bow", "1_41");         // Elven Bow: ranged weapon.  "Bearer must be an Elf.  Bearer is an archer."
					// Elven Sword: hand weapon.  "Bearer must be an Elf."  Its skirmish ability is never used.
					put("elvensword", "4_64");

					put("guard1", "5_83");      // Household Guard: a [rohan] Man; "Valiant." is his whole text
					put("guard2", "5_83");
					put("guard3", "5_83");
					// Spear of the Mark: hand weapon.  "Bearer must be a [rohan] Man.  If bearer is mounted, bearer is
					// defender +1."  The card that reads "mounted" here.
					put("markspear", "15_140");
					// Rider's Mount: a mount.  "Bearer must be a [rohan] Man.  At the start of each skirmish involving
					// bearer, each minion skirmishing bearer must exert."  No skirmish is fought here.
					put("mount1", "4_287");
					put("mount2", "4_287");

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, site 4, no text at all
					put("troop2", "1_177");
					put("troop3", "1_177");
					// Goblin Spear: hand weapon, strength +2.  "Bearer must be a [moria] minion."  Its other text
					// answers a kill.
					put("spear1", "1_182");
					put("spear2", "1_182");
					// Second Level: a [gondor] Fortification condition for the support area, printed site number +2.
					// "Maneuver: Exert a [gondor] Man or spot a ranger to transfer this condition from your support
					// area to a minion."
					put("level", "7_118");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACharacterBearingAnItemOfAClassIsNotOfferedASecondOfThatClass() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(pippin, merry);
		scn.AttachCardsTo(pippin, sword1);
		scn.MoveCardsToHand(sword2);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sword2));
		scn.FreepsPlayCard(sword2);

		// Every Hobbit may bear a Hobbit Sword; the one who has a hand weapon is not offered.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin));
		scn.FreepsChooseCard(merry);

		assertAttachedTo(sword2, merry);
		assertAttachedTo(sword1, pippin);
		assertEquals(1, scn.GetAttachedCards(pippin).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionBearingAnItemOfAClassIsNotOfferedASecondOfThatClass() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var spear1 = scn.GetShadowCard("spear1");
		var spear2 = scn.GetShadowCard("spear2");
		scn.MoveCardsToHand(spear2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.AttachCardsTo(troop1, spear1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The only [moria] minion bears a hand weapon already: the Spear cannot be played.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(spear2));

		scn.MoveMinionsToTable(troop2, troop3);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(spear2));
		scn.ShadowPlayCard(spear2);

		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop2, troop3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop1));
		scn.ShadowChooseCard(troop2);

		assertAttachedTo(spear2, troop2);
		assertEquals(1, scn.GetAttachedCards(troop1).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACharacterMayBearItemsOfDifferentClassesAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var sword1 = scn.GetFreepsCard("sword1");
		var salt = scn.GetFreepsCard("salt");
		scn.MoveCompanionsToTable(pippin, merry);
		scn.AttachCardsTo(pippin, sword1);
		scn.MoveCardsToHand(salt);

		scn.StartGame();

		// A hand weapon is no bar to a box.
		scn.FreepsPlayCard(salt);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, merry));
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(salt, pippin);
		assertAttachedTo(sword1, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACharacterMayBearAnyNumberOfItemsWithNoClass() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var athelas3 = scn.GetFreepsCard("athelas3");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(knight, athelas1, athelas2);
		scn.MoveCardsToHand(athelas3);

		scn.StartGame();

		scn.FreepsPlayCard(athelas3);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		scn.FreepsChooseCard(knight);

		assertAttachedTo(athelas1, knight);
		assertAttachedTo(athelas2, knight);
		assertAttachedTo(athelas3, knight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnArtifactAndAPossessionOfOneClassAreTwoOfThatClass() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var mithril = scn.GetFreepsCard("mithril");
		var orcarmor = scn.GetFreepsCard("orcarmor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(pippin, merry, aragorn);
		scn.AttachCardsTo(frodo, mithril);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToHand(orcarmor, anduril);

		scn.StartGame();

		// Aragorn's possession bars the artifact, and he is its only possible bearer.
		assertFalse(scn.FreepsPlayAvailable(anduril));

		// Frodo's artifact bars the possession.
		scn.FreepsPlayCard(orcarmor);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(orcarmor, pippin);
		assertInZone(Zone.HAND, anduril);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnItemOfTwoClassesBarsAndIsBarredByEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ordulus = scn.GetFreepsCard("ordulus");
		var guardian = scn.GetFreepsCard("guardian");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var naith1 = scn.GetFreepsCard("naith1");
		var naith2 = scn.GetFreepsCard("naith2");
		var elvensword = scn.GetFreepsCard("elvensword");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(ordulus, guardian, lorienelf);
		scn.AttachCardsTo(ordulus, naith1);
		scn.AttachCardsTo(guardian, elvensword);
		scn.MoveCardsToHand(naith2, bow);

		scn.StartGame();

		// Naith Armaments is a hand weapon and a ranged weapon.  The two valiant Elves are Ordulus, who bears one,
		// and the Guardian, whose sword is a hand weapon: neither may take a second.
		assertTrue(scn.HasKeyword(ordulus, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guardian, Keyword.VALIANT));
		assertFalse(scn.FreepsPlayAvailable(naith2));

		// A ranged weapon: not for Ordulus, whose Armaments are one; the Guardian has a hand weapon only.
		scn.FreepsPlayCard(bow);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, lorienelf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ordulus));
		scn.FreepsChooseCard(guardian);

		assertAttachedTo(bow, guardian);
		assertAttachedTo(elvensword, guardian);
		assertInZone(Zone.HAND, naith2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountMakesItsBearerMountedAndHeBearsOnlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var markspear = scn.GetFreepsCard("markspear");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard1, guard2, guard3);
		scn.AttachCardsTo(guard1, markspear);
		scn.MoveCardsToHand(mount1, mount2);

		scn.StartGame();

		// The Spear makes its bearer defender +1 only while he is mounted, and he is not.
		assertFalse(scn.HasKeyword(guard1, Keyword.DEFENDER));

		scn.FreepsPlayCard(mount1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2, guard3));
		scn.FreepsChooseCard(guard1);

		assertAttachedTo(mount1, guard1);
		assertTrue(scn.HasKeyword(guard1, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(guard1, Keyword.DEFENDER));

		// A mount is a class: he is not offered a second.
		scn.FreepsPlayCard(mount2);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, guard3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard1));
		scn.FreepsChooseCard(guard2);

		assertAttachedTo(mount2, guard2);
		assertEquals(2, scn.GetAttachedCards(guard1).size());
		// Mounted with no Spear is nothing to him.
		assertFalse(scn.HasKeyword(guard2, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PrintedStrengthVitalityAndResistanceBonusesReachTheBearerAndLeaveWithTheItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var sting = scn.GetFreepsCard("sting");
		var salt = scn.GetFreepsCard("salt");
		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(pippin, salt);
		scn.MoveCardsToHand(sting);

		scn.StartGame();
		scn.AddBurdens(1);

		// Frodo, 3 strength, 4 vitality, 10 resistance, bearing The Ruling Ring (strength +1); one burden.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(4, scn.GetVitality(frodo));
		assertEquals(9, scn.GetResistance(frodo));

		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, frodo);

		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(5, scn.GetVitality(frodo));
		assertEquals(10, scn.GetResistance(frodo));

		// Pippin, resistance 6, with the Salt's +2 and the burden's -1.
		assertEquals(7, scn.GetResistance(pippin));

		// The Salt's own ability discards it to remove the burden: Pippin loses its 2 and gets the burden's 1 back,
		// and Frodo, who bore nothing that went, gets back the burden's 1 alone.
		assertTrue(scn.FreepsActionAvailable(salt));
		scn.FreepsUseCardAction(salt);

		assertInZone(Zone.DISCARD, salt);
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(pippin));
		assertEquals(11, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASiteNumberPrintedOnABorneCardReachesTheMinionBearingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var level = scn.GetFreepsCard("level");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(level);

		var troop1 = scn.GetShadowCard("troop1");
		var spear1 = scn.GetShadowCard("spear1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.AttachCardsTo(troop1, spear1);

		// The Troop, 13 and site 4, bears a Spear printed strength +2.
		assertEquals(15, scn.GetStrength(troop1));
		assertEquals(4, scn.GetMinionSiteNumber(troop1));

		scn.SkipToPhase(Phase.MANEUVER);

		// Second Level's own ability: the Knight is the one [gondor] Man to exert, and the Troop the one minion.
		assertTrue(scn.FreepsActionAvailable(level));
		scn.FreepsUseCardAction(level);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertAttachedTo(level, troop1);
		assertEquals(6, scn.GetMinionSiteNumber(troop1));
		assertEquals(15, scn.GetStrength(troop1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
