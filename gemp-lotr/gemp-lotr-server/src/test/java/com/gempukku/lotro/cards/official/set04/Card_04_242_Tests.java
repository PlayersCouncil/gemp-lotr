package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_242_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raiders", "4_242");
					put("raiders2", "4_242");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Easterling Man, vitality 2; strength +3 and fierce only at 2 burdens
					put("soldier", "4_220");    // Desert Soldier: [raider] Southron Man, vitality 2; a start-of-skirmish trigger only
					put("footmen", "6_3");      // Dunlending Footmen: [dunland] Man, vitality 2; a cost text only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RaidersFromtheEastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Raiders From the East
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	<b>Shadow:</b> Remove (3) and exert a [raider] Man to place a [raider] token here.
		 * 	<b>Maneuver:</b> Remove a [raider] token here to add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raiders");

		assertEquals("Raiders From the East", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityRemovesThreeAndExertsARaiderManToPlaceATokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		var footmen = scn.GetShadowCard("footmen");
		scn.MoveCardsToSupportArea(raiders, raiders2);
		scn.MoveMinionsToTable(axeman, soldier, footmen);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(raiders2));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertTrue(scn.ShadowActionAvailable(raiders));
		scn.ShadowUseCardAction(raiders);
		// An Easterling and a Southron [raider] Man are offered; a [dunland] Man is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footmen));
		scn.ShadowChooseCard(axeman);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(soldier));
		// The token goes on this copy and not on the other.
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(raiders2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiders = scn.GetShadowCard("raiders");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(raiders);
		scn.MoveMinionsToTable(axeman);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		// The Axeman could exert; the pool is one short.
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(raiders));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheOnlyRaiderManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiders = scn.GetShadowCard("raiders");
		var axeman = scn.GetShadowCard("axeman");
		var footmen = scn.GetShadowCard("footmen");
		scn.MoveCardsToSupportArea(raiders);
		scn.MoveMinionsToTable(axeman, footmen);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(3);
		// Vitality 2: one wound exhausts the Axeman.  The [dunland] Man could exert.
		scn.AddWoundsToChar(axeman, 1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(raiders));
	}

	@Test
	public void ManeuverAbilityRemovesATokenHereToAddOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(raiders, raiders2);
		scn.AddTokensToCard(raiders, 1);
		scn.AddTokensToCard(raiders2, 1);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(raiders2));
		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(raiders));
		scn.ShadowUseCardAction(raiders);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(raiders2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithNoTokenHereThoughTheOtherCopyHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(raiders, raiders2);
		scn.AddTokensToCard(raiders2, 1);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		assertEquals(0, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(raiders2));
		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(raiders));
		assertTrue(scn.ShadowActionAvailable(raiders2));
	}
}
