package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("polearm", "6_79");
					put("veteran", "7_147");    // Easterling Veteran: [raider] Easterling Man; his ability is used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Easterling who is a Nazgul
					put("soldier", "4_254");    // Southron Soldier: a [raider] Man who is no Easterling, no text
					put("scout", "15_77");      // Easterling Scout: a [men] Man, an Easterling by title only
					put("attacker", "7_141");   // Easterling Attacker: another Easterling, his ability used only on purpose

					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf who pays for Baruk Khazad
					put("baruk", "5_5");        // Baruk Khazad: Maneuver, exert a Dwarf to wound a minion
					put("light", "4_309");      // Light Shining Faintly: Fellowship, add a burden (to heal a Ring-bound companion)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,      // resistance 10: five burdens do not corrupt him
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingPolearmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Easterling Polearm
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be an Easterling.<br>While you can spot 2 burdens, bearer cannot take wounds.<br>While you can spot 5 burdens, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("polearm");

		assertEquals("Easterling Polearm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnEasterlingOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polearm = scn.GetShadowCard("polearm");
		var veteran = scn.GetShadowCard("veteran");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(polearm);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, khamul, soldier, scout);
		assertInHand(polearm);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(polearm));
		scn.ShadowPlayCard(polearm);
		// An Easterling Man and an Easterling Nazgul may bear it; a [raider] Man who is no Easterling and a
		// [men] Man who is an Easterling by title only may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, scout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertAttachedTo(polearm, khamul);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTwoBurdensCanBeSpottedBearerCannotTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polearm = scn.GetShadowCard("polearm");
		var veteran = scn.GetShadowCard("veteran");
		var attacker = scn.GetShadowCard("attacker");
		var soldier = scn.GetShadowCard("soldier");
		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk, light);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, attacker, soldier);
		scn.AttachCardsTo(veteran, polearm);
		scn.AddBurdens(1);
		assertAttachedTo(polearm, veteran);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		// The second burden by legal play.
		scn.FreepsPlayCard(light);
		assertEquals(2, scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(attacker));
		scn.FreepsPlayCard(baruk);
		// The bearer cannot take wounds, so he cannot be chosen to take one; the other minions can, an
		// Easterling who does not bear it among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(attacker, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(veteran));
		scn.FreepsChooseCard(attacker);

		assertEquals(1, scn.GetWoundsOn(attacker));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOneBurdenBearerTakesAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polearm = scn.GetShadowCard("polearm");
		var veteran = scn.GetShadowCard("veteran");
		var attacker = scn.GetShadowCard("attacker");
		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, attacker);
		scn.AttachCardsTo(veteran, polearm);
		scn.AddBurdens(1);
		assertAttachedTo(polearm, veteran);
		assertEquals(1, scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(veteran));
		scn.FreepsPlayCard(baruk);
		assertTrue(scn.FreepsHasCardChoiceAvailable(veteran, attacker));
		scn.FreepsChooseCard(veteran);

		assertEquals(1, scn.GetWoundsOn(veteran));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OnceFiveBurdensCanBeSpottedBearerIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var polearm = scn.GetShadowCard("polearm");
		var veteran = scn.GetShadowCard("veteran");
		var attacker = scn.GetShadowCard("attacker");
		var light = scn.GetFreepsCard("light");
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, attacker);
		scn.AttachCardsTo(veteran, polearm);
		scn.AddBurdens(4);
		assertAttachedTo(polearm, veteran);
		assertEquals(4, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		// Four burdens: one short.
		assertEquals(0, scn.GetKeywordCount(veteran, Keyword.DAMAGE));

		scn.FreepsPlayCard(light);
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(veteran, Keyword.DAMAGE));
		// The Easterling who does not bear it gains nothing.
		assertEquals(0, scn.GetKeywordCount(attacker, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
