package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_15_148_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flower", "65_148");

					// Hobbits.  The Ring-bearer is Frodo, Son of Drogo: strength 3, resistance 10.
					put("sam", "1_311");            // Sam, Son of Hamfast: strength 3, vitality 4, resistance 5; Fellowship: exert Sam to remove a burden
					put("merry", "1_302");          // Merry, Friend to Sam: strength 3, vitality 4, resistance 6; a skirmish ability used only on purpose
					put("pippin", "103_31");        // Pippin, Trollslayer: a [gondor] Hobbit, strength 3, vitality 4, resistance 6
					put("bilbo", "1_284");          // Bilbo, Retired Adventurer: Hobbit ally, strength 2, vitality 3; a fellowship ability only
					put("gaffer", "1_291");         // The Gaffer, Sam's Father: Hobbit ally, strength 2, vitality 2; Fellowship: exert to heal Frodo or Sam
					put("ted", "18_75");            // Ted Sandyman: a [men] minion who is a Hobbit, strength 3, vitality 4; maneuver and regroup abilities only
					// Not Hobbits.
					put("goldberry", "9_51");       // Goldberry: a [shire] ally of no race, strength 4; her texts need a river
					put("smeagol", "103_116");      // Smeagol, Once Like Us: a [shire] companion of no race, strength 3, resistance 6; an assignment ability only
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4, resistance 6
					put("gimli", "1_13");           // Gimli, Son of Gloin: strength 6, damage +1
					put("guard", "1_7");            // Dwarf Guard: strength 4, vitality 2

					put("sword1", "1_299");         // Hobbit Sword: hand weapon, strength +2, bearer must be a Hobbit; no other text
					put("sword2", "1_299");
					put("pipe", "1_292");           // The Gaffer's Pipe: a possession a Hobbit bears that is no hand weapon
					put("ranger", "1_112");         // Ranger's Sword: hand weapon on Aragorn, strength +2 (and damage +1)
					put("dagger", "18_69");         // Henchman's Dagger: hand weapon, strength +1, bearer must be a [men] minion
					put("bow", "11_89");            // Long Battle Bow: ranged weapon, strength +1, archer; bearer must be a [men] minion.  Its when-played text is not used: it is cheated on
					put("worker1", "15_135");       // Rohan Worker: a follower with no printed strength or resistance;
					put("worker2", "15_135");       // its texts answer a transfer or a skirmish won
					put("worker3", "15_135");
					put("worker4", "15_135");
					put("army", "18_47");           // Elendil's Army: follower, strength +2, no printed resistance.  Aid: exert a [gondor] companion
					put("follow", "1_109");         // One Whom Men Would Follow: Maneuver event; exert Aragorn, and an ally is strength +2 and takes part in skirmishes until the regroup phase

					put("party1", "1_158");         // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
					put("party2", "1_158");
					put("savage", "1_151");         // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("berserker", "3_66");       // Orthanc Berserker: strength 11, damage +1; its maneuver ability needs 5 burdens
					put("marksman1", "1_176");      // Goblin Marksman: archer and nothing else
					put("marksman2", "1_176");
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
		 * Subtype: Support Area
		 * Game Text: Each Hobbit bearing a hand weapon takes no more than 1 wound per skirmish.
		 * Each Hobbit bearing a follower is resistance +1.
		 * Each unwounded Hobbit is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flower");

		assertEquals("Little Golden Flower", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AHobbitBearingAHandWeaponTakesOneWoundInEachSkirmishAndOthersTakeBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword1");
		var pipe = scn.GetFreepsCard("pipe");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(sam, merry, aragorn);
		scn.MoveCardsToSupportArea(flower);
		// A Hobbit whose possession is no hand weapon, and a hand weapon on a Man.
		scn.AttachCardsTo(merry, pipe);
		scn.AttachCardsTo(aragorn, ranger);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry));
		scn.FreepsChooseCard(sam);
		assertAttachedTo(sword, sam);

		var party = scn.GetShadowCard("party1");
		var savage = scn.GetShadowCard("savage");
		var berserker = scn.GetShadowCard("berserker");
		scn.MoveMinionsToTable(party, savage, berserker);
		// Fierce, so that Sam fights the Raiding Party in a second skirmish of the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, party, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, party},
				new PhysicalCardImpl[]{merry, savage},
				new PhysicalCardImpl[]{aragorn, berserker});
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Merry, 3 + 1 unwounded, loses to the Savage, strength 5 and damage +1: two wounds.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(merry));

		// Aragorn, 8 + 2 for his sword, loses to the Berserker, strength 11 and damage +1: two wounds.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Sam, 3 + 2 for his sword + 1 unwounded, loses to the Raiding Party, strength 9 and damage +1: one
		// wound of the two.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		// The fierce skirmish is another skirmish: one wound more.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(sam, party);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGondorHobbitAndAHobbitAllyBearingHandWeaponsTakeOneWoundOfTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var pippin = scn.GetFreepsCard("pippin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(flower);
		scn.AttachCardsTo(pippin, sword1);
		scn.AttachCardsTo(bilbo, sword2);
		scn.MoveCardsToHand(follow);

		scn.StartGame();

		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveMinionsToTable(party1, party2);

		scn.SkipToPhase(Phase.MANEUVER);
		// Bilbo's home is site 3: One Whom Men Would Follow has him take part in skirmishes here, at +2.
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{pippin, party1},
				new PhysicalCardImpl[]{bilbo, party2});
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// Pippin, 3 + 2 for his sword + 1 unwounded, loses to a Raiding Party, strength 9 and damage +1.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pippin));

		// Bilbo, 2 + 2 for his sword + 2 for the event + 1 unwounded, loses to the other.
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.SUPPORT, bilbo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOpponentsHobbitMinionBearingAHandWeaponIsAHobbitToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(flower);

		scn.StartGame();

		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, dagger);

		scn.SkipToAssignments();
		// "Each Hobbit" names no side.  3 + 1 for his dagger + 1 unwounded.
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(5, scn.GetStrength(ted));

		scn.FreepsAssignToMinions(gimli, ted);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli, strength 6 and damage +1, wins: one wound of the two.
		assertEquals(1, scn.GetWoundsOn(ted));
		// Wounded now: 3 + 1 for his dagger.
		assertEquals(4, scn.GetStrength(ted));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARangedWeaponIsNoHandWeaponSoItsHobbitBearerTakesBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(flower);

		scn.StartGame();

		var ted = scn.GetShadowCard("ted");
		var bow = scn.GetShadowCard("bow");
		scn.MoveMinionsToTable(ted);
		scn.SkipToAssignments();
		// Put down after the archery phase, which its archer keyword would have joined.
		scn.AttachCardsTo(ted, bow);

		// The only Hobbit that can bear a weapon of another class.  3 + 1 for the bow + 1 unwounded.
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(5, scn.GetStrength(ted));

		scn.FreepsAssignToMinions(gimli, ted);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli, strength 6 and damage +1, wins: both wounds, since the bow is no hand weapon.
		assertEquals(2, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheArcheryPhaseAHobbitBearingAHandWeaponTakesEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword1");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(flower);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();

		scn.MoveMinionsToTable(scn.GetShadowCard("marksman1"), scn.GetShadowCard("marksman2"));

		// Two archers: two archery wounds for the Free Peoples player to place.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo));
		scn.FreepsChooseCard(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertAttachedTo(sword, sam);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishAHobbitBearingAHandWeaponIsGivenOneThreatWoundAndNoSecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword1");
		scn.MoveCompanionsToTable(sam, merry, guard);
		scn.MoveCardsToSupportArea(flower);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();

		var party = scn.GetShadowCard("party1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(party, savage);
		scn.AddThreats(2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, savage},
				new PhysicalCardImpl[]{sam, party});
		scn.FreepsResolveSkirmish(guard);
		assertFalse(scn.IsCharSkirmishing(sam));
		scn.PassSkirmishActions();

		// The Guard, strength 4 and vitality 2, loses to the Savage, strength 5 and damage +1, and is killed: the
		// two threats come off as two wounds for the Free Peoples player to place, in the Guard's skirmish.
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry, frodo));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		// Sam has taken his one wound of this skirmish, though he is not fighting in it.  Merry, a Hobbit with
		// no hand weapon, may still be given one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));

		// Sam's own skirmish is the next: 3 + 2 for his sword, wounded, he loses to the Raiding Party, strength
		// 9 and damage +1, and takes one wound of the two.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedToTheSupportAreaAndEachHobbitBearingAFollowerIsResistancePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, merry, pippin, smeagol);
		// A follower on the Ring-bearer, on a [gondor] Hobbit and on a [shire] companion who is no Hobbit; two
		// on Sam.  Merry bears a possession and no follower.
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("worker1"));
		scn.AttachCardsTo(sam, scn.GetFreepsCard("worker2"), scn.GetFreepsCard("worker3"));
		scn.AttachCardsTo(pippin, scn.GetFreepsCard("worker4"));
		scn.AttachCardsTo(smeagol, scn.GetFreepsCard("army"));
		scn.AttachCardsTo(merry, scn.GetFreepsCard("sword1"));
		scn.MoveCardsToHand(flower);

		scn.StartGame();

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(pippin));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(smeagol));
		assertTrue(scn.FreepsPlayAvailable(flower));

		scn.FreepsPlayCard(flower);
		assertInZone(Zone.SUPPORT, flower);
		assertNull(flower.getAttachedTo());

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
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		// Aid: exert a [gondor] companion to transfer Elendil's Army to a companion.
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
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(gaffer, goldberry);
		scn.AddWoundsToChar(merry, 1);
		scn.MoveCardsToHand(flower);

		scn.StartGame();

		// The Ruling Ring makes its bearer strength +1.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(2, scn.GetStrength(gaffer));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(goldberry));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(flower);
		assertInZone(Zone.SUPPORT, flower);
		// Unwounded Hobbits: the Ring-bearer, a [shire] companion, a [gondor] companion and an ally.
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(gaffer));
		// A wounded Hobbit; an unwounded [shire] ally and an unwounded [shire] companion who are no Hobbits; an
		// unwounded Man.
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
