package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_358_Tests
{

	// The King path, Morgulduin its site 8.  Site 7 is Osgiliath Channel (a regroup ability that needs 6
	// companions) and site 9 Dagorlad.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, cost 5, site number 2; fierce and
					// damage +1 only at 2 burdens or 2 wounds on the Ring-bearer.
					put("nelya", "2_84");
					// Ulaire Attea, Khamul of the East: [raider] Nazgûl, cost 6, site number 3; his bonuses need 3
					// burdens.
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc, cost 2; a bonus at 2 wounds on the Ring-bearer.
					put("hound", "7_192");
					// Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl of a title no other card here has, for the
					// other player's discard pile.
					put("lemenya", "1_232");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, so that threats can lie
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: a companion; strength +3 only against a Nazgûl
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_336");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "7_358");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulduinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgulduin
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Right
		 * Game Text: <b>Shadow:</b> Remove 2 threats to play a Nazgûl from your discard pile. His twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Morgulduin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowActionRemovesTwoThreatsToPlayANazgulFromTheShadowPlayersDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable("aragorn", "arwen");

		scn.StartGame();
		scn.SkipToSite(7);
		// The cards the skips dealt about are put where the test wants them.
		scn.MoveCardsToDiscard(nelya, khamul, hound, scn.GetFreepsCard("lemenya"));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("lemenya"));
		scn.RemoveThreats(scn.GetThreats());
		scn.AddThreats(3);
		scn.FreepsPass();

		var morgulduin = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("Morgulduin", morgulduin.getBlueprint().getTitle());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetThreats());

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(morgulduin));
		assertInDiscard(nelya);
		scn.ShadowUseCardAction(morgulduin);
		assertEquals(1, scn.GetThreats());

		// Two Nazgûl of two cultures in his own discard pile are offered; a [wraith] Orc there is not, nor a
		// Nazgûl in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, scn.GetFreepsCard("lemenya")));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		// 10 less (5 - 2); site number 2 at site 8, so no roaming.
		assertEquals(7, scn.GetTwilight());
		assertInDiscard(khamul, hound);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithOneThreatAndIsWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable("aragorn", "arwen");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(nelya);
		scn.RemoveThreats(scn.GetThreats());
		scn.AddThreats(1);
		scn.FreepsPass();

		var morgulduin = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(morgulduin));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(morgulduin));
		assertInDiscard(nelya);
		scn.ShadowUseCardAction(morgulduin);

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedUntilThePoolCoversTheNazgulLessTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable("aragorn");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(nelya);
		scn.RemoveThreats(scn.GetThreats());
		scn.AddThreats(2);
		scn.FreepsPass();

		var morgulduin = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetThreats());

		// Nelya costs 5 - 2 = 3.  At 2 (enough for a discount of 3) the action is not offered.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(morgulduin));

		// At 3 (too little for a discount of 1) it is.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(morgulduin));
		assertInDiscard(nelya);
		scn.ShadowUseCardAction(morgulduin);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
