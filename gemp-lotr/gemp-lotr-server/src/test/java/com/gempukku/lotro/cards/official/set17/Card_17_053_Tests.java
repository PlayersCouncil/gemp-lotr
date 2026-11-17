package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "17_53");
					put("savage2", "17_53");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, cost 3, site 4; a maneuver ability never used
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), cost 4, site 4; "Each [men] minion
					// bearing a possession is strength +2" and a skirmish ability, neither reached here
					put("saruman", "17_48");
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, cost 2; a regroup ability only
					// Man of Bree: [men] Man, cost 2; strength +1 for each card in the Free Peoples hand
					put("bree", "11_90");
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
					put("brand", "17_47");      // Primitive Brand: a [men] possession, no minion
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					// Rampaging Easterling: a [men] Man; laid in the Free Peoples player's discard pile only
					put("rampager", "11_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StampedingSavageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Stampeding Savage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Discard this minion and X minions stacked on a [men] possession to play a [men] minion from your discard pile. That minion is twilight cost -X.
		*/

		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");

		assertEquals("Stampeding Savage", savage.getBlueprint().getTitle());
		assertNull(savage.getBlueprint().getSubtitle());
		assertFalse(savage.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, savage.getBlueprint().getSide());
		assertEquals(Culture.MEN, savage.getBlueprint().getCulture());
		assertEquals(CardType.MINION, savage.getBlueprint().getCardType());
		assertEquals(Race.MAN, savage.getBlueprint().getRace());
		assertEquals(3, savage.getBlueprint().getTwilightCost());
		assertEquals(8, savage.getBlueprint().getStrength());
		assertEquals(2, savage.getBlueprint().getVitality());
		assertEquals(4, savage.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardsItselfAndTwoMinionsStackedOnMenPossessionsToPlayAMenMinionFromTheDiscardPileTwoCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var hillsman = scn.GetShadowCard("hillsman");
		var bree = scn.GetShadowCard("bree");
		var runner = scn.GetShadowCard("runner");
		var squad = scn.GetShadowCard("squad");
		var dagger = scn.GetShadowCard("dagger");
		var fleet = scn.GetShadowCard("fleet");
		var pass = scn.GetShadowCard("pass");
		var brand = scn.GetShadowCard("brand");
		var brigand = scn.GetShadowCard("brigand");
		var rampager = scn.GetFreepsCard("rampager");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, squad);
		scn.AttachCardsTo(squad, dagger);
		scn.MoveCardsToSupportArea(fleet, pass);
		scn.StackCardsOn(dagger, hillsman);
		scn.StackCardsOn(fleet, bree);
		scn.StackCardsOn(pass, runner);
		scn.MoveCardsToDiscard(warrior, saruman, brand, brigand, rampager);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, savage2, squad);
		assertInZone(Zone.STACKED, hillsman, bree, runner);
		assertInDiscard(warrior, saruman, brand, brigand, rampager);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(savage));
		scn.ShadowUseCardAction(savage);

		// X: the minions on the borne Dagger and on the Fleet in the support area, not the one on the [gollum] Pass.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, bree));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(hillsman, bree);

		assertInDiscard(savage, hillsman, bree);
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertInZone(Zone.STACKED, runner);

		// A [men] Man and a [men] Wizard; not a [men] possession, a [dunland] Man, or the Free Peoples player's own
		// [men] Man in his discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brand, brigand, rampager));
		scn.ShadowChooseCardBPFromSelection(saruman);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		// 4 printed + 2 roaming at site 2 - 2 for the two stacked minions discarded
		assertEquals(6, scn.GetTwilight());
		assertInDiscard(warrior);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOneStackedMinionDiscardedTheMinionPlayedIsOneCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var hillsman = scn.GetShadowCard("hillsman");
		var bree = scn.GetShadowCard("bree");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(fleet);
		// A [men] Wizard as well as a [men] Man is a minion stacked on the possession.
		scn.StackCardsOn(fleet, hillsman, saruman);
		scn.MoveCardsToDiscard(warrior, bree);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.STACKED, hillsman, saruman);
		assertInDiscard(warrior, bree);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(savage);
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, saruman));
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(saruman);

		assertInDiscard(savage, saruman);
		assertInZone(Zone.STACKED, hillsman);
		scn.ShadowChooseCardBPFromSelection(warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		// 3 printed + 2 roaming at site 2 - 1 for the stacked minion discarded
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoStackedMinionDiscardedTheMinionPlayedPaysItsFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var hillsman = scn.GetShadowCard("hillsman");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, hillsman);
		scn.MoveCardsToDiscard(warrior, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.STACKED, hillsman);
		assertInDiscard(warrior, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(savage);
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertInDiscard(savage);
		assertInZone(Zone.STACKED, hillsman);
		scn.ShadowChooseCardBPFromSelection(warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		// 3 printed + 2 roaming at site 2, nothing off
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoMenMinionInTheDiscardPileToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var hillsman = scn.GetShadowCard("hillsman");
		var fleet = scn.GetShadowCard("fleet");
		var brand = scn.GetShadowCard("brand");
		var brigand = scn.GetShadowCard("brigand");
		var rampager = scn.GetFreepsCard("rampager");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, hillsman);
		scn.MoveCardsToDiscard(brand, brigand, rampager);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.STACKED, hillsman);
		assertInDiscard(brand, brigand, rampager);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The play is a cost (Q-0062): with no [men] minion of his own in the pile it cannot be paid.
		assertFalse(scn.ShadowActionAvailable(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.MoveCardsToDiscard(warrior);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(savage));
	}

	@Test
	public void TheSavageAndTheStackedMinionsDiscardedForTheCostAreNotTheMinionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var hillsman = scn.GetShadowCard("hillsman");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, hillsman);
		scn.MoveCardsToDiscard(warrior, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.STACKED, hillsman);
		assertInDiscard(warrior, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(savage);
		scn.ShadowChooseCards(hillsman);
		assertInDiscard(savage, hillsman);

		// The They Are Coming principle (Q-0192, Q-0320, Q-0331): what the cost discarded is not the card played.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, hillsman));
		scn.ShadowChooseCardBPFromSelection(warrior);

		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertInDiscard(savage, hillsman, saruman);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsOfferedWhereTheMinionCanBePaidForOnlyWithTheDiscountOfTheStackedMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var squad = scn.GetShadowCard("squad");
		var hillsman = scn.GetShadowCard("hillsman");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, hillsman);
		// Two [men] Men of the same cost, so that the choice of the minion is asked whatever else is corrected.
		scn.MoveCardsToDiscard(warrior, squad);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.STACKED, hillsman);
		assertInDiscard(warrior, squad);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// The Warrior costs 3 + 2 roaming at site 2, and 4 with the one stacked minion discarded: 3 is one short.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(savage));
		scn.ShadowUseCardAction(savage);
		scn.ShadowChooseCards(hillsman);
		assertInDiscard(savage, hillsman);
		scn.ShadowChooseCardBPFromSelection(warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
