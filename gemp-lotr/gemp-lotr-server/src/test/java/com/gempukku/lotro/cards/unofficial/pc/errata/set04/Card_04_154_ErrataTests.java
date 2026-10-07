package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_04_154_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "54_154");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound companion; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound companion; a skirmish ability never used here
					put("boromir", "1_97");     // Boromir, Lord of Gondor: unbound companion; a skirmish ability never used here
					put("coat1", "1_101");      // Coat of Mail: Free Peoples possession, armor; about being overwhelmed only
					put("coat2", "1_101");
					put("shield1", "1_107");    // Great Shield: Free Peoples possession, a shield; minion archery total -1
					put("shield2", "1_107");
					put("saga", "1_114");       // The Saga of Elendil: Free Peoples condition borne by a companion
					put("armor1", "1_8");       // Dwarven Armor: Free Peoples possession, armor; about being overwhelmed only
					put("armor2", "1_8");
					put("axe", "1_14");         // Gimli's Battle Axe: Free Peoples possession, a hand weapon
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
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Maneuver: Exert Grima to hinder a possession borne by an unbound companion (or all Free Peoples cards borne by that companion if they are bearing 3 or more cards).
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
	public void AHinderedCardCountsTowardsTheThreeCardsACompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var coat1 = scn.GetFreepsCard("coat1");
		var coat2 = scn.GetFreepsCard("coat2");
		var shield1 = scn.GetFreepsCard("shield1");
		var shield2 = scn.GetFreepsCard("shield2");
		var saga = scn.GetFreepsCard("saga");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		// Aragorn bears three cards of which one will be hindered; Gimli bears three (the second armor is put
		// there by the rig); Boromir bears two.
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
}
