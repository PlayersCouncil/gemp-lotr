package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sergeant", "7_146");
					put("sergeant2", "7_146");
					// Easterling Ransacker: a [raider] Easterling Man; his only text is a skirmish ability
					put("ransacker", "7_144");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl who is an Easterling; his texts need 3 and 5 burdens
					put("khamul", "103_61");
					// Desert Soldier: a [raider] Man who is a Southron and no Easterling; his text is a skirmish trigger
					put("soldier", "4_220");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingSergeantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Easterling Sergeant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br><b>Maneuver:</b> If you cannot spot a burden, spot another Easterling and exert this minion to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sergeant");

		assertEquals("Easterling Sergeant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsHimToAddABurdenOnlyWhileNoBurdenCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sergeant = scn.GetShadowCard("sergeant");
		var sergeant2 = scn.GetShadowCard("sergeant2");
		var ransacker = scn.GetShadowCard("ransacker");

		scn.StartGame();
		scn.MoveMinionsToTable(sergeant, sergeant2, ransacker);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sergeant));
		assertEquals(0, scn.GetWoundsOn(sergeant2));
		assertEquals(0, scn.GetWoundsOn(ransacker));
		assertTrue(scn.ShadowActionAvailable(sergeant));

		scn.ShadowUseCardAction(sergeant);

		// He exerts himself, and nobody else.
		assertEquals(1, scn.GetWoundsOn(sergeant));
		assertEquals(0, scn.GetWoundsOn(sergeant2));
		assertEquals(0, scn.GetWoundsOn(ransacker));
		assertEquals(1, scn.GetBurdens());

		// With a burden to spot, the other Sergeant is not offered his own, though he could exert and the
		// Ransacker is still another Easterling.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sergeant2));
		assertFalse(scn.ShadowActionAvailable(sergeant));
		assertEquals(0, scn.GetWoundsOn(sergeant2));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sergeant = scn.GetShadowCard("sergeant");
		var ransacker = scn.GetShadowCard("ransacker");

		scn.StartGame();
		scn.MoveMinionsToTable(sergeant, ransacker);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(sergeant, 1);
		assertEquals(1, scn.GetWoundsOn(sergeant));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(ransacker));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sergeant));
	}

	@Test
	public void AnotherEasterlingIsOneOtherThanHimselfAndNoOtherRaiderMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sergeant = scn.GetShadowCard("sergeant");
		var soldier = scn.GetShadowCard("soldier");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(sergeant, soldier);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Himself and a [raider] Man who is no Easterling: not offered.
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sergeant));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sergeant));

		// An Easterling who is a Nazgûl and no Man.
		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(sergeant));
		scn.ShadowUseCardAction(sergeant);
		assertEquals(1, scn.GetWoundsOn(sergeant));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherCopyOfThisMinionIsAnotherEasterling() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sergeant = scn.GetShadowCard("sergeant");
		var sergeant2 = scn.GetShadowCard("sergeant2");

		scn.StartGame();
		scn.MoveMinionsToTable(sergeant, sergeant2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sergeant));
		assertEquals(0, scn.GetWoundsOn(sergeant2));
		assertTrue(scn.ShadowActionAvailable(sergeant));
		assertTrue(scn.ShadowActionAvailable(sergeant2));

		scn.ShadowUseCardAction(sergeant2);
		assertEquals(0, scn.GetWoundsOn(sergeant));
		assertEquals(1, scn.GetWoundsOn(sergeant2));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
