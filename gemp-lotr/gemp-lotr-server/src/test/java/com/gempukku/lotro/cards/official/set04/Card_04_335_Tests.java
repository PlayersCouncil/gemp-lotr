package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_335_Tests
{

	// The Towers path, Uruk Camp its site 2, reached by the first move of the game.  Site 1 is Eastemnet
	// Gullies 4_325, whose fellowship ability is used only on purpose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fighter", "1_146");    // Uruk Fighter: [isengard] Uruk-hai, vitality 2; a maneuver ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, vitality 3; a regroup ability used only on purpose
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCampStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Camp
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. <b>Plains</b>. Each time an Uruk-hai is played, that minion must exert.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Uruk Camp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachUrukHaiPlayedThereExertsOnceAndAnUrukHaiAlreadyInPlayIsNotExertedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowSite(2);
		var fighter = scn.GetShadowCard("fighter");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(fighter, sentry);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertSame(camp, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] Uruk-hai.
		scn.ShadowPlayCard(fighter);
		assertInZone(Zone.SHADOW_CHARACTERS, fighter);
		assertEquals(1, scn.GetWoundsOn(fighter));

		// A [sauron] Uruk-hai, the second played: it exerts, and the first is not exerted again.
		scn.ShadowPlayCard(sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(fighter));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionThatIsNoUrukHaiPlayedThereDoesNotExertAndNorDoesAnUrukHaiInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowSite(2);
		var fighter = scn.GetShadowCard("fighter");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(worker);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertSame(camp, scn.GetCurrentSite());
		scn.MoveMinionsToTable(fighter);
		assertEquals(0, scn.GetWoundsOn(fighter));

		// An [isengard] Orc, of the Fighter's culture: no exertion for it, nor for the Uruk-hai beside it.
		scn.ShadowPlayCard(worker);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(fighter));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
