package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_152_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shaman", "51_152");
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc minion, vitality 3; a regroup ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai minion, vitality 2; Damage +1 is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukShamanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Shaman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * <b>Maneuver:</b> Remove (2) to heal an [Isengard] minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shaman");

		assertEquals("Uruk Shaman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForThreePlusRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCardsToHand(shaman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(shaman));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shaman);
		// 3 to play + 2 roaming (site 5 minion at site 2)
		assertEquals(twilight - 5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, shaman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverRemovesTwoTwilightToHealAnIsengardMinionOfAnyRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var servant = scn.GetShadowCard("servant");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, servant, sentry);
		scn.AddWoundsToChar(shaman, 1);
		// Two wounds on the Servant, so that healing one wound is told from healing two.
		scn.AddWoundsToChar(servant, 2);
		scn.AddWoundsToChar(sentry, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);
		assertEquals(0, scn.GetTwilight());

		// The Shaman himself and the [isengard] Orc; not the [sauron] Uruk-hai, nor a wounded companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shaman, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(servant);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, servant);
		scn.AddWoundsToChar(servant, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shaman));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void NotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.MoveMinionsToTable(shaman, servant);
		scn.AddWoundsToChar(servant, 1);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shaman));
		assertEquals(1, scn.GetWoundsOn(servant));
	}
}
