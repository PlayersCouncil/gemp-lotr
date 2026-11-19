package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_045_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The assignment tests
	// are played at Rivendell (site 3: a sanctuary where Trolls cannot be played), reached by SkipToSite(2) and the
	// move, so that Elrond stands at his home site.  A doubt is the Hobbit game's burden.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "32_45");

					// Orkish Aggressor: [gundabad] Orc, 6/2; an assignment ability of its own, not used.
					put("aggressor", "30_39");
					// Yazneg, Orkish Assassin: unique [gundabad] Orc, 5/2; a maneuver ability, not used.
					put("yazneg", "30_41");
					// Bill Ferny Sr., Servant of Azog: [gundabad] Man, 4/2.  The Free Peoples player may not assign a
					// character to skirmish him, and he may not skirmish Bilbo; a regroup ability, not used.
					put("ferny", "33_22");
					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire.
					put("runner", "31_30");
					// Orkish Veteran: [sauron] Orc, 7/2; an archer text at site 9 and a regroup ability.
					put("veteran", "32_43");
					// Úlairë Nelya, Revived: [wraith] Nazgûl, 10/3, fierce; damage +1 only while a companion bears a
					// follower (none is laid).
					put("nelya", "32_63");
					// He Is Summoning His Servants: a [wraith] condition (no minion) in the support area; each
					// [wraith] minion is strength +2 while 2 Wise characters cannot be spotted, and a Shadow ability.
					put("summoning", "32_58");

					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability.
					put("balin", "30_2");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, 7/4; Thorin costs 2 less, and he cannot
					// be discarded beside 2 [dwarven] followers (none is laid).
					put("gandalf", "30_26");
					// Elrond, Elven Lord: [elven] Elf ally, home site 3; a start-of-turn heal and a regroup ability.
					put("elrond", "30_21");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Sauron, The Necromancer
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Maia
		 * Strength: 13
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: Each [gundabad] Orc is strength +1 and <b>fierce</b>.<br><b>Assignment:</b> Exert Sauron to assign a [gundabad] or [wraith] minion to a companion (except Bilbo). The Free Peoples player may add 2 doubts or exert that companion to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sauron");

		assertEquals("Sauron", card.getBlueprint().getTitle());
		assertEquals("The Necromancer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAIA, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachGundabadOrcIsStrengthPlus1AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, yazneg, ferny, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, yazneg, ferny, runner);

		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(yazneg, Keyword.FIERCE));

		scn.MoveMinionsToTable(sauron);
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);

		// Two [gundabad] Orcs, one unique and one not.
		assertEquals(7, scn.GetStrength(aggressor));
		assertEquals(6, scn.GetStrength(yazneg));
		assertTrue(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(yazneg, Keyword.FIERCE));
		// A [gundabad] Man, a [moria] Orc and Sauron himself: neither.
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(sauron));
		assertFalse(scn.HasKeyword(ferny, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.FIERCE));
	}

	@Test
	public void AssignmentActionExertsSauronToAssignAGundabadOrWraithMinionToACompanionOtherThanBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var ferny = scn.GetShadowCard("ferny");
		var nelya = scn.GetShadowCard("nelya");
		var veteran = scn.GetShadowCard("veteran");
		var summoning = scn.GetShadowCard("summoning");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(balin, gandalf);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin, gandalf);
		assertInZone(Zone.SUPPORT, elrond);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron, aggressor, ferny, nelya, veteran);
		scn.MoveCardsToSupportArea(summoning);
		assertInZone(Zone.SUPPORT, summoning);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, summoning);

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(sauron));

		scn.ShadowUseCardAction(sauron);
		assertEquals(1, scn.GetWoundsOn(sauron));

		// A [dwarven] and a [gandalf] companion; not Bilbo, nor an ally at his home site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(balin, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, elrond));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(balin);

		// [gundabad] Orc and Man, and a [wraith] Nazgûl; not a [sauron] Orc, Sauron, or a [wraith] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, ferny, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(veteran, sauron, summoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(balin, nelya));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddTwoDoubtsToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin, gandalf);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin, gandalf);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron, aggressor);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowUseCardAction(sauron);
		assertEquals(1, scn.GetWoundsOn(sauron));
		scn.ShadowChooseCard(gandalf);
		// The Aggressor, the one minion of the kind, is taken unasked.

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		// Both can be paid, and he is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Add 2 doubts"));
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		scn.FreepsChoose("Add 2 doubts");

		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(aggressor));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// Sauron's exertion stays paid.
		assertEquals(1, scn.GetWoundsOn(sauron));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayExertThatCompanionToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin, gandalf);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, balin, gandalf);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron, aggressor);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowUseCardAction(sauron);
		assertEquals(1, scn.GetWoundsOn(sauron));
		scn.ShadowChooseCard(gandalf);
		// The Aggressor, the one minion of the kind, is taken unasked.

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		// Both can be paid, and he is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Add 2 doubts"));
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.FreepsChoose("Exert");

		// The companion chosen exerts, and not another.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(aggressor));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(1, scn.GetWoundsOn(sauron));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithThatCompanionExhaustedOnlyTheDoubtsCanPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var ferny = scn.GetShadowCard("ferny");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin, gandalf);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron, ferny);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowUseCardAction(sauron);
		scn.ShadowChooseCard(balin);
		// Bill Ferny Sr., the one minion of the kind, is taken unasked.

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The doubts are what is left, and they are added with nothing more asked.
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(balin));
		assertFalse(scn.IsCharAssigned(ferny));
		assertFalse(scn.IsCharAssigned(balin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentActionIsNotOfferedWhileSauronIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron, aggressor);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(sauron, 3);
		assertEquals(3, scn.GetWoundsOn(sauron));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(3, scn.GetWoundsOn(sauron));

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sauron));
	}

	@Test
	public void WithNoGundabadOrWraithMinionTheActionIsPaidForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var veteran = scn.GetShadowCard("veteran");
		var summoning = scn.GetShadowCard("summoning");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron, veteran);
		scn.MoveCardsToSupportArea(summoning);
		assertInZone(Zone.SUPPORT, summoning);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertInZone(Zone.SUPPORT, summoning);
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertTrue(scn.ShadowActionAvailable(sauron));

		scn.ShadowUseCardAction(sauron);

		assertEquals(1, scn.GetWoundsOn(sauron));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertFalse(scn.IsCharAssigned(balin));
		assertFalse(scn.IsCharAssigned(veteran));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionButBilboTheActionIsPaidForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var bilbo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elrond);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron, aggressor);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertTrue(scn.ShadowActionAvailable(sauron));

		scn.ShadowUseCardAction(sauron);

		assertEquals(1, scn.GetWoundsOn(sauron));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertFalse(scn.IsCharAssigned(bilbo));
		assertFalse(scn.IsCharAssigned(elrond));
		assertFalse(scn.IsCharAssigned(aggressor));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
