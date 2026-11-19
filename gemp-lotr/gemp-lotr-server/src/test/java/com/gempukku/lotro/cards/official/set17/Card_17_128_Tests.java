package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("invader", "17_128");
					put("invader2", "17_128");
					put("butcher", "17_122");    // White Hand Butcher: [uruk-hai] Uruk-hai, cost 4, site 5; hunter 6 is its whole text
					put("troop", "1_177");       // Goblin Patrol Troop: a minion with no text at all, vitality 3

					put("knight", "5_35");       // Gondorian Knight: [gondor] Man companion, vitality 3; knight and no other text
					put("lorien", "1_53");       // Lórien Elf: [elven] Elf companion, vitality 2; its text is only about its play
					put("bounder", "1_286");     // Bounder: a [shire] Hobbit ally; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandInvaderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Invader
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, exert a companion for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("invader");

		assertEquals("White Hand Invader", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
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
	public void WithTwoSitesOfHisOwnItExertsTwiceAndTheSameCompanionMayBeChosenBothTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var invader = scn.GetShadowCard("invader");
		var troop = scn.GetShadowCard("troop");
		var knight = scn.GetFreepsCard("knight");
		var lorien = scn.GetFreepsCard("lorien");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(knight, lorien);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(invader);
		assertInHand(invader);

		scn.StartGame();
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		// Site 3 is the Free Peoples player's, as if taken on one of the other player's turns: not counted.
		scn.FreepsTakeControlOfSite(site3);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsControls(site3));
		// Vitality 2: one wound leaves the Lórien Elf exhausted.
		scn.AddWoundsToChar(lorien, 1);
		assertEquals(1, scn.GetWoundsOn(lorien));
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.ShadowPlayCard(invader);

		// No "may": the Shadow player chooses a companion and must choose one.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invader));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lorien, bounder, troop, invader));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));

		// The second site: the Knight may be exerted again (CR 4.2 "for each").
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, knight));
		scn.ShadowChooseCard(knight);
		assertEquals(2, scn.GetWoundsOn(knight));

		// Two, not three: the Free Peoples player's site is not his.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneSiteItExertsOnceAndAnotherMinionPlayedAfterItExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var invader = scn.GetShadowCard("invader");
		var invader2 = scn.GetShadowCard("invader2");
		var butcher = scn.GetShadowCard("butcher");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(invader, invader2, butcher);
		assertInHand(invader);
		assertInHand(invader2);
		assertInHand(butcher);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.ShadowPlayCard(invader);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, knight));
		scn.ShadowChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [uruk-hai] minion played with the Invader in play: no one exerts.
		scn.ShadowPlayCard(butcher);
		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: its own site count answers its own play, once, and the first Invader adds nothing.
		scn.ShadowPlayCard(invader2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, knight));
		scn.ShadowChooseCard(knight);
		assertInZone(Zone.SHADOW_CHARACTERS, invader2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
