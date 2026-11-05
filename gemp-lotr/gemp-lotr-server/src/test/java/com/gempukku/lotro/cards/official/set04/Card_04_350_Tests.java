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

public class Card_04_350_Tests
{

	// The Towers path with Hornburg Courtyard as site 5.  Site 4 is White Rocks (Shadow number 3, +1 for each
	// mounted companion: there are none) and site 6 Caves of Aglarond (a maneuver ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Aragorn; a maneuver ability used only on purpose
					put("rohanaragorn", "17_93");// Aragorn, Defender of Rohan: the [rohan] Aragorn; text only while the Ring-bearer is assigned
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion who is no Aragorn; a skirmish ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site5", "4_350");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornburgCourtyardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hornburg Courtyard
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 5
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. While you can spot Aragorn, the Shadow number of Hornburg Courtyard is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Hornburg Courtyard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void WithAragornTheMoveToHornburgCourtyardAddsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(0);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		var courtyard = scn.GetCurrentSite();
		assertEquals("Hornburg Courtyard", courtyard.getBlueprint().getTitle());
		assertEquals(6, scn.GetTwilightCost(courtyard));
		// 2 companions + 8 - 2 for Hornburg Courtyard
		assertEquals(8, scn.GetTwilight());
		// White Rocks, site 4, keeps its own number.
		assertEquals(3, scn.GetTwilightCost(scn.GetSite(4)));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRohanAragornLowersItToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("rohanaragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		var courtyard = scn.GetCurrentSite();
		assertEquals("Hornburg Courtyard", courtyard.getBlueprint().getTitle());
		assertEquals(6, scn.GetTwilightCost(courtyard));
		// 2 companions + 8 - 2 for Hornburg Courtyard
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithACompanionWhoIsNoAragornTheMoveAddsTheFullEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		var courtyard = scn.GetCurrentSite();
		assertEquals("Hornburg Courtyard", courtyard.getBlueprint().getTitle());
		assertEquals(8, scn.GetTwilightCost(courtyard));
		// 2 companions + 8 for Hornburg Courtyard
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AragornPlayedAtHornburgCourtyardLowersItsShadowNumberByTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.SkipToSite(5);

		var courtyard = scn.GetCurrentSite();
		assertEquals("Hornburg Courtyard", courtyard.getBlueprint().getTitle());
		assertEquals(8, scn.GetTwilightCost(courtyard));

		scn.FreepsPlayCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(6, scn.GetTwilightCost(courtyard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromHornburgCourtyardItsNumberIsEightAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(5);

		var courtyard = scn.GetCurrentSite();
		assertEquals("Hornburg Courtyard", courtyard.getBlueprint().getTitle());
		assertEquals(6, scn.GetTwilightCost(courtyard));

		scn.SkipToSite(6);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(8, scn.GetTwilightCost(courtyard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
