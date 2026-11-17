package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentry", "6_102");
					put("sentry2", "6_102");
					// Morgul Hunter: [sauron] Orc; a strength bonus only
					put("hunter", "1_256");
					// Troll of Cirith Gorgor: [sauron] Troll; a strength bonus for possessions only
					put("troll", "10_101");
					// Goblin Patrol Troop: [moria] Orc, no text
					put("troop", "1_177");
					// The Number Must Be Few: [sauron] support-area condition; nothing below 7 companions
					put("few", "1_260");
					// You Bring Great Evil: [sauron] condition borne by a companion; nothing away from a sanctuary
					put("evil", "1_283");
					// Stairs of Khazad-dûm: [dwarven] support-area condition; nothing outside a skirmish
					put("stairs", "1_24");
					// The Last Alliance of Elves and Men: [elven] condition borne by a [gondor] Man; a strength bonus only
					put("alliance", "1_49");
					// Dwarven Axe: [dwarven] possession borne by a Dwarf; nothing outside a skirmish
					put("axe", "1_9");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GateSentryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gate Sentry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Maneuver:</b> Exert this minion twice and spot another [sauron] minion to discard a condition.
		*/

		var scn = GetScenario();

		var sentry = scn.GetFreepsCard("sentry");

		assertEquals("Gate Sentry", sentry.getBlueprint().getTitle());
		assertNull(sentry.getBlueprint().getSubtitle());
		assertFalse(sentry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sentry.getBlueprint().getSide());
		assertEquals(Culture.SAURON, sentry.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sentry.getBlueprint().getCardType());
		assertEquals(Race.ORC, sentry.getBlueprint().getRace());
		assertEquals(3, sentry.getBlueprint().getTwilightCost());
		assertEquals(9, sentry.getBlueprint().getStrength());
		assertEquals(3, sentry.getBlueprint().getVitality());
		assertEquals(6, sentry.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsTwiceAndSpotsAnotherSauronMinionToDiscardACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stairs = scn.GetFreepsCard("stairs");
		var alliance = scn.GetFreepsCard("alliance");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(stairs);
		scn.AttachCardsTo(aragorn, alliance);
		scn.AttachCardsTo(gimli, axe);

		var sentry = scn.GetShadowCard("sentry");
		var hunter = scn.GetShadowCard("hunter");
		var few = scn.GetShadowCard("few");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToSupportArea(few);
		scn.AttachCardsTo(gimli, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, hunter);
		assertInZone(Zone.SUPPORT, stairs, few);
		assertInZone(Zone.ATTACHED, alliance, axe, evil);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(hunter));
		assertTrue(scn.ShadowActionAvailable(sentry));
		scn.ShadowUseCardAction(sentry);

		// Exerted twice; the [sauron] Orc spotted is not exerted.
		assertEquals(2, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(hunter));
		// Conditions of either side, borne or in a support area; not a possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, alliance, few, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(alliance);

		assertInDiscard(alliance);
		assertInZone(Zone.SUPPORT, stairs, few);
		assertInZone(Zone.ATTACHED, axe, evil);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASecondGateSentryIsAnotherSauronMinionAndWithNoConditionTheExertionsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, axe);

		var sentry = scn.GetShadowCard("sentry");
		var sentry2 = scn.GetShadowCard("sentry2");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, sentry2);
		assertInZone(Zone.ATTACHED, axe);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(sentry2));
		assertTrue(scn.ShadowActionAvailable(sentry));
		scn.ShadowUseCardAction(sentry);

		// The copy used exerts twice, the other copy not at all; no condition is in play, and the possession stays.
		assertEquals(2, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(sentry2));
		assertInZone(Zone.ATTACHED, axe);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedUntilAnotherSauronMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs);

		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var few = scn.GetShadowCard("few");
		scn.MoveCardsToSupportArea(few);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, troop);
		assertInZone(Zone.SUPPORT, stairs, few);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Gate Sentry itself, a [moria] minion and a [sauron] condition: no other [sauron] minion.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertFalse(scn.ShadowActionAvailable(sentry));

		// A [sauron] Troll is one.
		scn.MoveMinionsToTable(troll);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(sentry));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertInZone(Zone.SUPPORT, stairs, few);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenGateSentryCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs);

		var sentry = scn.GetShadowCard("sentry");
		var hunter = scn.GetShadowCard("hunter");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, hunter);
		// Vitality 3 with a wound: 2 left, enough to exert once and not twice.
		scn.AddWoundsToChar(sentry, 1);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertInZone(Zone.SUPPORT, stairs);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sentry));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertInZone(Zone.SUPPORT, stairs);
	}
}
