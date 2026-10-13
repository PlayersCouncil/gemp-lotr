package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_154_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "4_154");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf; a skirmish ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound [gondor] Man; a skirmish ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("maggot", "1_288");     // Farmer Maggot: [shire] Hobbit ally; a fellowship ability never used here

					// What Aragorn may bear: a hand weapon, a ranged weapon and an artifact
					put("sword", "1_112");      // Ranger's Sword: possession, hand weapon, bearer must be Aragorn
					put("bow", "1_90");         // Aragorn's Bow: possession, ranged weapon, bearer must be Aragorn; an archery ability
					put("barahir", "6_55");     // Ring of Barahir: artifact, bearer must be Aragorn; a start-of-turn heal
					// What Gimli may bear: a possession, an artifact and a condition
					put("armor", "1_8");        // Dwarven Armor: possession, armor, bearer must be a Dwarf; about overwhelming only
					put("retribution", "9_9");  // Ring of Retribution: artifact, bearer must be a Dwarf; a response to a won skirmish
					put("endurance", "2_4");    // Endurance of Dwarves: condition, bearer must be a Dwarf; a skirmish ability
					// What Boromir may bear
					put("cloak", "1_42");       // Elven Cloak: possession, cloak, bearer must be a companion; minion archery total -1
					put("gondorsword", "4_134");// Sword of Gondor: possession, hand weapon, bearer must be a [gondor] Man
					// What a Hobbit may bear, companion or ally
					put("hobbitsword", "1_299");// Hobbit Sword: hand weapon, bearer must be a Hobbit, no other text
					put("hobbitsword2", "1_299");
					put("orcarmor", "10_113");  // Orc Armor: armor, bearer must be a Hobbit; about Orcs skirmishing bearer
					put("orcarmor2", "10_113");
					put("pipe", "1_285");       // Bilbo's Pipe: a pipe, bearer must be a Hobbit; a fellowship ability never used here

					put("poison", "8_28");      // Spider Poison: a Shadow possession a companion bears; exerts bearer when he is assigned
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
		 * Name: Gríma, Wormtongue
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Maneuver:</b> Exert Gríma and spot an unbound companion bearing 3 or more cards to return each Free Peoples card that companion bears to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grima");

		assertEquals("Gríma", card.getBlueprint().getTitle());
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
	public void ManeuverAbilityExertsGrimaToReturnEveryFreePeoplesCardTheChosenUnboundCompanionBearsToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var barahir = scn.GetFreepsCard("barahir");
		var armor = scn.GetFreepsCard("armor");
		var retribution = scn.GetFreepsCard("retribution");
		var endurance = scn.GetFreepsCard("endurance");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var hobbitsword2 = scn.GetFreepsCard("hobbitsword2");
		var orcarmor = scn.GetFreepsCard("orcarmor");
		var orcarmor2 = scn.GetFreepsCard("orcarmor2");
		var cloak = scn.GetFreepsCard("cloak");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(maggot);
		// Gimli: a possession, an artifact, a condition and a Shadow possession.
		scn.AttachCardsTo(gimli, armor, retribution, endurance, poison);
		// Aragorn: three Free Peoples cards, the other valid candidate.
		scn.AttachCardsTo(aragorn, sword, bow, barahir);
		// Sam is Ring-bound, Farmer Maggot an ally: three cards each.
		scn.AttachCardsTo(sam, hobbitsword, orcarmor, cloak);
		scn.AttachCardsTo(maggot, hobbitsword2, orcarmor2, pipe);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, maggot, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertInZone(Zone.HAND, armor, retribution, endurance);
		assertEquals(3, scn.GetFreepsHandCount());
		assertAttachedTo(poison, gimli);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(hobbitsword, sam);
		assertAttachedTo(hobbitsword2, maggot);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilAnUnboundCompanionBearsAThirdCardAndAShadowCardCountsButStaysOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var sword = scn.GetFreepsCard("sword");
		var barahir = scn.GetFreepsCard("barahir");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var hobbitsword2 = scn.GetFreepsCard("hobbitsword2");
		var orcarmor = scn.GetFreepsCard("orcarmor");
		var orcarmor2 = scn.GetFreepsCard("orcarmor2");
		var cloak = scn.GetFreepsCard("cloak");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(aragorn, sword, barahir);
		scn.AttachCardsTo(sam, hobbitsword, orcarmor, cloak);
		scn.AttachCardsTo(maggot, hobbitsword2, orcarmor2, pipe);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Aragorn bears two; the Ring-bound companion and the ally bearing three are not spotted.
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grima));

		// A Shadow card is the third card he bears.
		scn.AttachCardsTo(aragorn, poison);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.HAND, sword, barahir);
		assertEquals(2, scn.GetFreepsHandCount());
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(hobbitsword, sam);
		assertAttachedTo(hobbitsword2, maggot);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenGrimaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword, bow, barahir);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.AddWoundsToChar(grima, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: he cannot exert.
		assertEquals(2, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grima));
		assertAttachedTo(sword, aragorn);
	}

	@Test
	public void ManeuverAbilitySpotsACompanionWhoseThirdCardIsHinderedAndReturnsThatCardWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var hindered = scn.GetFreepsCard("barahir");
		var armor = scn.GetFreepsCard("armor");
		var retribution = scn.GetFreepsCard("retribution");
		var endurance = scn.GetFreepsCard("endurance");
		var cloak = scn.GetFreepsCard("cloak");
		var gondorsword = scn.GetFreepsCard("gondorsword");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		// Aragorn bears three cards, one of them hindered; Gimli two for now (a third comes later); Boromir two.
		scn.AttachCardsTo(aragorn, sword, bow, hindered);
		scn.AttachCardsTo(gimli, armor, retribution);
		scn.AttachCardsTo(boromir, cloak, gondorsword);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		// A hindered card is a card still, still borne, and a Free Peoples card still (CR 5.0 "hinder")
		scn.HinderCard(hindered);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(hindered));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetFreepsHandCount());
		// Aragorn is the only companion bearing three, and his third card is the hindered one.
		assertTrue(scn.ShadowActionAvailable(grima));

		// A third card on Gimli, so that there is a valid companion beside Aragorn to choose from.
		scn.AttachCardsTo(gimli, endurance);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, frodo));
		scn.ShadowChooseCard(aragorn);

		assertInZone(Zone.HAND, sword, bow, hindered);
		assertFalse(scn.IsHindered(hindered));
		assertEquals(3, scn.GetFreepsHandCount());
		assertAttachedTo(armor, gimli);
		assertAttachedTo(cloak, boromir);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
