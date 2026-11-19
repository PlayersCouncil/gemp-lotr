package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// White Hand Scout: cost 2, site number 5
					put("scout1", "17_130");
					put("scout2", "17_130");   // not unique: the penalty is lowered once for each copy in play
					put("scout3", "17_130");
					// White Hand Butcher: an [uruk-hai] Uruk-hai, cost 4, site number 5; Hunter 6 is its whole text
					put("butcher1", "17_122");
					put("butcher2", "17_122");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard), cost 4,
					// site number 4; each companion resistance -1, and a skirmish ability
					put("saruman", "17_116");
					// Tracking Uruk: an [uruk-hai] Uruk-hai, cost 5, site number 5; Hunter 1 is its whole text
					put("tracker", "15_171");
					// Uruk Savage: an [isengard] Uruk-hai, cost 2, site number 5; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Hunter 2</b>. <helper>(While skirmishing a non-hunter character, this character is strength +2.)</helper><br>The roaming penalty for each [uruk-hai] minion you play is -1.
		*/

		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout1");

		assertEquals("White Hand Scout", scout.getBlueprint().getTitle());
		assertNull(scout.getBlueprint().getSubtitle());
		assertFalse(scout.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, scout.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, scout.getBlueprint().getCulture());
		assertEquals(CardType.MINION, scout.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, scout.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(scout, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(scout, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(scout, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(scout, Keyword.HUNTER));
		assertEquals(2, scout.getBlueprint().getTwilightCost());
		assertEquals(5, scout.getBlueprint().getStrength());
		assertEquals(3, scout.getBlueprint().getVitality());
		assertEquals(5, scout.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheNextUrukHaiMinionPaysOneLessForRoamingAndTheScoutItselfPaysThePenaltyInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var scout3 = scn.GetShadowCard("scout3");
		var butcher1 = scn.GetShadowCard("butcher1");
		var butcher2 = scn.GetShadowCard("butcher2");
		var saruman = scn.GetShadowCard("saruman");
		var tracker = scn.GetShadowCard("tracker");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(scout1, scout2, scout3, butcher1, butcher2, saruman, tracker, savage);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		// Site 2: every minion here is roaming, and the penalty is 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 40 set + 1 companion + 2 for Ettenmoors
		assertEquals(43, scn.GetTwilight());

		scn.ShadowPlayCard(scout1);
		// 2 to play + 2 roaming: its own text is not in force while it is being played
		assertEquals(39, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, scout1);

		scn.ShadowPlayCard(butcher1);
		// An [uruk-hai] Uruk-hai: 4 to play + 2 roaming - 1 for the Scout.  The penalty is less; the minion is no less roaming.
		assertEquals(34, scn.GetTwilight());
		assertTrue(scn.HasKeyword(butcher1, Keyword.ROAMING));

		scn.ShadowPlayCard(saruman);
		// An [uruk-hai] minion who is no Uruk-hai, roaming below his site 4: 4 to play + 2 roaming - 1
		assertEquals(29, scn.GetTwilight());
		assertTrue(scn.HasKeyword(saruman, Keyword.ROAMING));

		scn.ShadowPlayCard(savage);
		// An [isengard] Uruk-hai: 2 to play + 2 roaming
		assertEquals(25, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		scn.ShadowPlayCard(scout2);
		// 2 to play + 2 roaming - 1 for the first Scout, and nothing for itself
		assertEquals(22, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, scout2);

		scn.ShadowPlayCard(tracker);
		// Two Scouts in play: 5 to play + 2 roaming - 2
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.HasKeyword(tracker, Keyword.ROAMING));

		scn.ShadowPlayCard(scout3);
		// 2 to play + 2 roaming - 2
		assertEquals(15, scn.GetTwilight());

		scn.ShadowPlayCard(butcher2);
		// Three Scouts in play take the penalty of 2 to nothing and no lower: 4 to play, not 4 - 1
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.HasKeyword(butcher2, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, butcher2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionWhoIsNotRoamingPaysHisPrintedCostAndNoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var butcher1 = scn.GetShadowCard("butcher1");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();

		// Site 5, the site number of the Butcher: he pays no roaming penalty for the Scout to make less.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(scout1);
		scn.SetShadowHand(butcher1);

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(butcher1));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(butcher1));

		scn.ShadowPlayCard(butcher1);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(butcher1, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, butcher1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
