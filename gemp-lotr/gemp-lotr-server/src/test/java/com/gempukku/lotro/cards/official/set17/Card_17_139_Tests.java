package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "17_139");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound, resistance 6, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, unbound, resistance 6, vitality 3
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound, resistance 6; bears nothing
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, resistance 5, vitality 4
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally, resistance 0
					// Ranger's Sword: a possession Aragorn may bear; strength +2 and damage +1
					put("sword", "1_112");
					// Dwarven Armor: a possession a Dwarf may bear; it changes no number
					put("armor", "1_8");
					// Weary: an [isengard] condition played on a companion or ally; its trigger needs an [isengard] tracker
					put("weary1", "4_212");
					put("weary2", "4_212");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Úlairë Cantëa, Duplicitous Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>Each unbound companion is resistance -1 for each card he or she bears.<br><b>Skirmish:</b> Exert Úlairë Cantëa to exert a companion with resistance 2 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Duplicitous Assassin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachUnboundCompanionIsResistanceMinusOneForEachCardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var cantea = scn.GetShadowCard("cantea");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.AttachCardsTo(aragorn, sword);
		// A Free Peoples possession and a Shadow condition on Gimli; a Shadow condition on Sam, who is Ring-bound.
		scn.AttachCardsTo(gimli, armor, weary1);
		scn.AttachCardsTo(sam, weary2);

		scn.StartGame();

		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(weary1, gimli);
		assertAttachedTo(weary2, sam);
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(boromir));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));

		scn.MoveMinionsToTable(cantea);

		// One card, two cards, none.
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(boromir));
		// Ring-bound companions, each bearing a card (Frodo bears The One Ring), are not reached.
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHinderedCardAnUnboundCompanionBearsLowersHisResistanceLikeTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var weary1 = scn.GetShadowCard("weary1");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);

		assertEquals(6, scn.GetResistance(gimli));
		scn.AttachCardsTo(gimli, armor, weary1);
		// 6 - 1 for each of two cards
		assertEquals(4, scn.GetResistance(gimli));

		// A hindered card is a card still, and still borne
		scn.HinderCard(armor);
		assertTrue(scn.IsHindered(armor));
		assertEquals(4, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsHimToExertACompanionWithResistanceTwoOrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var cantea = scn.GetShadowCard("cantea");
		var weary1 = scn.GetShadowCard("weary1");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, armor, weary1);

		scn.StartGame();
		scn.AddBurdens(3);
		// Sam is exhausted: 3 wounds of 4 vitality.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(cantea);

		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(sam));
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(weary1, gimli);
		// 6 - 3 burdens - 1 card; 6 - 3 - 2; 6 - 3; 5 - 3; 10 - 3.
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(1, scn.GetResistance(gimli));
		assertEquals(3, scn.GetResistance(boromir));
		assertEquals(2, scn.GetResistance(sam));
		assertEquals(7, scn.GetResistance(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		// Aragorn, skirmishing him, and Gimli, in no skirmish; not Boromir at 3, nor Sam, exhausted, nor the ally
		// (resistance 0, but no companion), nor the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, sam, barliman, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionAtResistanceTwoOrLessCanBeExertedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(cantea);

		assertAttachedTo(sword, aragorn);
		assertEquals(3, scn.GetBurdens());
		// Aragorn: 6 - 3 - 1.  Sam is Ring-bound, so the modifier leaves him alone: 5 - 3.  Both are at 2.
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(2, scn.GetResistance(sam));
		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(cantea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoCompanionAtResistanceTwoOrLessHeExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);

		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(10, scn.GetResistance(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExhaustedHeCannotUseIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(cantea);
		scn.AddWoundsToChar(cantea, 2);

		assertEquals(2, scn.GetWoundsOn(cantea));
		assertAttachedTo(sword, aragorn);
		assertEquals(3, scn.GetBurdens());
		// Aragorn is a target: 6 - 3 - 1.
		assertEquals(2, scn.GetResistance(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
	}
}
