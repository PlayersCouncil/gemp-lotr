package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_261_Tests
{

	// The Fellowship-block path with the Valley in place of site 9, which no test reaches: the Shadow player's copy
	// is put on the path as site 2 and the fellowship moves to it.  Site 3 is Rivendell Valley, a sanctuary that
	// heals only at the start of a turn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, vitality 4; an assignment ability
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2; a skirmish ability
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("valley", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValleyoftheSilverlodeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Valley of the Silverlode
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Dwelling</b>. <b>River</b>. When your fellowship moves to this site, you may heal each Hobbit companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("valley");

		assertEquals("Valley of the Silverlode", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void MovingToTheValleyOffersTheHealAndEachHobbitCompanionHealsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveCardToAdventurePath(valley);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(bounder));

		scn.FreepsPass();

		// The Shadow player's copy, and the offer is the moving player's.
		assertSame(valley, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valley));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(valley));
		scn.FreepsAcceptOptionalTrigger();

		// A Hobbit companion of each culture heals one wound, with nothing more to choose; the companion who is no
		// Hobbit and the Hobbit who is no companion keep theirs.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheHealLeavesEveryWoundWhereItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.MoveCardToAdventurePath(valley);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(pippin, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(pippin));

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valley));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoHobbitCompanionWoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveCardToAdventurePath(valley);
		// Only the companion who is no Hobbit and the Hobbit who is no companion are wounded.
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(bounder));

		scn.FreepsPass();

		assertSame(valley, scn.GetCurrentSite());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(valley));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromTheValleyHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.MoveCardToAdventurePath(valley);
		scn.FreepsPass();

		assertSame(valley, scn.GetCurrentSite());
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(pippin, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(pippin));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(valley));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
