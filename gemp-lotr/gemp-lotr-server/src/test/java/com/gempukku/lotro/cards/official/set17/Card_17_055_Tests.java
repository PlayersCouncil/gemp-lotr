package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "17_55");
					put("guard2", "17_55");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, cost 3, site 4; a maneuver ability never used
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), cost 4, site 4; "Each [men] minion
					// bearing a possession is strength +2" and a skirmish ability, neither reached here
					put("saruman", "17_48");
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, cost 2; a regroup ability only
					// Easterling Runner: [men] Man; a Shadow ability that needs a culture token, never used
					put("runner", "13_90");
					put("squad", "11_99");      // Squad of Haradrim: [men] Man, to bear the Dagger; a regroup ability only
					put("dagger", "18_69");     // Henchman's Dagger: a [men] hand weapon on a [men] minion, no other text
					// Fleet of Corsair Ships: a [men] possession in the support area; a play trigger and a regroup
					// ability, neither reached here
					put("fleet", "18_66");
					// Pass of the Spider: a [gollum] possession in the support area, which stacks any minion from play;
					// its texts want Shelob or a Free Peoples condition, neither laid
					put("pass", "103_12");

					// Companions with nothing in the way: Sam 1_311 (a fellowship ability and a response to Frodo's
					// death), Aragorn 1_89 (a maneuver ability never used), Gimli 1_13 and Boromir 1_97 (skirmish
					// abilities), Merry 1_303 (strength +2 while bearing a weapon)
					put("sam", "1_311");
					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("boromir", "1_97");
					put("merry", "1_303");
					put("bounder", "1_286");    // Bounder: a Hobbit ally, no companion; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Exert this minion to play a [men] minion (or 2 if you spot 6 companions) from a [men] possession as if from hand.
		*/

		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");

		assertEquals("Sunland Guard", guard.getBlueprint().getTitle());
		assertNull(guard.getBlueprint().getSubtitle());
		assertFalse(guard.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, guard.getBlueprint().getSide());
		assertEquals(Culture.MEN, guard.getBlueprint().getCulture());
		assertEquals(CardType.MINION, guard.getBlueprint().getCardType());
		assertEquals(Race.MAN, guard.getBlueprint().getRace());
		assertEquals(3, guard.getBlueprint().getTwilightCost());
		assertEquals(8, guard.getBlueprint().getStrength());
		assertEquals(2, guard.getBlueprint().getVitality());
		assertEquals(4, guard.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithFiveCompanionsExertsToPlayOneMenMinionStackedOnAMenPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");
		var squad = scn.GetShadowCard("squad");
		var dagger = scn.GetShadowCard("dagger");
		var fleet = scn.GetShadowCard("fleet");
		var pass = scn.GetShadowCard("pass");
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "boromir");
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, guard2, squad);
		scn.AttachCardsTo(squad, dagger);
		scn.MoveCardsToSupportArea(fleet, pass);
		scn.StackCardsOn(dagger, warrior);
		scn.StackCardsOn(fleet, saruman);
		scn.StackCardsOn(pass, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, guard, guard2, squad);
		assertInZone(Zone.STACKED, warrior, saruman, runner);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(guard));
		scn.ShadowUseCardAction(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guard2));

		// A [men] Man on the borne Dagger and a [men] Wizard on the Fleet; not a [men] Man on the [gollum] Pass.
		// Five companions and an ally are not six companions: one minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(saruman);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertInZone(Zone.STACKED, warrior, runner);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSixCompanionsPlaysTwoMenMinionsFromMenPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var hillsman = scn.GetShadowCard("hillsman");
		var runner = scn.GetShadowCard("runner");
		var squad = scn.GetShadowCard("squad");
		var dagger = scn.GetShadowCard("dagger");
		var fleet = scn.GetShadowCard("fleet");
		var pass = scn.GetShadowCard("pass");
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "boromir", "merry");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, squad);
		scn.AttachCardsTo(squad, dagger);
		scn.MoveCardsToSupportArea(fleet, pass);
		scn.StackCardsOn(dagger, warrior);
		scn.StackCardsOn(fleet, saruman, hillsman);
		scn.StackCardsOn(pass, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, guard, squad);
		assertInZone(Zone.STACKED, warrior, saruman, hillsman, runner);
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(guard);
		assertEquals(1, scn.GetWoundsOn(guard));

		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, saruman, hillsman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);

		// Six companions: a second minion, here off the other possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, hillsman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(warrior);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman, warrior);
		assertInZone(Zone.STACKED, hillsman, runner);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var warrior = scn.GetShadowCard("warrior");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, guard2);
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, warrior);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.STACKED, warrior);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertFalse(scn.ShadowActionAvailable(guard));
		assertTrue(scn.ShadowActionAvailable(guard2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsOfferedOnlyWithAMenMinionOnAMenPossessionThatCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var fleet = scn.GetShadowCard("fleet");
		var pass = scn.GetShadowCard("pass");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.MoveCardsToSupportArea(fleet, pass);
		scn.StackCardsOn(pass, runner);
		assertInZone(Zone.STACKED, runner);
		assertInZone(Zone.DECK, warrior);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// The play is a cost (Q-0062): the Runner on the [gollum] Pass cannot pay it.
		assertFalse(scn.ShadowActionAvailable(guard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The Warrior on the Fleet, as if from hand: 3 printed + 2 roaming at site 2.
		scn.StackCardsOn(fleet, warrior);
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(guard));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(guard));
	}
}
