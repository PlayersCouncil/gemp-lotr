package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_248_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is his whole text
					// Narsil, Blade of the Faithful: [gondor] ARTIFACT, hand weapon, on a [gondor] Man; its bonus is
					// Elendil's or Isildur's, and its response is used only on purpose
					put("narsil", "9_34");
					// Bilbo, Retired Adventurer: [shire] Hobbit ally; a fellowship ability used only on purpose
					put("bilbo", "1_284");
					put("sword", "1_299");      // Hobbit Sword: [shire] POSSESSION, hand weapon, on a Hobbit; no other text
					// Faramir, Son of Denethor: Ring-bound [gondor] Man; his text bars the opponent's skirmish events
					put("faramir", "4_117");
					// Ranger's Bow: [gondor] possession, RANGED weapon, on a Ring-bound Man; makes no archer; its
					// skirmish ability is not reached here
					put("bow", "4_131");
					// Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, no damage bonus; his abilities are used
					// only on purpose
					put("ted", "18_75");
					// Henchman's Dagger: [men] Shadow possession, hand weapon, on a [men] minion; strength +1
					put("dagger", "18_69");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					// Moria Stairway has no site number: MoveCardToAdventurePath lays it as the site ahead.
					put("stairway", "11_248");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaStairwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Moria Stairway
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Underground</b>. Each character bearing a hand weapon is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("stairway");

		assertEquals("Moria Stairway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachCharacterBearingAHandWeaponIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var bilbo = scn.GetFreepsCard("bilbo");
		var faramir = scn.GetFreepsCard("faramir");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(knight, faramir);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(knight, scn.GetFreepsCard("narsil"));
		scn.AttachCardsTo(bilbo, scn.GetFreepsCard("sword"));
		scn.AttachCardsTo(faramir, scn.GetFreepsCard("bow"));

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("stairway"));
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals("Moria Stairway", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, scn.GetShadowCard("dagger"));
		scn.ShadowRefreshDecision();

		// A companion with an artifact hand weapon, an ally with a possession hand weapon, and a minion with a
		// Shadow one.
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(bilbo, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		// A ranged weapon is no hand weapon, and Frodo bears no weapon at all.
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyWhileTheFellowshipIsAtTheStairwayIsAnyoneDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(knight, scn.GetFreepsCard("narsil"));
		scn.AttachCardsTo(bilbo, scn.GetFreepsCard("sword"));

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("stairway"));

		// The Stairway lies ahead of the fellowship, at Bag End.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bilbo, Keyword.DAMAGE));

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(bilbo, Keyword.DAMAGE));

		// No minion is played: the game goes on to the regroup phase, and the fellowship moves on to site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bilbo, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
