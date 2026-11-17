package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("peril", "6_105");
					// The cost's candidates.
					// Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("soldier", "1_271");
					// Orc Chieftain: [sauron] Orc, vitality 2; answers a weapon played on him only
					put("chieftain", "1_266");
					// Corpse Lights: [sauron] Wraith, no Orc, vitality 2; a skirmish ability only
					put("lights", "6_99");
					// Morgul Predator: [wraith] Orc, vitality 3; a skirmish ability only
					put("predator", "7_196");
					// The minions to play.
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, cost 4, site 3; his text is about [wraith] conditions borne
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: [raider] Nazgûl, cost 6, site 3; his texts read burdens only
					put("khamul", "103_61");
					// Troll of Udûn: [sauron] Troll, cost 10, vitality 5, site 6; its own -4 only while a Troll can be
					// spotted (none is); a skirmish ability used only on purpose
					put("udun", "6_106");
					// Morgul Hunter: [sauron] Orc, cost 5, site 6; a strength bonus only
					put("hunter", "1_256");
					// Morgul Hound: [wraith] Orc, no Nazgûl, cost 2; stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("troop", "1_177");
					// Orc Banner: [sauron] condition, cost 1; a trigger on a skirmish lost only
					put("banner", "1_263");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNerteaScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("peril", "6_105");
					put("soldier", "1_271");
					put("udun", "6_106");
					// Uruk Axe: [sauron] possession, cost 1, bearer must be a [sauron] minion
					put("axe", "10_102");
					// Úlairë Nertëa, Sanctified for Cruelty: "Each time you play a minion, you may exert it to play a
					// possession on it from your discard pile"
					put("nertea", "103_77");
					// Isengard Informant: "Regroup: Exert this minion to play an [orc] Troll from your discard pile."
					put("informant", "13_110");
					// Beastly Olog-hai: [orc] Troll, cost 6, site 5; to play, spot an [orc] minion
					put("olog", "11_108");
					// Orc Spear: [orc] possession, cost 1, bearer must be an [orc] minion
					put("spear", "12_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PerilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Peril
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Regroup:</b> Exert a [sauron] Orc to play a Nazgûl or [sauron] minion. Its twilight cost is -4 and it comes into play exhausted.
		*/

		var scn = GetScenario();

		var peril = scn.GetFreepsCard("peril");

		assertEquals("Peril", peril.getBlueprint().getTitle());
		assertNull(peril.getBlueprint().getSubtitle());
		assertFalse(peril.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, peril.getBlueprint().getSide());
		assertEquals(Culture.SAURON, peril.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, peril.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(peril, Keyword.SUPPORT_AREA));
		assertEquals(1, peril.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsASauronOrcToPlayANazgulOrSauronMinionFromHandExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var lights = scn.GetShadowCard("lights");
		var predator = scn.GetShadowCard("predator");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var udun = scn.GetShadowCard("udun");
		var hunter = scn.GetShadowCard("hunter");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(peril);
		scn.MoveCardsToHand(lemenya, khamul, udun, hunter, hound, troop, banner);
		assertInHand(lemenya, khamul, udun, hunter, hound, troop, banner);
		assertInZone(Zone.SUPPORT, peril);

		scn.StartGame();
		// Laid in the Shadow phase's place, they stand in the regroup phase after an assignment phase with nobody assigned.
		scn.MoveMinionsToTable(soldier, chieftain, lights, predator);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// Enough for the Goblin Patrol Troop at its full cost, 6 + 2 roaming at site 2.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(predator));
		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);

		// [sauron] Orcs of two titles; not a [sauron] minion who is no Orc, nor a [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lights, predator));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		// Nazgûl of two cultures, a [sauron] Troll and a [sauron] Orc; not a [wraith] Orc or a [moria] Orc the pool
		// could pay for, nor a [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul, udun, hunter));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, troop, banner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(udun);

		assertInZone(Zone.SHADOW_CHARACTERS, udun);
		// Vitality 5: exhausted is 4 wounds.
		assertEquals(4, scn.GetWoundsOn(udun));
		assertTrue(scn.IsExhausted(udun));
		// 10 - (10 - 4 + 2 roaming)
		assertEquals(2, scn.GetTwilight());
		// Only the minion played comes in exhausted: the others are as they were, and the cards not chosen stay in hand.
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(predator));
		assertInHand(lemenya, khamul, hunter, hound, troop, banner);
		assertInZone(Zone.SUPPORT, peril);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheMinionsTwilightCostIsFourLessSoTheTrollOfUdunCostsSixAndItsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var soldier = scn.GetShadowCard("soldier");
		var udun = scn.GetShadowCard("udun");
		scn.MoveCardsToSupportArea(peril);
		scn.MoveCardsToHand(udun);
		assertInHand(udun);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// The Troll costs 10 - 4 = 6, and 2 more roaming at site 2: 8.  One short:
		scn.SetTwilight(7);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(peril));
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);

		assertInZone(Zone.SHADOW_CHARACTERS, udun);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(4, scn.GetWoundsOn(udun));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoSauronOrcAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var soldier = scn.GetShadowCard("soldier");
		var lights = scn.GetShadowCard("lights");
		var predator = scn.GetShadowCard("predator");
		var udun = scn.GetShadowCard("udun");
		scn.MoveCardsToSupportArea(peril);
		scn.MoveCardsToHand(udun);
		assertInHand(udun);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, lights, predator);
		// Orc Soldier, vitality 2, with a wound: exhausted.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(predator));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The [sauron] Wraith and the [wraith] Orc could exert, and the Troll could be paid for: only no [sauron] Orc can pay.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(peril));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInHand(udun);
	}

	@Test
	public void NotOfferedUntilANazgulOrSauronMinionIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var soldier = scn.GetShadowCard("soldier");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(peril);
		scn.MoveCardsToHand(hound, troop, banner);
		assertInHand(hound, troop, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The play is part of the cost: a [wraith] Orc, a [moria] Orc and a [sauron] condition give it nothing to play.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(peril));
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.MoveCardsToHand(lemenya);
		scn.ShadowRefreshDecision();
		assertInHand(lemenya);
		assertTrue(scn.ShadowActionAvailable(peril));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheMinionPlayedIsExhaustedAsItComesIntoPlaySoNerteaCannotExertIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNerteaScenario();

		var peril = scn.GetShadowCard("peril");
		var soldier = scn.GetShadowCard("soldier");
		var udun = scn.GetShadowCard("udun");
		var axe = scn.GetShadowCard("axe");
		var nertea = scn.GetShadowCard("nertea");
		var informant = scn.GetShadowCard("informant");
		var olog = scn.GetShadowCard("olog");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(peril);
		scn.MoveCardsToHand(udun);
		scn.MoveCardsToDiscard(olog, spear, axe);
		assertInHand(udun);
		assertInDiscard(olog, spear, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, nertea, informant);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Nertëa is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The control: an [orc] Troll played from the discard pile by Isengard Informant comes into play fresh, and
		// Nertëa offers to exert it to play the Orc Spear on it.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowUseCardAction(informant);
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertEquals(0, scn.GetWoundsOn(olog));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(spear, axe);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);

		// "Comes into play exhausted": by the time its play is answered the Troll cannot exert, so Nertëa has
		// nothing to offer, though the Uruk Axe could go on a [sauron] minion.
		assertInZone(Zone.SHADOW_CHARACTERS, udun);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(4, scn.GetWoundsOn(udun));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInDiscard(axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
