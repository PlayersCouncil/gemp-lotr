package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_232_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "1_232");

					put("otsea", "1_235");      // Úlairë Otsëa: [wraith] Nazgûl, strength 9; a Shadow ability used only on purpose
					// In Twilight: [wraith] condition, strength +2, "Bearer must be a [wraith] minion"; its triggers answer its
					// play and its bearer's win, neither of which happens here.
					put("twilight", "19_34");

					// Black Breath and Blade Tip: [wraith] conditions that come to be borne by a character in play (by a
					// transfer); Black Breath bars healing its bearer, Blade Tip wounds its bearer at the start of a
					// fellowship phase, and neither comes here.
					put("breath1", "1_207");
					put("breath2", "1_207");
					put("tip", "1_209");
					put("weary", "4_212");      // Weary: an [isengard] condition that plays on a companion or ally; its trigger needs an [isengard] tracker
					put("wreathed", "1_238");   // Wreathed in Shadow: a [wraith] support-area condition; an archery ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6; a skirmish ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, strength 3; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Lemenya, Lieutenant of Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Each companion or ally who bears a [wraith] condition is strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Lieutenant of Morgul", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCompanionOrAllyBearingAWraithConditionIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var twilight = scn.GetShadowCard("twilight");
		var breath1 = scn.GetShadowCard("breath1");
		var breath2 = scn.GetShadowCard("breath2");
		var tip = scn.GetShadowCard("tip");
		var weary = scn.GetShadowCard("weary");
		var wreathed = scn.GetShadowCard("wreathed");

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var grimir = scn.GetFreepsCard("grimir");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(grimir);
		scn.MoveMinionsToTable(otsea);
		scn.AttachCardsTo(frodo, breath1);
		scn.AttachCardsTo(aragorn, tip);
		scn.AttachCardsTo(grimir, breath2);
		scn.AttachCardsTo(gimli, weary);
		scn.AttachCardsTo(otsea, twilight);
		scn.MoveCardsToSupportArea(wreathed);

		// Without Lemenya.  Frodo is 3, and 1 for The Ruling Ring; Otsëa 9, and 2 for In Twilight.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(11, scn.GetStrength(otsea));

		scn.MoveMinionsToTable(lemenya);

		// The Ring-bearer and a [gondor] companion, bearing two different [wraith] conditions, and an ally.
		assertEquals(2, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetStrength(grimir));
		// A companion bearing an [isengard] condition; a companion bearing none while a [wraith] condition is in
		// the support area; a minion bearing a [wraith] condition.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(11, scn.GetStrength(otsea));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
