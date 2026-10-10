package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
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
					put("guard", "1_7");        // Dwarf Guard: unbound companion, no text but a requirement to play

					// Weapons.
					put("sword", "1_112");      // Ranger's Sword: hand weapon, bearer must be Aragorn
					put("bow", "1_41");         // Elven Bow: ranged weapon, bearer must be an Elf
					put("aragornsbow", "1_90"); // Aragorn's Bow: ranged weapon, bearer must be Aragorn; an archery ability never used here
					put("axe", "1_14");         // Gimli's Battle Axe: hand weapon, bearer must be Gimli
					put("gondorsword", "4_134");// Sword of Gondor: hand weapon, bearer must be a [gondor] Man
					put("hobbitsword", "1_299");// Hobbit Sword: hand weapon, bearer must be a Hobbit, companion or ally
					put("hobbitsword2", "1_299");
					put("broadsword", "4_142"); // Broad-bladed Sword: Shadow hand weapon, bearer must be an Uruk-hai

					// Free Peoples cards that are not weapons.
					put("armor", "1_8");        // Dwarven Armor: armor, about being overwhelmed only
					put("armor2", "1_8");
					put("coat", "1_101");       // Coat of Mail: armor, about being overwhelmed only
					put("coat2", "1_101");
					put("shield", "1_107");     // Great Shield: a shield; minion archery total -1
					put("shield2", "1_107");
					// Cards a Hobbit may bear, one of each class: no skirmish or archery phase is reached, so their texts stay idle.
					put("orcarmor", "10_113");  // Orc Armor: armor, bearer must be a Hobbit; about Orcs and Uruk-hai skirmishing bearer
					put("orcarmor2", "10_113");
					put("pipe", "1_285");       // Bilbo's Pipe: a pipe, bearer must be a Hobbit; a fellowship ability never used here
					put("gafferspipe", "1_292");// The Gaffer's Pipe: a pipe, bearer must be a Hobbit; a fellowship ability never used here
					put("salt", "11_171");      // Salt from the Shire: a box, bearer must be a Hobbit; abilities never used here
					// Cards any companion may bear.
					put("cloak", "1_42");       // Elven Cloak: a cloak, bearer must be a companion; minion archery total -1
					put("brooch", "4_63");      // Elven Brooch: a brooch, bearer must be a companion; a response to a discard by a Shadow card
					put("saga", "1_114");       // The Saga of Elendil: condition on a [gondor] companion; a skirmish ability
					put("endurance", "2_4");    // Endurance of Dwarves: condition on a Dwarf; a skirmish ability

					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion
					put("poison2", "8_28");
					put("poison3", "8_28");
					put("evil", "1_283");       // You Bring Great Evil: Shadow condition on a companion; at a sanctuary bearer cannot heal
					put("evil2", "1_283");
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
		 * Game Text: Maneuver: Exert Grima to spot an unbound companion. Hinder a weapon borne by that companion (or
		 * all Free Peoples cards borne by them if they bear 3 or more cards).
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
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		var aragornsbow = scn.GetFreepsCard("aragornsbow");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var broadsword = scn.GetShadowCard("broadsword");

		scn.MoveCompanionsToTable(aragorn, legolas, gimli, sam);
		// Aragorn bears two cards, a hand weapon and a ranged weapon: one short of three.
		scn.AttachCardsTo(aragorn, sword, aragornsbow);
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
		// Unbound companions bearing a weapon; not Gimli, whose armor is no weapon, nor the Ring-bound Sam and Frodo,
		// nor a minion bearing a weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, legolas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, sam, frodo, warrior));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// A weapon of his, of the Shadow player's choosing; no one else's.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, aragornsbow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bow, armor, hobbitsword, broadsword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragornsbow);

		assertTrue(scn.IsHindered(aragornsbow));
		assertAttachedTo(aragornsbow, aragorn);
		assertFalse(scn.IsHindered(sword));
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
		// A hindered companion cannot be spotted, and a card he bears is not borne by a companion (Q-0108).
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, legolas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir));
		scn.ShadowChooseCard(aragorn);

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
		// Exactly three cards, one of them a Shadow card.  Aragorn bears one card and no weapon, so Gimli is the
		// only companion who can be spotted, and is spotted without asking.
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
		var endurance = scn.GetFreepsCard("endurance");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		// Aragorn bears three cards of which one will be hindered, and no weapon; Gimli bears three (his armor, a
		// condition and his axe); Boromir bears two.
		scn.AttachCardsTo(aragorn, coat1, saga, shield1);
		scn.AttachCardsTo(gimli, armor1, endurance, axe);
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
		assertFalse(scn.IsHindered(endurance));
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
		var orcarmor = scn.GetFreepsCard("orcarmor");
		var cloak = scn.GetFreepsCard("cloak");
		var hobbitsword2 = scn.GetFreepsCard("hobbitsword2");
		var orcarmor2 = scn.GetFreepsCard("orcarmor2");
		var pipe = scn.GetFreepsCard("pipe");

		scn.MoveCompanionsToTable(aragorn, legolas, sam);
		scn.MoveAlliesToTable(maggot);
		// Two unbound companions bear one weapon each and nothing else.
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(legolas, bow);
		// Sam is Ring-bound and Maggot, a Hobbit too, is an ally: each bears three cards, one of them a weapon.
		scn.AttachCardsTo(sam, hobbitsword, orcarmor, cloak);
		scn.AttachCardsTo(maggot, hobbitsword2, orcarmor2, pipe);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(grima));
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		// Sam is Ring-bound and Maggot no companion: neither can be spotted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, legolas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, maggot));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legolas);

		assertTrue(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(hobbitsword));
		assertFalse(scn.IsHindered(orcarmor));
		assertFalse(scn.IsHindered(cloak));
		assertFalse(scn.IsHindered(hobbitsword2));
		assertFalse(scn.IsHindered(orcarmor2));
		assertFalse(scn.IsHindered(pipe));
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
		var orcarmor = scn.GetFreepsCard("orcarmor");
		var cloak = scn.GetFreepsCard("cloak");
		var pipe = scn.GetFreepsCard("pipe");
		var orcarmor2 = scn.GetFreepsCard("orcarmor2");
		var gafferspipe = scn.GetFreepsCard("gafferspipe");
		var salt = scn.GetFreepsCard("salt");

		scn.MoveCompanionsToTable(gimli, sam);
		scn.MoveAlliesToTable(maggot);
		// Gimli is the one unbound companion bearing three cards or a weapon, so he is spotted without asking.
		// Sam and Maggot bear three Free Peoples cards each and no weapon: an armor, a cloak and a pipe; an
		// armor, a pipe and a box.
		scn.AttachCardsTo(gimli, axe, armor, poison);
		scn.AttachCardsTo(sam, orcarmor, cloak, pipe);
		scn.AttachCardsTo(maggot, orcarmor2, gafferspipe, salt);

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
		assertFalse(scn.IsHindered(orcarmor));
		assertFalse(scn.IsHindered(cloak));
		assertFalse(scn.IsHindered(pipe));
		assertFalse(scn.IsHindered(orcarmor2));
		assertFalse(scn.IsHindered(gafferspipe));
		assertFalse(scn.IsHindered(salt));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
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
		// Each may be spotted: Aragorn and Gimli bear a weapon, Gimli and Boromir three cards.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, boromir));
		scn.ShadowChooseCard(aragorn);

		// Aragorn bears two cards: his one weapon is hindered, and not the condition beside it.
		assertTrue(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(axe));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(shield));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OnlyACompanionBearingAWeaponOrThreeCardsWithAnUnhinderedFreePeoplesCardMayBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var endurance = scn.GetFreepsCard("endurance");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		var brooch = scn.GetFreepsCard("brooch");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var poison = scn.GetShadowCard("poison");
		var poison2 = scn.GetShadowCard("poison2");
		var poison3 = scn.GetShadowCard("poison3");
		var evil = scn.GetShadowCard("evil");
		var evil2 = scn.GetShadowCard("evil2");

		scn.MoveCompanionsToTable(aragorn, guard, gimli, legolas, boromir, sam);
		// Aragorn bears one weapon; the Guard three cards, two of them Shadow cards.
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(guard, armor2, poison3, evil2);
		// Gimli bears two cards and no weapon.
		scn.AttachCardsTo(gimli, armor, endurance);
		// Legolas bears three cards (his bow, a cloak and a brooch), a weapon among them, all of which will be hindered.
		scn.AttachCardsTo(legolas, bow, cloak, brooch);
		// Boromir bears three Shadow cards.
		scn.AttachCardsTo(boromir, poison, poison2, evil);
		// Sam is Ring-bound.
		scn.AttachCardsTo(sam, hobbitsword);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.HinderCard(bow, cloak, brooch);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(bow));
		assertAttachedTo(bow, legolas);
		assertEquals(0, scn.GetWoundsOn(grima));
		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, legolas, boromir, sam, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		// The Guard bears three: his one Free Peoples card is hindered, and neither Shadow card.
		assertTrue(scn.IsHindered(armor2));
		assertFalse(scn.IsHindered(poison3));
		assertFalse(scn.IsHindered(evil2));
		assertFalse(scn.IsHindered(guard));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(endurance));
		assertFalse(scn.IsHindered(hobbitsword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
