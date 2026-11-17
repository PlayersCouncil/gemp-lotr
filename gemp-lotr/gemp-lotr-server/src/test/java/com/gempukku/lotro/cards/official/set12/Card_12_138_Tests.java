package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heirloom", "12_138");

					// Murderous Uruk: an [uruk-hai] Uruk-hai, damage +1 and muster (a regroup matter) only.
					put("uruk", "11_195");
					// Saruman, Agent of the Dark Lord: an [uruk-hai] minion who is no Uruk-hai (a Wizard, lurker).  His
					// skirmish ability and his heal on a companion's death are never reached.
					put("saruman", "12_144");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 and no other text.
					put("cuguard", "10_81");
					// Worthy of Mordor: an [uruk-hai] support-area condition, so an [uruk-hai] card that is no minion.
					// Its start-of-regroup offer is never reached.
					put("worthy", "12_160");
					// Weapon of Opportunity: an [uruk-hai] hand weapon possession on an [uruk-hai] minion; its draw is
					// only on its own play.
					put("weapon", "12_159");

					// Gimli, Son of Gloin: a [dwarven] Dwarf companion, resistance 6.  His skirmish ability is never used.
					put("gimli", "1_13");
					// Dwarven Armor: a [dwarven] armor possession.
					put("armor", "1_8");
					// Hand Axe: a [dwarven] hand weapon possession; its archery ability is never used.
					put("handaxe", "2_10");
					// Ring of Artifice: a [dwarven] ARTIFACT a Dwarf bears; its regroup ability is never used.
					put("ring", "17_4");
					// Aragorn, Ranger of the North: a [gondor] Man companion, resistance 6.
					put("aragorn", "1_89");
					// Ranger's Sword: a [gondor] hand weapon possession; bearer must be Aragorn.
					put("sword", "1_112");
					// Elven Scout: an [elven] Elf companion, resistance 7; muster only while another Elf is spotted.
					put("scout", "11_18");
					// Elven Sword: an [elven] hand weapon possession on an Elf; its skirmish ability is never used.
					put("elvensword", "4_64");
					// Farmer Maggot, Chaser of Rascals: a Hobbit ally (resistance 0 by CR 4.2 "resistance").
					put("maggot", "1_288");
					// Hobbit Sword: a [shire] hand weapon possession on a Hobbit, no other text.
					put("hobbitsword", "1_299");
					// Old Toby: a Free Peoples possession that nobody bears, in the support area.
					put("toby", "1_305");
					// Spider Poison: a Shadow possession a companion can bear; its bearer exerts as he is assigned, which
					// no test reaches.
					put("poison", "8_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrokenHeirloomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Broken Heirloom
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Toil 2</b>. <helper>(For each [uruk-hai] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot an [uruk-hai] minion to discard a possession borne by a companion who has resistance 4 or less.
		*/

		var scn = GetScenario();

		var heirloom = scn.GetFreepsCard("heirloom");

		assertEquals("Broken Heirloom", heirloom.getBlueprint().getTitle());
		assertNull(heirloom.getBlueprint().getSubtitle());
		assertFalse(heirloom.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, heirloom.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, heirloom.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, heirloom.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(heirloom, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(heirloom, Keyword.TOIL));
		assertTrue(scn.HasTimeword(heirloom, Timeword.MANEUVER));
		assertEquals(3, heirloom.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAPossessionBorneByACompanionOfResistance4AndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var scout = scn.GetFreepsCard("scout");
		var elvensword = scn.GetFreepsCard("elvensword");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, aragorn, scout);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(gimli, armor, handaxe, ring);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(scout, elvensword);
		scn.AttachCardsTo(maggot, hobbitsword);
		scn.MoveCardsToSupportArea(toby);

		var heirloom = scn.GetShadowCard("heirloom");
		var uruk = scn.GetShadowCard("uruk");
		var weapon = scn.GetShadowCard("weapon");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToHand(heirloom);

		scn.StartGame();
		// Two burdens: each companion is resistance -2.
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk);
		scn.AttachCardsTo(uruk, weapon);
		scn.AttachCardsTo(gimli, poison);

		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(scout));
		assertEquals(0, scn.GetResistance(maggot));
		assertAttachedTo(armor, gimli);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(elvensword, scout);
		assertAttachedTo(hobbitsword, maggot);
		assertAttachedTo(weapon, uruk);
		assertInZone(Zone.SUPPORT, toby);
		assertInHand(heirloom);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(heirloom));
		scn.ShadowPlayCard(heirloom);
		// The pool pays in full, so toil still asks, from 0, and nobody exerts.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(uruk));

		// The possessions of the two companions of resistance 4, of either side (the Shadow one Gimli bears too); not Gimli's artifact, the possession of the
		// companion of resistance 5, the ally's (resistance 0, but no companion), the minion's, or the one nobody bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, handaxe, poison, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, elvensword, hobbitsword, weapon, toby));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertInDiscard(sword, heirloom);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(elvensword, scout);
		assertAttachedTo(hobbitsword, maggot);
		assertAttachedTo(weapon, uruk);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWithOnlyASauronUrukHaiAndAnUrukHaiConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		var heirloom = scn.GetShadowCard("heirloom");
		var cuguard = scn.GetShadowCard("cuguard");
		var worthy = scn.GetShadowCard("worthy");
		scn.MoveCardsToHand(heirloom);
		scn.MoveCardsToSupportArea(worthy);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(cuguard);

		assertEquals(4, scn.GetResistance(gimli));
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, worthy);
		assertInHand(heirloom);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// The pool covers the whole cost, toil or no toil.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertFalse(scn.ShadowPlayAvailable(heirloom));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertAttachedTo(armor, gimli);
	}

	@Test
	public void SpottingAnUrukHaiWizardItIsPlayedForNothingWhereNoCompanionOfResistance4BearsAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var scout = scn.GetFreepsCard("scout");
		var elvensword = scn.GetFreepsCard("elvensword");
		scn.MoveCompanionsToTable(gimli, aragorn, scout);
		scn.AttachCardsTo(gimli, ring);
		scn.AttachCardsTo(scout, elvensword);

		var heirloom = scn.GetShadowCard("heirloom");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(heirloom);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(saruman);

		// Gimli and Aragorn at 4 bear no possession (the Ring is an artifact); the Scout's sword is at 5.
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(scout));
		assertAttachedTo(ring, gimli);
		assertAttachedTo(elvensword, scout);
		assertInHand(heirloom);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		// Saruman, an [uruk-hai] Wizard, is spotted; the target is no requirement.
		assertTrue(scn.ShadowPlayAvailable(heirloom));
		scn.ShadowPlayCard(heirloom);
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertInDiscard(heirloom);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(elvensword, scout);
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachUrukHaiCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		var heirloom = scn.GetShadowCard("heirloom");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		var cuguard = scn.GetShadowCard("cuguard");
		scn.MoveCardsToHand(heirloom);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(uruk, saruman, cuguard);

		assertEquals(4, scn.GetResistance(gimli));
		assertAttachedTo(armor, gimli);
		assertInHand(heirloom);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		// 3 - 2 for one exertion = 1, which the pool covers.
		assertTrue(scn.ShadowPlayAvailable(heirloom));
		scn.ShadowPlayCard(heirloom);

		// One exertion is owed (toil 1 would owe two); either [uruk-hai] character may make it, not the [sauron] Guard.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cuguard));
		scn.ShadowChooseCard(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		// 1 - (3 - 2): toil 3 would have left the pool at 1.
		assertEquals(0, scn.GetTwilight());
		// The armor is the one possession a companion of resistance 4 bears.
		assertInDiscard(armor, heirloom);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
