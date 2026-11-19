package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keepit", "3_105");

					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally; a fellowship ability never used
					put("melancholy", "12_119"); // Bilbo, Melancholy Hobbit: [shire] Hobbit companion; a trigger at the start of his skirmishes, not reached
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a [shire] Hobbit ally who is no Bilbo; a fellowship ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion to bear the Saga; a maneuver ability never used
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition borne by a companion; a skirmish ability never used
					put("stairs", "1_24");      // Stairs of Khazad-dum: a [dwarven] condition in the support area; a bonus only in a skirmish
					put("mallorn", "1_54");     // Mallorn-trees: an [elven] condition in the support area; a fellowship ability never used
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession (no condition) on Frodo; no text but its bearer

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability never used
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai; damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion; no text
					put("hatred", "7_310");     // Sauron's Hatred: a [sauron] condition (no minion) in the support area; Shadow and regroup abilities never used
					put("relics", "1_195");     // Relics of Moria: a Shadow condition in the support area; a Shadow ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhyShouldntIKeepItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Why Shouldn't I Keep It?
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a [sauron] minion to discard Bilbo. The Free Peoples player may discard 2 Free Peoples conditions to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("keepit");

		assertEquals("Why Shouldn't I Keep It?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithASauronMinionBilboIsDiscardedWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var maggot = scn.GetFreepsCard("maggot");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		var keepit = scn.GetShadowCard("keepit");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo, maggot);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.ATTACHED, saga);
		assertInZone(Zone.SUPPORT, stairs);
		scn.MoveCardsToHand(keepit);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, bilbo, maggot, stairs);
		assertTrue(scn.ShadowPlayAvailable(keepit));

		scn.ShadowPlayCard(keepit);
		// Two Free Peoples conditions are in play, so he is asked.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		// Discarded, not killed: to the discard pile.
		assertInDiscard(bilbo);
		// A Hobbit ally who is no Bilbo stays, and so do the conditions.
		assertInZone(Zone.SUPPORT, maggot, stairs);
		assertInZone(Zone.ATTACHED, saga);
		assertInZone(Zone.DISCARD, keepit);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOneFreePeoplesConditionNothingIsAskedAndTheCompanionBilboIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var melancholy = scn.GetFreepsCard("melancholy");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var keepit = scn.GetShadowCard("keepit");
		var sentry = scn.GetShadowCard("sentry");
		var relics = scn.GetShadowCard("relics");
		scn.MoveCompanionsToTable(melancholy, aragorn);
		// One Free Peoples condition, beside a Free Peoples possession and a Shadow condition.
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(keepit);

		scn.StartGame();
		// A [sauron] Uruk-hai is the only minion.
		scn.MoveMinionsToTable(sentry);
		scn.MoveCardsToSupportArea(relics);
		assertInZone(Zone.ATTACHED, saga, sword);
		assertInZone(Zone.SUPPORT, relics);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.FREE_CHARACTERS, melancholy);
		assertTrue(scn.ShadowPlayAvailable(keepit));
		scn.ShadowPlayCard(keepit);

		// One Free Peoples condition cannot pay for the prevention, so the Free Peoples player is not asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInDiscard(melancholy);
		assertInZone(Zone.ATTACHED, saga, sword);
		assertInZone(Zone.SUPPORT, relics);
		assertInZone(Zone.DISCARD, keepit);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTwoFreePeoplesConditionsToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		var mallorn = scn.GetFreepsCard("mallorn");
		var sword = scn.GetFreepsCard("sword");
		var keepit = scn.GetShadowCard("keepit");
		var soldier = scn.GetShadowCard("soldier");
		var relics = scn.GetShadowCard("relics");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		// Free Peoples conditions of three cultures, one borne and two in the support area.
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(stairs, mallorn);
		// A Free Peoples possession.
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(keepit);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		// A Shadow condition.
		scn.MoveCardsToSupportArea(relics);
		assertInZone(Zone.ATTACHED, saga, sword);
		assertInZone(Zone.SUPPORT, stairs, mallorn, relics);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, bilbo);
		scn.ShadowPlayCard(keepit);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The three Free Peoples conditions are offered; the possession and the Shadow condition are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(saga, stairs, mallorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, relics));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(saga, mallorn);

		assertInDiscard(saga, mallorn);
		assertInZone(Zone.SUPPORT, stairs, relics, bilbo);
		assertInZone(Zone.ATTACHED, sword);
		assertInZone(Zone.DISCARD, keepit);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASauronMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var keepit = scn.GetShadowCard("keepit");
		var troop = scn.GetShadowCard("troop");
		var hatred = scn.GetShadowCard("hatred");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(keepit);

		scn.StartGame();
		// A minion of another culture and a [sauron] card that is no minion.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(hatred);
		assertInZone(Zone.SUPPORT, hatred);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(keepit));

		scn.MoveMinionsToTable(sentry);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(keepit));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoBilboInPlayItIsPlayedForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var maggot = scn.GetFreepsCard("maggot");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		var keepit = scn.GetShadowCard("keepit");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.ATTACHED, saga);
		assertInZone(Zone.SUPPORT, stairs);
		scn.MoveCardsToHand(keepit);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Bilbo is no requirement of the play.
		assertTrue(scn.ShadowPlayAvailable(keepit));
		scn.ShadowPlayCard(keepit);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.SUPPORT, maggot, stairs);
		assertInZone(Zone.ATTACHED, saga);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.DISCARD, keepit);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
