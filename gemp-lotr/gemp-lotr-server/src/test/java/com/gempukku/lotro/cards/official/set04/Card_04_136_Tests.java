package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_136_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "4_136");
					put("patrol2", "4_136");      // a second copy: each copy's +4 is its own, and they do not add up

					put("savage", "1_151");       // Uruk Savage: another Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					// Southron Explorer: strength 5, vitality 3. "Regroup: Exert this minion and remove (5) to take
					// control of a site."
					put("explorer1", "4_250");
					put("explorer2", "4_250");
				}},
				// The Towers path, with Uruk Camp as site 2 ("Battleground. Plains. Each time an Uruk-hai is played,
				// that minion must exert": nobody is played here). Site 1, Eastemnet Gullies, has no keyword (its
				// fellowship ability is never used); site 3, Barrows of Edoras, is a sanctuary; site 4 is
				// Westemnet Hills, a plains and no battleground (it heals mounted characters, and nobody is mounted).
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AdvanceUrukPatrolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Advance Uruk Patrol
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While at a battleground, this minion is <b>fierce</b>.<br>While you control a battleground, this minion is strength +4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patrol");

		assertEquals("Advance Uruk Patrol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceWhileAtABattlegroundAndNotBeforeOrAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, savage);

		// At Eastemnet Gullies, no battleground.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(patrol, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		// The fellowship moves to Uruk Camp, a battleground: this minion is fierce, the other Uruk-hai is not.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(patrol, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// And on to Barrows of Edoras by the regroup move: no battleground, no longer fierce.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(patrol, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus4OnceItsOwnerTakesControlOfABattlegroundAndNotOfAnotherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		scn.SkipToSite(3);
		var gullies = scn.GetSite(1);
		var camp = scn.GetSite(2);
		scn.MoveMinionsToTable(patrol, savage, explorer1, explorer2);
		// The fellowship moves to Westemnet Hills, site 4; both markers have passed sites 1 and 2.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(10, scn.GetStrength(patrol));
		assertEquals(5, scn.GetStrength(savage));

		// Eastemnet Gullies is controlled first, and is no battleground.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer1);
		assertTrue(scn.ShadowControls(gullies));
		assertEquals(10, scn.GetStrength(patrol));

		// Then Uruk Camp, a battleground still in its controller's support area: +4 for this minion alone.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer2);
		assertTrue(scn.ShadowControls(camp));
		assertTrue(scn.HasKeyword(camp, Keyword.BATTLEGROUND));
		assertEquals(14, scn.GetStrength(patrol));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotStrengthPlus4WhileTheOtherPlayerControlsTheBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(3);
		var gullies = scn.GetSite(1);
		var camp = scn.GetSite(2);
		// The Free Peoples player took Uruk Camp on a turn of his as the Shadow player; this minion's owner
		// holds Eastemnet Gullies, no battleground.
		scn.FreepsTakeControlOfSite(camp);
		scn.ShadowTakeControlOfSite(gullies);
		scn.MoveMinionsToTable(patrol);

		assertTrue(scn.FreepsControls(camp));
		assertTrue(scn.HasKeyword(camp, Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowControls(gullies));
		assertEquals(10, scn.GetStrength(patrol));

		scn.FreepsPass();
		assertEquals(10, scn.GetStrength(patrol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABattlegroundItsOwnerControlsMakesItStrengthPlus4ButNotFierceAwayFromABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var patrol2 = scn.GetShadowCard("patrol2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		var camp = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(patrol, patrol2, savage);

		// Uruk Camp is in the Shadow player's support area; the fellowship is at Barrows of Edoras.
		assertTrue(scn.ShadowControls(camp));
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 10 + 4 each, not 10 + 8: a copy's text is about that copy alone.
		assertEquals(14, scn.GetStrength(patrol));
		assertEquals(14, scn.GetStrength(patrol2));
		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(patrol, Keyword.FIERCE));

		// Westemnet Hills, a plains and no battleground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(patrol, Keyword.FIERCE));
		assertEquals(14, scn.GetStrength(patrol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
