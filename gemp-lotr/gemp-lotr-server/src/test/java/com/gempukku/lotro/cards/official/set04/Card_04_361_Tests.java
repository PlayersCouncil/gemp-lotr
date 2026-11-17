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

public class Card_04_361_Tests
{

	// Orthanc Balcony is site 9 of a Towers path.  Each test skips to site 8, lays the board there and moves on, so
	// that it acts in the Shadow phase at site 9.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Saruman, Black Traitor: [isengard] Wizard minion, cost 4, site 4; "When you play Saruman, you may
					// discard a condition" (none in play, so not offered)
					put("saruman", "4_173");
					// Saruman, Rabble-rouser: [dunland] Wizard minion, cost 4, site 4; his move trigger counts [dunland] Men
					put("rabble", "4_33");
					// Saruman, Servant of Sauron: [isengard] follower, cost 1; an Aid and a skirmish-win trigger only
					put("follower", "17_38");
					// Saruman, Keeper of Isengard: [isengard] Wizard minion; laid in the Shadow player's hand
					put("keeper", "3_68");
					// Saruman, Master of Foul Folk: [isengard] Wizard minion; laid only in the other player's discard pile
					put("foulfolk", "5_56");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; Damage +1 is its whole text
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6; no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site9", "4_361");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrthancBalconyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Orthanc Balcony
		 * Unique: False
		 * Shadow Number: 8
		 * Type: Site
		 * Block: Towers
		 * Direction: Left
		 * Site Number: 9T
		 * Game Text: <b>Battleground</b>. <b>Shadow:</b> Play Saruman from your discard pile; his twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Orthanc Balcony", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void PlaysSarumanFromTheShadowPlayersDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var rabble = scn.GetShadowCard("rabble");
		var keeper = scn.GetShadowCard("keeper");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var freepsFoulFolk = scn.GetFreepsCard("foulfolk");

		scn.StartGame();
		scn.SkipToSite(8);
		// The turns skipped have drawn cards: every pile is laid now. A pile's choice is read by blueprint, so the
		// other player's Saruman in his own discard pile is of a blueprint that lies in no pile of this player's.
		scn.MoveCardsToDiscard(saruman, rabble, savage, freepsFoulFolk);
		scn.MoveCardsToHand(keeper, savage2);
		scn.SetTwilight(10);
		assertInZone(Zone.DISCARD, saruman, rabble, savage, freepsFoulFolk);
		assertInHand(keeper, savage2);
		scn.FreepsPass();

		var balcony = scn.GetCurrentSite();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals("Orthanc Balcony", balcony.getBlueprint().getTitle());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(balcony));
		scn.ShadowUseCardAction(balcony);

		// Sarumen of two cultures in his discard pile; not a minion of another title there, a Saruman in his hand,
		// or the other player's Saruman in that player's own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, rabble));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, keeper, freepsFoulFolk));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(saruman);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertInZone(Zone.DISCARD, rabble, savage, freepsFoulFolk);
		// 4 printed - 2; site 4, so no roaming at site 9.
		assertEquals(pool - 2, scn.GetTwilight());

		// Only that play is cheaper: a minion played from hand next pays its whole cost of 2.
		scn.ShadowPlayCard(savage2);
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertEquals(pool - 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysTheSarumanFollowerFromTheDiscardPileAtNoCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var follower = scn.GetShadowCard("follower");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToDiscard(follower, savage);
		scn.SetTwilight(3);
		assertInDiscard(follower, savage);
		scn.FreepsPass();

		var balcony = scn.GetCurrentSite();
		assertEquals("Orthanc Balcony", balcony.getBlueprint().getTitle());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(balcony));
		scn.ShadowUseCardAction(balcony);

		// The follower is the one Saruman there: played unasked, to the Shadow player's support area. His cost of 1
		// less 2 is 0 (a cost never reads below 0).
		assertInZone(Zone.SUPPORT, follower);
		assertInDiscard(savage);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoSarumanInTheDiscardPileOrAPoolShortOfHisLoweredCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var keeper = scn.GetShadowCard("keeper");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		// No Saruman in the discard pile: a minion of another title there, a Saruman in hand.
		scn.MoveCardsToDiscard(savage);
		scn.MoveCardsToHand(keeper);
		scn.SetTwilight(10);
		assertInDiscard(savage);
		assertInHand(keeper);
		scn.FreepsPass();

		var balcony = scn.GetCurrentSite();
		assertEquals("Orthanc Balcony", balcony.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInDiscard(savage);
		assertInHand(keeper);
		assertFalse(scn.ShadowActionAvailable(balcony));

		// Saruman (4) in the discard pile costs 2 from here: with (1) he cannot be played, with (2) he can.
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(saruman);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertInDiscard(saruman);
		assertFalse(scn.ShadowActionAvailable(balcony));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(balcony));
	}

	@Test
	public void IsAShadowPhaseActionAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToDiscard(saruman);
		scn.SetTwilight(10);
		assertInDiscard(saruman);
		scn.FreepsPass();

		var balcony = scn.GetCurrentSite();
		assertEquals("Orthanc Balcony", balcony.getBlueprint().getTitle());
		assertTrue(scn.ShadowActionAvailable(balcony));
		assertInDiscard(saruman);
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(balcony));
		assertInDiscard(saruman);
	}
}
