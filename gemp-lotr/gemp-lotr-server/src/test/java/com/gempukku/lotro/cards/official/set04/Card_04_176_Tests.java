package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ugluk", "4_176");
					// Uruk Seeker: [isengard] Uruk-hai tracker, cost 2, strength 6, vitality 2, site 5, fierce; its
					// bonus needs a character bearing a search card
					put("seeker", "4_195");
					// Uruk Pursuer: [isengard] Uruk-hai tracker, cost 2, strength 5, vitality 3, site 5, fierce; its
					// texts are about unbound Hobbits
					put("pursuer", "4_190");
					// Uruk Savage: an [isengard] Uruk-hai who is no tracker, cost 2, site 5; Damage +1 is its whole text
					put("savage", "1_151");
					// Orc Searcher: a [sauron] Orc tracker, cost 2, site 6 made 5 by its own text; its skirmish ability
					// is used only on purpose
					put("searcher", "102_64");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					// used only on purpose
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UglukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uglúk, Servant of Saruman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Tracker</b>. <b>Fierce</b>.<br>The roaming penalty for each [isengard] tracker you play is -2.<br>While you can spot 2 [isengard] trackers, Uglúk is strength +3.<br>While you can spot 3 [isengard] trackers, Uglúk is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ugluk");

		assertEquals("Uglúk", card.getBlueprint().getTitle());
		assertEquals("Servant of Saruman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsengardTrackersPlayedAfterHimPayNoRoamingPenaltyAndEachOneBringsHisBonusesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ugluk = scn.GetShadowCard("ugluk");
		var seeker = scn.GetShadowCard("seeker");
		var pursuer = scn.GetShadowCard("pursuer");
		var savage = scn.GetShadowCard("savage");
		var searcher = scn.GetShadowCard("searcher");
		scn.MoveCardsToHand(ugluk, savage, searcher, seeker, pursuer);

		scn.StartGame();
		assertInZone(Zone.HAND, ugluk, savage, searcher, seeker, pursuer);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Site 2: every one of these minions is roaming, and the penalty is 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 20 set + 1 companion + 2 for Ettenmoors
		assertEquals(23, scn.GetTwilight());

		scn.ShadowPlayCard(ugluk);
		// 4 to play + 2 roaming: his own text is not in force while he is being played
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.HasKeyword(ugluk, Keyword.ROAMING));
		// One [isengard] tracker, himself
		assertEquals(9, scn.GetStrength(ugluk));
		assertEquals(0, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));

		scn.ShadowPlayCard(savage);
		// An [isengard] minion who is no tracker: 2 to play + 2 roaming
		assertEquals(13, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(ugluk));

		scn.ShadowPlayCard(searcher);
		// A tracker of another culture: 2 to play + 2 roaming
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.HasKeyword(searcher, Keyword.ROAMING));
		assertEquals(9, scn.GetStrength(ugluk));
		assertEquals(0, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));

		scn.ShadowPlayCard(seeker);
		// 2 to play + 2 roaming - 2.  The penalty is gone; the minion is no less roaming.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.HasKeyword(seeker, Keyword.ROAMING));
		// Two [isengard] trackers: strength +3, and no damage bonus yet
		assertEquals(12, scn.GetStrength(ugluk));
		assertEquals(0, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));

		scn.ShadowPlayCard(pursuer);
		// 2 to play + 2 roaming - 2
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.HasKeyword(pursuer, Keyword.ROAMING));
		// Three [isengard] trackers are enough for both
		assertEquals(12, scn.GetStrength(ugluk));
		assertEquals(1, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));
		assertInZone(Zone.SHADOW_CHARACTERS, ugluk, savage, searcher, seeker, pursuer);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardTrackerWhoIsNotRoamingPaysHisPrintedCostAndNoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ugluk = scn.GetShadowCard("ugluk");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();

		// Site 5, the site number of the Seeker: he pays no roaming penalty for Ugluk to make less.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(ugluk);
		scn.SetShadowHand(seeker);
		assertInZone(Zone.SHADOW_CHARACTERS, ugluk);
		assertInZone(Zone.HAND, seeker);

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(seeker));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(seeker));

		scn.ShadowPlayCard(seeker);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(seeker, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeIsengardTrackersHeBeatsAragornAndPlacesTwoWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ugluk = scn.GetShadowCard("ugluk");
		var seeker = scn.GetShadowCard("seeker");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk, seeker, pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ugluk);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(12, scn.GetStrength(ugluk));
		assertEquals(1, scn.GetKeywordCount(ugluk, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 12 against 8, short of double: one wound for the win and one for damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(ugluk));
		// Ugluk is fierce.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
