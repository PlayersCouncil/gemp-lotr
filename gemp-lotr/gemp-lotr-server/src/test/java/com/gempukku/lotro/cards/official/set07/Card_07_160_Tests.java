package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_160_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bandit", "7_160");
					put("bandit2", "7_160");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that the threat limit is 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronBanditStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Bandit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br><b>Maneuver:</b> Exert this minion to add (5). The Free Peoples player may add a threat to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bandit");

		assertEquals("Southron Bandit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsThisCopyToAdd5WhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bandit = scn.GetShadowCard("bandit");
		var bandit2 = scn.GetShadowCard("bandit2");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(bandit, bandit2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(bandit));
		assertEquals(0, scn.GetWoundsOn(bandit2));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(bandit));

		scn.ShadowUseCardAction(bandit);

		// The copy used exerts, the other does not; the Free Peoples player is asked whether to prevent the (5).
		assertEquals(1, scn.GetWoundsOn(bandit));
		assertEquals(0, scn.GetWoundsOn(bandit2));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(pool + 5, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerAddsAThreatToPreventTheTwilightAndTheExertionStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bandit = scn.GetShadowCard("bandit");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(bandit);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(bandit));
		assertEquals(0, scn.GetThreats());
		// Frodo and Aragorn: two companions, so up to two threats can be added.
		assertEquals(2, scn.GetThreatLimit());

		scn.ShadowUseCardAction(bandit);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		assertEquals(pool, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(bandit));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheFreePeoplesPlayerIsNotAskedAnd5IsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bandit = scn.GetShadowCard("bandit");

		scn.StartGame();
		scn.MoveMinionsToTable(bandit);
		// Frodo is the only companion: one threat is the most there can be.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetThreatLimit());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(bandit));
		scn.ShadowUseCardAction(bandit);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(bandit));
		assertEquals(1, scn.GetThreats());
		assertEquals(pool + 5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedOnAnExhaustedCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bandit = scn.GetShadowCard("bandit");
		var bandit2 = scn.GetShadowCard("bandit2");

		scn.StartGame();
		scn.MoveMinionsToTable(bandit, bandit2);
		// Vitality 2 with 1 wound: this copy is exhausted and cannot exert.  The other copy can.
		scn.AddWoundsToChar(bandit, 1);
		assertEquals(1, scn.GetWoundsOn(bandit));
		assertEquals(0, scn.GetWoundsOn(bandit2));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bandit));
		assertTrue(scn.ShadowActionAvailable(bandit2));
	}
}
