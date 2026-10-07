package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
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
					put("card", "4_154");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion
					// Dwarven Armor: a Free Peoples possession that changes no number, put on the Men by the rig
					put("armor1", "1_8");
					put("armor2", "1_8");
					put("armor3", "1_8");
					put("armor4", "1_8");
					put("armor5", "1_8");
					put("armor6", "1_8");
					put("armor7", "1_8");
					put("armor8", "1_8");
					put("poison", "8_28");      // Spider Poison: a Shadow card that a companion bears
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

		var card = scn.GetFreepsCard("card");

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

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GrimaTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void ManeuverAbilitySpotsACompanionWhoseThirdCardIsHinderedAndReturnsThatCardWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("card");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		var hindered = scn.GetFreepsCard("armor3");
		var armor4 = scn.GetFreepsCard("armor4");
		var armor5 = scn.GetFreepsCard("armor5");
		var armor6 = scn.GetFreepsCard("armor6");
		var armor7 = scn.GetFreepsCard("armor7");
		var armor8 = scn.GetFreepsCard("armor8");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		// Aragorn bears three cards, one of them hindered, and a Shadow card; Gimli three, none hindered;
		// Boromir two.
		scn.AttachCardsTo(aragorn, armor1, armor2, hindered);
		scn.AttachCardsTo(gimli, armor4, armor5, armor6);
		scn.AttachCardsTo(boromir, armor7, armor8);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.MANEUVER);
		// A hindered card is a card still, still borne, and a Free Peoples card still (CR 5.0 "hinder")
		scn.HinderCard(hindered);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(hindered));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, frodo));
		scn.ShadowChooseCard(aragorn);

		assertInZone(Zone.HAND, armor1, armor2, hindered);
		assertFalse(scn.IsHindered(hindered));
		assertEquals(3, scn.GetFreepsHandCount());
		assertAttachedTo(armor4, gimli);
		assertAttachedTo(armor7, boromir);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
