package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_120_Tests
{

	// Valley of the Silverlode is site 6; the rest of the path is that of FellowshipSites
	// (site 7, Anduin Confluence, discards every ally as the fellowship moves there).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home 2F; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site6", "2_120");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValleyoftheSilverlodeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Valley of the Silverlode
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>River</b>. <b>Sanctuary</b>. When the fellowship moves to Valley of the Silverlode, each Hobbit companion may heal.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Valley of the Silverlode", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingToTheValleyEachHobbitCompanionMayHealAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, pippin, aragorn, smeagol);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(smeagol, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(smeagol));

		scn.FreepsPass();
		var valley = scn.GetSite(6);
		assertEquals("Valley of the Silverlode", valley.getBlueprint().getTitle());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valley));
		scn.FreepsAcceptOptionalTrigger();

		// The three Hobbit companions, of two cultures, may heal; the Hobbit ally and the companions who are no
		// Hobbits may not.  "May": none need be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, aragorn, smeagol));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(frodo, pippin);

		// Each one chosen heals once; Sam, not chosen, keeps both his wounds.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheHealMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.FreepsPass();
		var valley = scn.GetSite(6);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valley));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoHobbitCompanionWoundedTheMoveToTheValleyOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bounder);
		// A wounded Hobbit ally and a wounded companion who is no Hobbit; the Hobbit companions are unwounded.
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scn.GetSite(6)));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingOnFromTheValleyOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(6);
		var valley = scn.GetCurrentSite();
		assertEquals("Valley of the Silverlode", valley.getBlueprint().getTitle());
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 2);

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(valley));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
