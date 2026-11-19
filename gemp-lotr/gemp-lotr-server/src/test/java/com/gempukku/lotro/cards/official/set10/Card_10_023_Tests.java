package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "10_23");
					put("gollum", "5_25");      // Gollum, Stinker: a [gollum] Gollum; strength +1 a burden, an optional burden when he wins
					put("wgollum", "103_66");   // Gollum, Half a Wraith Himself: a [wraith] Gollum; his assignment ability wants a threat or a companion of cost 0 other than the Ring-bearer
					put("smeagol", "8_27");     // Smeagol, Slippery Sneak: a [gollum] companion who is no Gollum; abilities used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus by the races he spots
					put("gandalf2", "1_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion, strength 7, vitality 3; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally; a fellowship ability used only on purpose
					put("twofaced", "103_15");  // Two-faced: [gollum] Shadow event, "Restore a [gollum] card", then an optional replacement that wants a threat
					put("sentback", "9_27");    // Sent Back: "Fellowship or Regroup: Play a Wizard (even if another copy of that Wizard is in your dead pile)"
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Shelob, Her Ladyship
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 8
		 * Site Number: 8
		 * Game Text: Enduring. Fierce.
		 * 	When you play Shelob, if you can spot Gollum, you may spot a companion. That companion cannot be assigned to a skirmish until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shelob");

		assertEquals("Shelob", card.getBlueprint().getTitle());
		assertEquals("Her Ladyship", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(8, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithGollumSheMaySpotACompanionWhoCannotBeAssignedUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(grimir);

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(shelob);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(shelob);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowAcceptOptionalTrigger();
		// Any companion, the Ring-bearer included; not the ally, not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertFalse(scn.ShadowHasCardChoiceAvailable(grimir));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gollum));
		assertFalse(scn.ShadowHasCardChoiceAvailable(shelob));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gandalf);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(gandalf));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));

		// Nobody is assigned, so no skirmish is fought; Shelob, fierce, brings the fierce assignment phase.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertFalse(scn.FreepsCanAssign(gandalf));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// A second move in the same turn: still barred at its assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		assertFalse(scn.InFierceRound());
		assertFalse(scn.FreepsCanAssign(gandalf));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The turn ends; on the player's next turn Gandalf may be assigned again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void DeclinedNobodyIsBarredAndAGollumOfAnotherCultureAlsoLetsHerAsk() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		var shelob = scn.GetShadowCard("shelob");
		var wgollum = scn.GetShadowCard("wgollum");
		scn.MoveCardsToHand(shelob);

		scn.StartGame();
		scn.MoveMinionsToTable(wgollum);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(shelob);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
	}

	@Test
	public void WithoutGollumSheAsksNothingAndAGollumPlayedAfterHerDoesNotSetHerOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var smeagol = scn.GetFreepsCard("smeagol");
		// A [gollum] card who is no Gollum: no [gollum] minion has another title than Gollum or Shelob.
		scn.MoveCompanionsToTable(gandalf, smeagol);

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(shelob, gollum);

		scn.StartGame();
		scn.SetTwilight(12);
		scn.FreepsPass();

		scn.ShadowPlayCard(shelob);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(smeagol));
	}

	@Test
	public void RestoredAfterBeingHinderedSheIsNotPlayedAgainAndSpotsNoSecondCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var twofaced = scn.GetShadowCard("twofaced");
		scn.MoveCardsToHand(shelob, twofaced);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(shelob);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gandalf);

		// CR 5.0 "restore": hindered and restored, she is not being played again.
		scn.HinderCard(shelob);
		assertTrue(scn.IsHindered(shelob));
		scn.ShadowPlayCard(twofaced);
		assertFalse(scn.IsHindered(shelob));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
	}

	@Test
	public void AGandalfPlayedAgainWithSentBackAfterTheSpottedOneDiedMayBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sentback = scn.GetFreepsCard("sentback");
		scn.MoveCompanionsToTable(gandalf, aragorn, boromir);
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToHand(gandalf2);

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(shelob);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Gandalf (vitality 4) is one wound from death, and so is Boromir (vitality 3).
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddThreats(1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// CR 4.2 "modifier": Shelob is played and Gandalf may not be assigned this turn.
		scn.ShadowPlayCard(shelob);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gandalf);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(gandalf));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, shelob}, new PhysicalCardImpl[]{aragorn, gollum});

		// Shelob kills a companion in a skirmish, and Gandalf dies when the threat wound is assigned.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetThreats());
		scn.FreepsChooseCard(gandalf);
		assertInZone(Zone.DEAD, gandalf);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// In the regroup phase Gandalf is played with Sent Back.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(sentback));
		scn.FreepsUseCardAction(sentback);
		assertInZone(Zone.FREE_CHARACTERS, gandalf2);

		// After a second move, with Shelob still in play, this Gandalf may be assigned.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertTrue(scn.FreepsCanAssign(gandalf2));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(gandalf2));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}
}
