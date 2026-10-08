package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_154_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "54_154");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound companion; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound companion; a skirmish ability never used here
					put("legolas", "1_50");     // Legolas, Greenleaf: unbound companion; an archery ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound companion; a skirmish ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai minion, Damage +1 and nothing else
					put("maggot", "1_288");     // Farmer Maggot: [shire] ally; a fellowship ability never used here

					// Weapons.
					put("sword", "1_112");      // Ranger's Sword: hand weapon, bearer must be Aragorn
					put("bow", "1_41");         // Elven Bow: ranged weapon, bearer must be an Elf
					put("axe", "1_14");         // Gimli's Battle Axe: hand weapon, bearer must be Gimli
					put("gondorsword", "4_134");// Sword of Gondor: hand weapon, bearer must be a [gondor] Man
					put("hobbitsword", "1_299");// Hobbit Sword: hand weapon, bearer must be a Hobbit
					put("broadsword", "4_142"); // Broad-bladed Sword: Shadow hand weapon, bearer must be an Uruk-hai

					// Free Peoples cards that are not weapons.
					put("armor", "1_8");        // Dwarven Armor: armor, about being overwhelmed only
					put("armor2", "1_8");
					put("coat", "1_101");       // Coat of Mail: armor, about being overwhelmed only
					put("coat2", "1_101");
					put("shield", "1_107");     // Great Shield: a shield; minion archery total -1
					put("shield2", "1_107");
					put("saga", "1_114");       // The Saga of Elendil: condition on a [gondor] companion; a skirmish ability
					put("endurance", "2_4");    // Endurance of Dwarves: condition on a Dwarf; a skirmish ability

					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion
					put("poison2", "8_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Grima, Wormtongue
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Maneuver: Exert Grima to hinder a weapon borne by an unbound companion (or all Free Peoples
		 * cards borne by that companion if they are bearing 3 or more cards).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grima");

		assertEquals("Grima", card.getBlueprint().getTitle());
		assertEquals("Wormtongue", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedForTwoPlusRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(grima));

		scn.ShadowPlayCard(grima);
		// 2 to play + 2 roaming (site 3 minion at site 2)
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, grima);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsGrimaToHinderAWeaponBorneByAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var warrior = scn.GetShadowCard("warrior");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var broadsword = scn.GetShadowCard("broadsword");

		scn.MoveCompanionsToTable(aragorn, legolas, gimli, sam);
		// Aragorn bears two cards, a hand weapon and a condition: one short of three.
		scn.AttachCardsTo(aragorn, sword, saga);
		// A ranged weapon.
		scn.AttachCardsTo(legolas, bow);
		// Armor is not a weapon.
		scn.AttachCardsTo(gimli, armor);
		// Sam is Ring-bound.
		scn.AttachCardsTo(sam, hobbitsword);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, warrior);
		// A weapon borne by a minion.
		scn.AttachCardsTo(warrior, broadsword);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saga, armor, hobbitsword, broadsword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertTrue(scn.IsHindered(sword));
		assertAttachedTo(sword, aragorn);
		assertFalse(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(hobbitsword));
		assertFalse(scn.IsHindered(broadsword));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenGrimaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(grima, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grima));
		assertFalse(scn.IsHindered(sword));
	}

	@Test
	public void AWeaponBorneByAHinderedCompanionCannotBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var gondorsword = scn.GetFreepsCard("gondorsword");

		scn.MoveCompanionsToTable(aragorn, legolas, boromir);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(legolas, bow);
		scn.AttachCardsTo(boromir, gondorsword);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.HinderCard(boromir);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(gondorsword));
		assertAttachedTo(gondorsword, boromir);

		scn.ShadowUseCardAction(grima);
		// A card borne by a hindered companion is not borne by a companion (Q-0108).
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gondorsword));
		scn.ShadowChooseCard(sword);

		assertTrue(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(gondorsword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AWeaponOnACompanionBearingThreeCardsHindersEveryFreePeoplesCardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var poison = scn.GetShadowCard("poison");
		var saga = scn.GetFreepsCard("saga");

		scn.MoveCompanionsToTable(aragorn, gimli);
		// Exactly three cards, one of them a Shadow card.  The axe is the only weapon on the table, so it is
		// chosen without asking.
		scn.AttachCardsTo(gimli, axe, armor, poison);
		scn.AttachCardsTo(aragorn, saga);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(grima));
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));

		assertTrue(scn.IsHindered(axe));
		assertTrue(scn.IsHindered(armor));
		assertAttachedTo(armor, gimli);
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(saga));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHinderedCardCountsTowardTheThreeCardsAWeaponBearerBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var endurance = scn.GetFreepsCard("endurance");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe, armor, endurance);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.HinderCard(endurance);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(endurance));
		assertFalse(scn.IsHindered(armor));
		scn.ShadowUseCardAction(grima);

		// With the hindered condition counted Gimli bears three, so the armor is hindered with the axe.
		assertTrue(scn.IsHindered(axe));
		assertTrue(scn.IsHindered(armor));
		assertTrue(scn.IsHindered(endurance));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHinderedCardCountsTowardsTheThreeCardsACompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var coat1 = scn.GetFreepsCard("coat");
		var coat2 = scn.GetFreepsCard("coat2");
		var shield1 = scn.GetFreepsCard("shield");
		var shield2 = scn.GetFreepsCard("shield2");
		var saga = scn.GetFreepsCard("saga");
		var armor1 = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		// Aragorn bears three cards of which one will be hindered, and no weapon; Gimli bears three (the second
		// armor is put there by the rig); Boromir bears two.
		scn.AttachCardsTo(aragorn, coat1, saga, shield1);
		scn.AttachCardsTo(gimli, armor1, armor2, axe);
		scn.AttachCardsTo(boromir, coat2, shield2);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.HinderCard(shield1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(shield1));
		assertAttachedTo(shield1, aragorn);
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		// Bearing 3 or more cards: Aragorn with his hindered third, and Gimli.  Not Boromir with two.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// All the Free Peoples cards Aragorn bears are hindered, and nobody else's.
		assertTrue(scn.IsHindered(coat1));
		assertTrue(scn.IsHindered(saga));
		assertTrue(scn.IsHindered(shield1));
		assertFalse(scn.IsHindered(armor1));
		assertFalse(scn.IsHindered(armor2));
		assertFalse(scn.IsHindered(axe));
		assertFalse(scn.IsHindered(coat2));
		assertFalse(scn.IsHindered(shield2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ARingBoundCompanionOrAnAllyBearingThreeCardsIsNotReachedAndAWeaponIsHinderedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var armor = scn.GetFreepsCard("armor");
		var coat = scn.GetFreepsCard("coat");
		var shield = scn.GetFreepsCard("shield");
		var armor2 = scn.GetFreepsCard("armor2");
		var coat2 = scn.GetFreepsCard("coat2");

		scn.MoveCompanionsToTable(aragorn, legolas, sam);
		scn.MoveAlliesToTable(maggot);
		// Two unbound companions bear one weapon each and no one else.
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(legolas, bow);
		// Sam is Ring-bound and bears three cards, one of them a weapon; Maggot is an ally and bears three.
		scn.AttachCardsTo(sam, hobbitsword, armor, coat);
		scn.AttachCardsTo(maggot, shield, armor2, coat2);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(grima));
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		// Nobody who can be reached bears three cards, so a weapon is what is chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hobbitsword, sam, maggot, armor, coat, shield, armor2, coat2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bow);

		assertTrue(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(hobbitsword));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(shield));
		assertFalse(scn.IsHindered(armor2));
		assertFalse(scn.IsHindered(coat2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OnlyAnUnboundCompanionBearingThreeCardsIsReachedBesideARingBoundCompanionAndAnAllyBearingThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var poison = scn.GetShadowCard("poison");
		var coat = scn.GetFreepsCard("coat");
		var shield = scn.GetFreepsCard("shield");
		var saga = scn.GetFreepsCard("saga");
		var armor2 = scn.GetFreepsCard("armor2");
		var coat2 = scn.GetFreepsCard("coat2");
		var shield2 = scn.GetFreepsCard("shield2");

		scn.MoveCompanionsToTable(gimli, sam);
		scn.MoveAlliesToTable(maggot);
		// Gimli is the one unbound companion bearing three cards, and his axe the one weapon on the table, so he
		// is chosen without asking whichever way the card is read.  Sam and Maggot bear three Free Peoples cards
		// each and no weapon.
		scn.AttachCardsTo(gimli, axe, armor, poison);
		scn.AttachCardsTo(sam, coat, shield, saga);
		scn.AttachCardsTo(maggot, armor2, coat2, shield2);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(grima));
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));

		assertTrue(scn.IsHindered(axe));
		assertTrue(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(shield));
		assertFalse(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(armor2));
		assertFalse(scn.IsHindered(coat2));
		assertFalse(scn.IsHindered(shield2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	@Ignore("Q-0138: whether 'that companion' is only the bearer of the weapon chosen")
	public void AWeaponOnACompanionBearingTwoCardsMayStillBeChosenBesideACompanionBearingThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var poison = scn.GetShadowCard("poison");
		var coat = scn.GetFreepsCard("coat");
		var shield = scn.GetFreepsCard("shield");
		var poison2 = scn.GetShadowCard("poison2");

		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		// Aragorn bears two cards, one a weapon; Gimli bears three, one a weapon; Boromir bears three and no weapon.
		scn.AttachCardsTo(aragorn, sword, saga);
		scn.AttachCardsTo(gimli, axe, armor, poison);
		scn.AttachCardsTo(boromir, coat, shield, poison2);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		// The weapons are what is chosen.  Boromir is reached through no weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, axe));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, coat, shield));
		scn.ShadowChooseCard(sword);

		// Aragorn bears two cards: the sword alone.
		assertTrue(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(axe));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(shield));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
