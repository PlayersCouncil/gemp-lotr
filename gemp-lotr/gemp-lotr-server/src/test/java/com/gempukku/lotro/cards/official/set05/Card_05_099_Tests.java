package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "5_99");
					put("veteran2", "5_99");      // a second copy: not unique, and "exert him" is the one played
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, cost 2; his text answers only a weapon played on him
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: [elven] Elf; stronger only against a Nazgul
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; his text only changes his own strength
					put("bounder", "1_286");      // Bounder: [shire] Hobbit ally, a character who is no companion
					// Blood of Numenor: [gondor] support-area condition; "Each [sauron] Orc comes into play exhausted"
					put("blood", "2_31");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GateVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Gate Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: When you play this minion, you may exert him and spot 6 companions to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("Gate Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedWithSixCompanionsHeMayExertHimselfToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var veteran2 = scn.GetShadowCard("veteran2");
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "boromir", "gandalf");

		scn.StartGame();
		scn.MoveMinionsToTable(veteran2);
		scn.MoveCardsToHand(veteran);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(veteran2));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(veteran);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran));
		scn.ShadowAcceptOptionalTrigger();

		// The one played exerts, not the copy already in play.
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(veteran2));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobodyAndDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "boromir", "gandalf");

		scn.StartGame();
		scn.MoveCardsToHand(veteran);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(veteran);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(veteran));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "boromir");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveCardsToHand(veteran);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Frodo and four companions, and an ally: six characters, five companions.
		scn.ShadowPlayCard(veteran);

		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(veteran));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherSauronOrcPlayedSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "boromir", "gandalf");

		scn.StartGame();
		scn.MoveMinionsToTable(veteran);
		scn.MoveCardsToHand(chieftain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(veteran));
		scn.ShadowPlayCard(chieftain);

		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(veteran));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenHeComesIntoPlayExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var blood = scn.GetFreepsCard("blood");
		scn.MoveCompanionsToTable("aragorn", "gimli", "arwen", "boromir", "gandalf");
		scn.MoveCardsToSupportArea(blood);

		scn.StartGame();
		scn.MoveCardsToHand(veteran);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(veteran);

		// Vitality 2, and he arrives with a wound: he cannot exert.
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertEquals(1, scn.GetWoundsOn(veteran));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(veteran));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
