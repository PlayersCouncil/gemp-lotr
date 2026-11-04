package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A hindered card cannot be spotted, chosen or counted by what it is.
 *
 * Comprehensive Rules 5.0, "hinder": "Other non-game-text card traits such as title, culture, card type, all
 * flavors of subtype (Race, Item Class, etc) also do not apply on hindered cards.  This means that hindered cards
 * cannot be spotted or targeted by their title, culture, card type, or race/item class by any game effect.  They
 * also cannot be counted by effects which count how many of certain cards you can spot."  Its examples: "If Gimli
 * is hindered, you cannot 'spot Gimli' or use Gimli when you 'spot a companion', 'spot a [Dwarven] card', or 'spot
 * a Dwarf'", and "If there are 6 companions on the table, but 1 of them is hindered, Ulaire Enquea, Lieutenant of
 * Morgul can only spot 5 companions and so cannot use his ability."  The same entry: "The entire game text of
 * hindered cards does not apply.  This includes triggers, special abilities, modifiers, keywords, and other
 * effects", so a keyword is not there to be spotted either, nor to be found missing.
 *
 * In every test here the hindering is only how the board is laid, and is done by the rig; the reading is a real
 * card failing to find the hindered one, beside others of its kind that it does find.
 */
public class HinderedCardCannotBeSpottedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4, ranger, Gandalf signet
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound, vitality 4, Aragorn signet
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, unbound, strength 3, Frodo signet
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6
					put("boromir", "3_122");    // Boromir, Defender of Minas Tirith: [gondor] Man, strength 7
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf, Frodo signet; an archer, kept out of archery phases
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability never used

					// Bring Down the Wall: [men] Skirmish event, cost 1.  "Spot a companion to make a [men]
					// minion strength +1 for each wound on that companion."
					put("wall1", "13_82");
					put("wall2", "13_82");
					// Cruel Dunlending: [men] Man, strength 8; its ability needs it out of a skirmish.
					put("dunlending", "13_85");

					// Wielder of the Flame: [gandalf] Maneuver event, cost 1.  "Spot Gandalf to make a
					// companion defender +1 until the regroup phase.  Any Shadow player may remove (3) to
					// prevent this."
					put("flame1", "2_28");
					put("flame2", "2_28");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase and a skirmish

					// Ulaire Enquea, Lieutenant of Morgul: "Maneuver: Spot 6 companions (or 5 burdens) and
					// exert Ulaire Enquea to wound a companion (except the Ring-bearer)."
					put("enquea", "1_231");

					// Light the Beacons: [gandalf] Maneuver event, cost 3.  "Exert Gandalf three times (or
					// twice if you have initiative) to make all unbound companions of one culture (except
					// [gandalf]) strength +3 until the regroup phase."
					put("beacons", "7_43");

					// So Fair, So Desperate: [shire] Skirmish event, cost 1.  "If the Shadow has initiative,
					// add a threat to make an unbound Hobbit strength +1 for each [shire] card you spot."
					put("fair1", "8_111");
					put("fair2", "8_111");

					// Gandalf, Friend of the Shirefolk: strength 6.  "Gandalf is strength +1 for each of these
					// races you can spot in the fellowship: Hobbit, Dwarf, Elf, and Man."
					put("shirefolk", "1_72");
					put("lorienelf", "1_53");   // Lorien Elf: "To play, spot an Elf."

					// Pathfinder: "Fellowship or Regroup: Spot a ranger to play the fellowship's next site."
					put("pathfinder", "1_110");

					// Straining Towards Us: [shire] Regroup event, cost 0.  "Exert Frodo to remove (1) for
					// each Frodo signet you spot."
					put("straining1", "8_114");
					put("straining2", "8_114");

					// Remember Your Old Strength: [gandalf] Maneuver event, cost 2.  "Exert Gandalf to make
					// Theoden and each valiant companion strength +1 until the regroup phase (or strength +2
					// if every unbound companion is valiant)."
					put("remember1", "102_14");
					put("remember2", "102_14");
					// Gandalf, Lathspell: strength 7, vitality 4.  "Each mounted companion gains valiant", so
					// on Shadowfax he is valiant himself (no Gandalf is in print).  His other text answers a
					// minion's special ability, and no minion beside him has one.
					put("lathspell", "102_13");
					put("shadowfax", "8_21");   // Shadowfax, Greatheart: a mount for Gandalf that changes no number
					put("tallproud", "8_92");   // Theoden, Tall and Proud: valiant, strength 7
					put("guard1", "5_83");      // Household Guard: [rohan] Man, strength 6; "Valiant" is its whole text
					put("guard2", "5_83");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: [shire] Hobbit with the Frodo signet; a fellowship ability never used
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHinderedCompanionIsNotOfferedWhereACardSpotsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var wall1 = scn.GetShadowCard("wall1");
		var wall2 = scn.GetShadowCard("wall2");
		var dunlending = scn.GetShadowCard("dunlending");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(wall1, wall2);

		scn.StartGame();
		// A different number of wounds on each, so that the minion's strength says who was spotted.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(dunlending);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, dunlending);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();

		// With nobody hindered each of the three companions may be spotted, in the skirmish or out of it.
		assertEquals(8, scn.GetStrength(dunlending));
		scn.ShadowPlayCard(wall1);
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn));
		scn.ShadowChooseCard(aragorn);
		// 8 + 3 for the wounds on Aragorn
		assertEquals(11, scn.GetStrength(dunlending));

		scn.HinderCard(aragorn);
		scn.FreepsPass();

		// Hindered, Aragorn is no companion to spot.  The other two still are.
		assertTrue(scn.IsHindered(aragorn));
		scn.ShadowPlayCard(wall2);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(sam);
		// 11 + 2 for the wounds on Sam
		assertEquals(13, scn.GetStrength(dunlending));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AHinderedCompanionIsNotOfferedWhereACardChoosesACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var flame1 = scn.GetFreepsCard("flame1");
		var flame2 = scn.GetFreepsCard("flame2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(flame1, flame2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// With nobody hindered each of the three companions may be chosen.
		assertFalse(scn.HasKeyword(gandalf, Keyword.DEFENDER));
		scn.FreepsPlayCard(flame1);
		scn.ShadowChooseNo();
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		scn.FreepsChooseCard(gandalf);
		assertTrue(scn.HasKeyword(gandalf, Keyword.DEFENDER));

		scn.ShadowPass();
		scn.HinderCard(aragorn);

		// Hindered, Aragorn is no companion to choose.  The other two still are.
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.HasKeyword(frodo, Keyword.DEFENDER));
		scn.FreepsPlayCard(flame2);
		scn.ShadowChooseNo();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.HasKeyword(frodo, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHinderedCompanionIsNotSpottedByItsTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var flame1 = scn.GetFreepsCard("flame1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(flame1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// "Spot Gandalf".
		assertTrue(scn.FreepsPlayAvailable(flame1));

		scn.HinderCard(gandalf);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsPlayAvailable(flame1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Hindering another companion takes nothing from the spot.
		scn.RestoreCard(gandalf);
		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(flame1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHinderedCompanionIsNotCountedAmongTheCompanionsACardSpots() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var enquea = scn.GetShadowCard("enquea");
		// With the Ring-bearer, six companions.
		scn.MoveCompanionsToTable(aragorn, gandalf, sam, merry, gimli);

		scn.StartGame();
		// One burden short of the five that would do in place of the companions.
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(enquea));

		// The rules' own example: six companions on the table and one of them hindered are five to spot.
		scn.HinderCard(aragorn);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(enquea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.RestoreCard(aragorn);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(enquea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHinderedCardIsNotFoundByItsCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var beacons = scn.GetFreepsCard("beacons");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, gandalf);
		scn.MoveCardsToHand(beacons);

		scn.StartGame();
		scn.HinderCard(aragorn);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsHindered(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(gimli));

		// One card in hand: the Shadow has initiative, and Gandalf exerts three times.
		scn.FreepsPlayCard(beacons);
		assertEquals(3, scn.GetWoundsOn(gandalf));

		// The culture is named by choosing a companion of it.  Boromir, [gondor] as Aragorn is, may be chosen,
		// and the hindered Aragorn may not.  (Gandalf is [gandalf], and the Ring-bearer is not unbound.)
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gandalf, frodo));
		scn.FreepsChooseCard(boromir);
		// 7 + 3
		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Aragorn was not one of "all unbound [gondor] companions" when the event was played: restored, he is
		// at his printed 8.
		scn.RestoreCard(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(boromir));
	}

	@Test
	public void AHinderedCardIsNotCountedAmongTheCardsOfItsCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var fair1 = scn.GetFreepsCard("fair1");
		var fair2 = scn.GetFreepsCard("fair2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, sam, merry);
		scn.MoveCardsToHand(fair1, fair2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);

		// The event names a culture and no kind of card.  The Ring-bearer, Sam and Merry are the [shire] cards
		// in play; Aragorn is [gondor].  Merry is the only unbound Hobbit, and takes the bonus unasked.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(fair1);
		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");
		// 3 + 3
		assertEquals(6, scn.GetStrength(merry));

		scn.ShadowPass();
		scn.HinderCard(sam);

		// Hindered, Sam is no [shire] card to spot: there are two.
		assertTrue(scn.IsHindered(sam));
		scn.FreepsPlayCard(fair2);
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		// 6 + 2
		assertEquals(8, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AHinderedCompanionIsNotSpottedOrCountedByItsRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shirefolk = scn.GetFreepsCard("shirefolk");
		var legolas = scn.GetFreepsCard("legolas");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		// Legolas is the only Elf in play.
		scn.MoveCompanionsToTable(shirefolk, legolas);
		scn.MoveCardsToHand(lorienelf);

		scn.StartGame();

		// 6 + 1 for the Hobbit (the Ring-bearer) + 1 for the Elf
		assertEquals(8, scn.GetStrength(shirefolk));
		assertTrue(scn.FreepsPlayAvailable(lorienelf));

		// Hindered, Legolas is no Elf: not for the count, and not for the spot.
		scn.HinderCard(legolas);
		scn.FreepsRefreshDecision();
		assertEquals(7, scn.GetStrength(shirefolk));
		assertFalse(scn.FreepsPlayAvailable(lorienelf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RestoreCard(legolas);
		scn.FreepsRefreshDecision();
		assertEquals(8, scn.GetStrength(shirefolk));
		assertTrue(scn.FreepsPlayAvailable(lorienelf));

		scn.FreepsPlayCard(lorienelf);
		assertInZone(Zone.FREE_CHARACTERS, lorienelf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedCompanionsKeywordIsNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		// Aragorn is the only ranger in play.
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(pathfinder);

		scn.StartGame();

		assertTrue(scn.HasKeyword(aragorn, Keyword.RANGER));
		assertTrue(scn.FreepsPlayAvailable(pathfinder));

		// A keyword is game text, and a hindered card's game text does not apply: there is no ranger to spot.
		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsPlayAvailable(pathfinder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Hindering another companion, who is no ranger, takes nothing from the spot.
		scn.RestoreCard(aragorn);
		scn.HinderCard(gimli);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(pathfinder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedCompanionsSignetIsNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var straining1 = scn.GetFreepsCard("straining1");
		var straining2 = scn.GetFreepsCard("straining2");
		// The Ring-bearer, Merry and Legolas have the Frodo signet.  Aragorn has Gandalf's.
		scn.MoveCompanionsToTable(merry, legolas, aragorn);
		scn.MoveCardsToHand(straining1, straining2);

		scn.StartGame();
		// No minion is in play, so the game goes from the move straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		// 4 companions + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPlayCard(straining1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");
		assertEquals(3, scn.GetTwilight());

		scn.ShadowPass();
		scn.HinderCard(merry);

		// Hindered, Merry has no signet to spot: there are two.
		assertTrue(scn.IsHindered(merry));
		scn.FreepsPlayCard(straining2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AHinderedCompanionNeitherHasNorLacksAKeyword() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lathspell = scn.GetFreepsCard("lathspell");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var tallproud = scn.GetFreepsCard("tallproud");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var remember1 = scn.GetFreepsCard("remember1");
		var remember2 = scn.GetFreepsCard("remember2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(lathspell, tallproud, guard1, guard2, sam, aragorn);
		scn.AttachCardsTo(lathspell, shadowfax);
		scn.MoveCardsToHand(remember1, remember2);

		scn.StartGame();
		// One of the two Household Guards is hindered throughout.
		scn.HinderCard(guard2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Gandalf (by his mount), Theoden and the Guard are valiant.  Aragorn is unbound and is not.  (Nor are
		// the Ring-bearer and Sam, who are Ring-bound and so no part of "every unbound companion".)
		assertTrue(scn.HasKeyword(lathspell, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(tallproud, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(guard1, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertEquals(7, scn.GetStrength(lathspell));
		assertEquals(7, scn.GetStrength(tallproud));
		assertEquals(6, scn.GetStrength(guard1));

		// Not every unbound companion is valiant: +1.
		scn.FreepsPlayCard(remember1);
		assertEquals(1, scn.GetWoundsOn(lathspell));
		assertEquals(8, scn.GetStrength(lathspell));
		assertEquals(8, scn.GetStrength(tallproud));
		assertEquals(7, scn.GetStrength(guard1));

		scn.ShadowPass();
		scn.HinderCard(aragorn);

		// Hindered, Aragorn is no companion to be found without valiant: every unbound companion there is to
		// spot is valiant, and the bonus is +2.
		scn.FreepsPlayCard(remember2);
		assertEquals(2, scn.GetWoundsOn(lathspell));
		// 8 + 2, 8 + 2, 7 + 2
		assertEquals(10, scn.GetStrength(lathspell));
		assertEquals(10, scn.GetStrength(tallproud));
		assertEquals(9, scn.GetStrength(guard1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Nor was the hindered Guard a valiant companion for either event to strengthen: restored, he is at
		// his printed 6, as Aragorn is at his 8.
		scn.RestoreCard(guard2, aragorn);
		assertTrue(scn.HasKeyword(guard2, Keyword.VALIANT));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(8, scn.GetStrength(aragorn));
	}
}
