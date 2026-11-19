package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_332_Tests
{

	// Midgewater Marshes is site 2; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion; a bonus with a weapon only
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4; a fellowship ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home site 2; a skirmish ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_332");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Midgewater Marshes is site 2; the rest is the usual Fellowship path.
	protected VirtualTableScenario GetMarshesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a second Hobbit; his text needs a weapon
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_332");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MidgewaterMarshesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Midgewater Marshes
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Marsh</b>. Each time a Hobbit moves to Midgewater Marshes, that Hobbit must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Midgewater Marshes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
	}

	@Test
	public void EveryHobbitCompanionWhoMovesToMidgewaterMarshesExertsOnceAndNoOtherCharacterDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		// The other player's Ring-bearer, a Hobbit of his own fellowship, which does not move this turn.
		var shadowFrodo = scn.GetShadowRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, sam, smeagol);
		scn.MoveAlliesToTable(bounder);
		// Vitality 4 with 3 wounds: Sam is exhausted, and cannot exert (a wound would kill him).
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));

		scn.FreepsPass();

		// The Hobbits who moved, of two cultures and the Ring-bearer among them, exert once each, unasked.
		assertEquals("Midgewater Marshes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		// The exhausted Hobbit cannot exert, so nothing happens to him.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		// A [shire] companion who is no Hobbit; a Hobbit ally, who does not move with the fellowship though this
		// is his home site; the other player's Hobbit, who is not moving.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from Midgewater Marshes to site 3: nobody exerts.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachHobbitMovingToMidgewaterMarshesExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshesScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals("Midgewater Marshes", scn.GetCurrentSite().getBlueprint().getTitle());
		// Each Hobbit has exerted once, and the Man not at all.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheOtherPlayersFellowshipMovesToMidgewaterMarshesOnHisTurnHisHobbitsExertAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var shadowFrodo = scn.GetShadowRingBearer();

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("Midgewater Marshes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));

		// The first player's fellowship stays at Midgewater Marshes; the second player's turn begins at site 1.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));

		scn.ShadowPass();

		// The same site is under his fellowship now: his Hobbit exerts; the first player's, who stands there
		// and does not move, does not.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals("Midgewater Marshes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetWoundsOn(shadowFrodo));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
