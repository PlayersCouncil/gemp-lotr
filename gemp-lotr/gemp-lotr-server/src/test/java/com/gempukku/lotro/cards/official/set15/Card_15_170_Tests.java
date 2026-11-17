package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentry", "15_170");

					// [uruk-hai] minions.
					put("ugluk", "15_172");         // Uglúk, Ugly Fellow: Uruk-hai; fierce, hunter 3; his text acts in his skirmishes
					put("scout", "17_130");         // White Hand Scout: Uruk-hai; damage +1, hunter 2; [uruk-hai] minions played roam for 1 less
					put("ruthless", "11_200");      // Ruthless Uruk: Uruk-hai, none of the three keywords; an offer when assigned
					put("saruman", "17_116");       // Saruman, Master of the White Hand: a Wizard; each companion is resistance -1
					put("tracking", "15_171");      // Tracking Uruk: Uruk-hai; hunter 1 is its whole text
					put("decoy", "12_141");         // Dark Alliance: an [uruk-hai] condition; its response needs an [uruk-hai] lurker
					// Minions of other cultures.
					put("savage", "1_151");         // Uruk Savage: an [isengard] Uruk-hai; damage +1 is its whole text
					put("rider", "12_161");         // Black Rider: [wraith] Nazgul, site 3; fierce, and stronger for each companion
					put("berserker", "10_40");      // Easterling Berserker: [raider] Man; damage +2 while he is unwounded
					put("hillman", "12_60");        // Crazed Hillman: [men] Man, fierce, strength 7, vitality 1; stronger only in a fierce skirmish

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: none of the three keywords
					put("faramir", "18_48");        // Faramir, Captain of Ithilien: cost 3; ranger, hunter 2
					put("gimli", "1_13");           // Gimli, Son of Glóin: damage +1
					put("grimir", "1_17");          // Grimir, Dwarven Elder: a Dwarf ally with no keyword
					put("guile", "9_8");            // Ring of Guile: artifact, cost 0, "Bearer must be a Dwarf. Bearer is damage +1."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SentryUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Sentry Uruk
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: To play, spot an [uruk-hai] minion.
		 * 	While you can spot a fierce minion, this minion is <b>fierce</b>.
		 * 	While you can spot a hunter, this minion gains <b>hunter 1</b>.
		 * 	While you can spot a character that is damage +1, this minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");

		assertEquals("Sentry Uruk", sentry.getBlueprint().getTitle());
		assertNull(sentry.getBlueprint().getSubtitle());
		assertTrue(sentry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sentry.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, sentry.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sentry.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, sentry.getBlueprint().getRace());
		assertEquals(4, sentry.getBlueprint().getTwilightCost());
		assertEquals(10, sentry.getBlueprint().getStrength());
		assertEquals(3, sentry.getBlueprint().getVitality());
		assertEquals(5, sentry.getBlueprint().getSiteNumber());
	}

	@Test
	public void SentryUrukRequiresUrukHaiMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ugluk = scn.GetShadowCard("ugluk");
		var savage = scn.GetShadowCard("savage");
		var decoy = scn.GetShadowCard("decoy");

		scn.MoveCardsToHand(sentry, ugluk);
		scn.MoveCardsToSupportArea(decoy);

		scn.StartGame();
		// An Uruk-hai of another culture, and an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(17);
		scn.FreepsPass();

		// 17 set + 1 companion + 2 for the site
		assertEquals(20, scn.GetTwilight());
		assertInZone(Zone.HAND, sentry);
		assertFalse(scn.ShadowPlayAvailable(sentry));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 4 to play + 2 roaming
		scn.ShadowPlayCard(ugluk);
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(sentry));

		scn.ShadowPlayCard(sentry);
		// 4 to play + 2 roaming: its site number is 5 and this is site 2.
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedBesideAnUrukHaiCultureMinionWhoIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(sentry);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 companion + 2 for the site
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(sentry));

		scn.ShadowPlayCard(sentry);
		// 4 to play + 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		// Saruman is not fierce, no hunter, and has no damage bonus.
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFierceMinionPlayedMakesItFierceAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ruthless = scn.GetShadowCard("ruthless");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(rider);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, ruthless);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Beside a minion and two companions that have none of the three keywords.
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));

		scn.ShadowPlayCard(rider);
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));
		// "This minion": the other [uruk-hai] Uruk-hai is as it was.
		assertFalse(scn.HasKeyword(ruthless, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHunterCompanionPlayedGivesItHunter1AndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ruthless = scn.GetShadowCard("ruthless");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(faramir);

		scn.StartGame();
		// The minions are laid early, to read the Sentry before and after a companion is played.
		scn.MoveMinionsToTable(sentry, ruthless);
		scn.FreepsRefreshDecision();

		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(sentry, Keyword.HUNTER));

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertEquals(2, scn.GetKeywordCount(faramir, Keyword.HUNTER));
		// Hunter 1, whatever the number of the hunter spotted.
		assertTrue(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(ruthless, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BesideUglukItIsFierceAndHunter1AndWithAWhiteHandScoutDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ugluk = scn.GetShadowCard("ugluk");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, ugluk);

		// Uglúk is fierce and hunter 3, and has no damage bonus.
		assertTrue(scn.HasKeyword(ugluk, Keyword.FIERCE));
		assertEquals(3, scn.GetKeywordCount(ugluk, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));

		// A minion that is damage +1 (and hunter 2).
		scn.MoveMinionsToTable(scout);
		assertTrue(scn.HasKeyword(scout, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		// Two hunters in play: still hunter 1.
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
	}

	@Test
	public void AnAllyMadeDamagePlus1ByAnArtifactPlayedOnHimMakesItDamagePlus1AndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ruthless = scn.GetShadowCard("ruthless");
		var grimir = scn.GetFreepsCard("grimir");
		var guile = scn.GetFreepsCard("guile");
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(guile);

		scn.StartGame();
		// The minions are laid early, to read the Sentry before and after the artifact is played.
		scn.MoveMinionsToTable(sentry, ruthless);
		scn.FreepsRefreshDecision();

		assertFalse(scn.HasKeyword(grimir, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// Grimir is the only Dwarf: the Ring goes on him without a question.
		scn.FreepsPlayCard(guile);
		assertAttachedTo(guile, grimir);
		assertTrue(scn.HasKeyword(grimir, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(ruthless, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BesideACompanionWhoIsDamagePlus1ItIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
	}

	@Test
	public void BesideAMinionWhoIsDamagePlus2AndAnotherWhoIsDamagePlus1ItIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var berserker = scn.GetShadowCard("berserker");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, berserker);

		// A character whose damage bonus is +2 is a character with a damage bonus; the Sentry's own is +1.
		assertEquals(2, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// A second such character adds nothing.
		scn.MoveMinionsToTable(savage);
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
	}

	@Test
	public void ItSpotsItselfAndKeepsEachKeywordWhenTheOtherCardThatHasItLeavesPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var rider = scn.GetShadowCard("rider");
		var tracking = scn.GetShadowCard("tracking");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// A fierce minion, a hunter and a character that is damage +1: three cards, one keyword each.
		scn.MoveMinionsToTable(sentry, rider, tracking, savage);

		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// The fierce minion goes. Fierce by its own text, the Sentry is itself a fierce minion it can spot, and
		// stays fierce (Q-0276: "once it has bootstrapped itself it must continue spotting itself").
		scn.MoveCardsToDiscard(rider);
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// So for the hunter, and for the character that is damage +1: alone, it keeps all three.
		scn.MoveCardsToDiscard(tracking);
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		scn.MoveCardsToDiscard(savage);
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
	}

	@Test
	public void AFierceMinionKilledInTheFirstRoundLeavesItFierceAndItFightsTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var hillman = scn.GetShadowCard("hillman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, hillman);
		// Nothing asks whether the Sentry is fierce before the Hillman dies: the game keeps track of it itself.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hillman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn's 8 against the Hillman's 7: one wound, and his 1 vitality is gone.
		assertInZone(Zone.DISCARD, hillman);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// No other minion is fierce, and there is a fierce round: the Sentry's.
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, sentry);
		assertTrue(scn.IsCharAssignedAgainst(sentry, aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		// The Sentry's 10 against Aragorn's 8.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
