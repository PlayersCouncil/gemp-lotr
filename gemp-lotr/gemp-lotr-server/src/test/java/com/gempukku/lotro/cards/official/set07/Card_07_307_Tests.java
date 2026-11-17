package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_307_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalker", "7_307");
					put("stalker2", "7_307");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, site 6, strength 7; his text answers a weapon played on him
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc, site 5; his text works only while he is stacked
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai), site 6; damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), site 4; his text needs 2 wounds on the Ring-bearer
					put("stairs", "1_24");      // Stairs of Khazad-dum: a Free Peoples condition of cost 0, played only to take the hand from four to three
					put("hand1", "1_7");        // Dwarf Guard: cards for the Free Peoples hand, which decides initiative
					put("hand2", "1_7");
					put("hand3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcStalkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Stalker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.
		 * The site number of each [sauron] Orc is -1.
		 * While you have initiative, this minion is strength +8.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stalker");

		assertEquals("Orc Stalker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheSiteNumberOfEachSauronOrcIsMinusOneItsOwnIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager, sentry, hound);

		// Before the Stalker is in play: printed site numbers.
		assertEquals(6, scn.GetMinionSiteNumber(stalker));
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertEquals(5, scn.GetMinionSiteNumber(pillager));
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));

		scn.MoveMinionsToTable(stalker);

		assertEquals(5, scn.GetMinionSiteNumber(stalker));
		assertEquals(5, scn.GetMinionSiteNumber(chieftain));
		assertEquals(4, scn.GetMinionSiteNumber(pillager));
		// The [sauron] Uruk-hai and the [wraith] Orc are no [sauron] Orcs.
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite5ASauronOrcOfSiteNumber6RoamsNoLongerOnceTheStalkerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(chieftain, sentry);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));

		scn.MoveMinionsToTable(stalker);

		// 6 - 1 is 5, the fellowship's site: the Chieftain is not roaming; the [sauron] Uruk-hai still is.
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(stalker, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASauronOrcPlayedAtSite5BesideTheStalkerPaysNoRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(stalker);
		scn.MoveCardsToHand(chieftain);
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		// The Chieftain's site number is 5 at site 5: his cost of 2 and no roaming penalty of 2.
		assertTrue(scn.ShadowPlayAvailable(chieftain));

		scn.ShadowPlayCard(chieftain);

		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileTheShadowHasInitiativeTheStalkerIsStrengthPlus8AndNoOtherOrcIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker");
		var stalker2 = scn.GetShadowCard("stalker2");
		var chieftain = scn.GetShadowCard("chieftain");
		var stairs = scn.GetFreepsCard("stairs");

		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.StartGame();
		scn.MoveMinionsToTable(stalker, stalker2, chieftain);
		scn.FreepsRefreshDecision();

		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(stalker));
		assertEquals(8, scn.GetStrength(stalker2));
		assertEquals(7, scn.GetStrength(chieftain));

		scn.FreepsPlayCard(stairs);

		// Three left: the Shadow has initiative. Each Stalker is +8 for itself alone, and the Chieftain is not.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(16, scn.GetStrength(stalker));
		assertEquals(16, scn.GetStrength(stalker2));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
