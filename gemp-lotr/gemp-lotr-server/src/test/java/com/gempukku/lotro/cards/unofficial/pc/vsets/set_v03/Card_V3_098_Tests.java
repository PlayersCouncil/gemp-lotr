package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_098_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "103_98");
					put("assassin2", "103_98");
					put("soldier", "1_271");      // Orc Soldier: a [sauron] Orc, strength 7, vitality 2; a skirmish ability that is only ever used on purpose
					put("troop", "1_177");        // Goblin Patrol Troop: an Orc with no text, strength 13
					put("armory", "1_173");       // Goblin Armory: a Shadow condition in the support area
					put("scimitar", "1_180");     // Goblin Scimitar: a Shadow hand weapon, strength +2, cheated onto a minion
					put("breath", "1_207");       // Black Breath: a Shadow condition, cheated onto a companion; its bearer cannot heal

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, no skirmish ability
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6; damage +1
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: an ally, strength 8, home site 3
					put("sword", "1_112");        // Ranger's Sword: a hand weapon, a possession
					put("bow", "1_90");           // Aragorn's Bow: a ranged weapon, a possession; its bearer is an archer
					put("anduril", "7_79");       // Anduril, Flame of the West: a hand weapon, an artifact
					put("alliance", "1_49");      // The Last Alliance of Elves and Men: a Free Peoples condition a [gondor] Man bears
					put("athelas", "1_94");       // Athelas: a possession that is no weapon
					put("axe", "1_9");            // Dwarven Axe: a hand weapon, strength +2
					put("axe2", "1_9");
					put("stairs", "1_24");        // Stairs of Khazad-dum: a Free Peoples condition in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RedEyeAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Red Eye Assassin
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each hindered card you can spot.
		 * 	Skirmish: Exert this minion to hinder a weapon or condition borne by a companion it is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("assassin");

		assertEquals("Red Eye Assassin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedForFiveAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToHand(assassin);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(assassin));

		scn.ShadowPlayCard(assassin);

		// 5 + 2 roaming (site 4, at site 2)
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, assassin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachHinderedCardOfEitherSideAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");
		var breath = scn.GetShadowCard("breath");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var athelas = scn.GetFreepsCard("athelas");
		var alliance = scn.GetFreepsCard("alliance");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, athelas, alliance, breath);
		scn.MoveCardsToSupportArea(armory, stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, assassin2, soldier, troop);

		// Nothing is hindered.
		assertEquals(6, scn.GetStrength(assassin));
		assertEquals(6, scn.GetStrength(assassin2));
		assertEquals(13, scn.GetStrength(troop));

		// Two Free Peoples cards: a companion, and a possession another companion bears.
		scn.HinderCard(gimli, athelas);
		assertEquals(8, scn.GetStrength(assassin));

		// The One Ring, which has no side.
		scn.HinderCard(scn.GetRing());
		assertEquals(9, scn.GetStrength(assassin));

		// Three Shadow cards: a condition in the support area, a minion, and a condition a companion bears.
		scn.HinderCard(armory, soldier, breath);
		assertEquals(12, scn.GetStrength(assassin));
		// Each copy counts them once for itself, and another Orc gets nothing.
		assertEquals(12, scn.GetStrength(assassin2));
		assertEquals(13, scn.GetStrength(troop));

		// The other copy, hindered, is one more card for this one.
		scn.HinderCard(assassin2);
		assertEquals(13, scn.GetStrength(assassin));

		// Restored, a card is counted no longer.
		scn.RestoreCard(gimli, athelas);
		assertEquals(11, scn.GetStrength(assassin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsItToHinderAWeaponOrConditionBorneByTheCompanionItFights() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		var breath = scn.GetShadowCard("breath");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var anduril = scn.GetFreepsCard("anduril");
		var alliance = scn.GetFreepsCard("alliance");
		var athelas = scn.GetFreepsCard("athelas");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, soldier);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Everything is put on after the archery phase, the Bow making its bearer an archer.
		scn.AttachCardsTo(aragorn, sword, bow, anduril, alliance, breath, athelas, axe2);
		scn.HinderCard(axe2);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(assassin, scimitar);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, assassin}, new PhysicalCardImpl[]{gimli, soldier});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(assassin));
		// 6 + 2 for the Scimitar + 1 for the one card hindered so far
		assertEquals(9, scn.GetStrength(assassin));
		assertTrue(scn.ShadowActionAvailable(assassin));

		scn.ShadowUseCardAction(assassin);

		assertEquals(1, scn.GetWoundsOn(assassin));
		// On Aragorn: two possessions and an artifact that are weapons, a Free Peoples condition and a Shadow one.
		// Not offered: his possession that is no weapon, his weapon that is hindered already, the weapon of a
		// companion waiting to fight another minion, the weapon the Assassin bears itself, a condition in the
		// support area, and Aragorn.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, bow, anduril, alliance, breath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(athelas, axe2, axe, scimitar, stairs, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(bow);

		assertTrue(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(alliance));
		assertFalse(scn.IsHindered(breath));
		// The card it has just hindered is one more for it to count.
		assertEquals(10, scn.GetStrength(assassin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(breath);

		assertEquals(2, scn.GetWoundsOn(assassin));
		assertTrue(scn.IsHindered(breath));
		assertEquals(11, scn.GetStrength(assassin));

		// Vitality 3 with 2 wounds: it cannot exert, though three cards are still there to hinder.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin));
	}

	@Test
	public void WaitingForItsOwnSkirmishItMayExertButHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, assassin2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, assassin2}, new PhysicalCardImpl[]{gimli, assassin});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Aragorn is fighting the other copy; this Assassin is assigned to Gimli and skirmishing nobody yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(assassin2));
		assertTrue(scn.ShadowActionAvailable(assassin));

		scn.ShadowUseCardAction(assassin);

		// The copy that was used exerts, not the one that is fighting, and what that one's companion bears is
		// not this one's to hinder.
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(assassin2));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(axe));
		assertEquals(6, scn.GetStrength(assassin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8 + 2, damage +1) beats the other copy (6, vitality 3), which lives. In its own skirmish
		// the Assassin hinders the one card there is: Gimli's Axe, and not the Sword.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);

		assertEquals(2, scn.GetWoundsOn(assassin));
		assertTrue(scn.IsHindered(axe));
		assertFalse(scn.IsHindered(sword));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FightingAnAllyItHindersNothingTheAllyBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var elrond = scn.GetFreepsCard("elrond");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin);
		scn.AttachCardsTo(elrond, sword);

		// The fellowship moves to site 3, Elrond's home, where he may fight.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, assassin);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(assassin));

		scn.ShadowUseCardAction(assassin);

		assertEquals(1, scn.GetWoundsOn(assassin));
		assertFalse(scn.IsHindered(sword));
		assertEquals(6, scn.GetStrength(assassin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
