package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.modifiers.CantHinderModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_025_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gate1", "103_25");
					put("gate2", "103_25");
					put("gate3", "103_25");
					// Gondorian Knight: a [gondor] Man, vitality 3, whose whole text is "Knight."
					put("knight1", "5_35");
					put("knight2", "5_35");
					put("knight3", "5_35");
					put("knight4", "5_35");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] knight who is a Hobbit, vitality 4; a skirmish ability only
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man, vitality 3, no knight; his ability needs a mount and a skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man who is no knight; Maneuver: exert him to make him defender +1
					put("boromir", "0_65");     // Boromir, Steward's Heir: a [gondor] Man, vitality 3; "Ring-bound. Knight. Ranger." is his whole text
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion who is no Man and no knight; a skirmish ability only
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man who is an ally, vitality 2; a fellowship ability only
					// Northern Signal-fire, Flame of Calenhad: "Maneuver: Hinder this beacon and exert an unbound Man. Until
					// the regroup phase, each of your other Men gain each of the unloaded keywords (except race) on that
					// Man." To play it hinders 2 beacons, so it is cheated in.
					put("flame", "103_86");

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion, strength 13, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion, strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: a third minion; its text is about being played
					put("armory", "1_173");     // Goblin Armory: a Shadow condition in the support area; answers [moria] weapons played and wounds on [moria] Orcs
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InnerGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Inner Gate
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Strength: -3
		 * Game Text: Fortification.
		 * 	Maneuver: If this is in your support area, exert and hinder 2 unbound knights to transfer this to a
		 * 	minion.  Hinder that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gate1");

		assertEquals("Inner Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedToTheSupportAreaAndAThirdCopyIsRefusedWhileTwoAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate1");
		var gate2 = scn.GetFreepsCard("gate2");
		var gate3 = scn.GetFreepsCard("gate3");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToHand(gate1, gate2, gate3);

		scn.StartGame();
		// A minion on the table in the fellowship phase, so that only the phase keeps the ability back.
		scn.MoveMinionsToTable(troop);

		assertTrue(scn.FreepsPlayAvailable(gate1));

		scn.FreepsPlayCard(gate1);

		assertInZone(Zone.SUPPORT, gate1);
		assertNotAttachedTo(gate1, knight1);
		assertNotAttachedTo(gate1, knight2);
		assertNotAttachedTo(gate1, frodo);
		assertNotAttachedTo(gate1, troop);
		// Two able knights and a minion, but this is not the maneuver phase.
		assertFalse(scn.FreepsActionAvailable(gate1));

		// Two dots: a second copy may be in play beside the first.
		assertTrue(scn.FreepsPlayAvailable(gate2));
		scn.FreepsPlayCard(gate2);

		assertInZone(Zone.SUPPORT, gate1, gate2);
		// And no third.
		assertFalse(scn.FreepsPlayAvailable(gate3));
		assertInZone(Zone.HAND, gate3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsAndHindersTwoUnboundKnightsToMoveToAMinionWhichIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate1");
		var gate2 = scn.GetFreepsCard("gate2");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var flame = scn.GetFreepsCard("flame");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight1, knight2, pippin, rider, boromir, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(gate1, gate2, flame, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, runner);
		// A minion already hindered is no longer "a minion" to anything that looks for one.
		scn.HinderCard(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Every knight in print is a [gondor] companion. The Flame of Calenhad makes knights of the player's
		// other Men for the rest of the maneuver phase: a [rohan] Man, and a Man who is an ally.
		assertFalse(scn.HasKeyword(rider, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(barliman, Keyword.KNIGHT));
		scn.FreepsUseCardAction(flame);
		scn.FreepsChooseCard(knight1);
		scn.ShadowPass();

		assertTrue(scn.HasKeyword(rider, Keyword.KNIGHT));
		assertTrue(scn.HasKeyword(barliman, Keyword.KNIGHT));
		assertTrue(scn.HasKeyword(boromir, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(gimli, Keyword.KNIGHT));

		int twilight = scn.GetTwilight();
		// The Flame's exertion.
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertFalse(scn.IsHindered(pippin));
		assertFalse(scn.IsHindered(rider));
		assertFalse(scn.IsHindered(savage));
		assertInZone(Zone.SUPPORT, gate1, gate2);
		assertTrue(scn.FreepsActionAvailable(gate1));
		assertTrue(scn.FreepsActionAvailable(gate2));

		scn.FreepsUseCardAction(gate1);

		// [gondor] Men, a [gondor] Hobbit, and a [rohan] Man who is a knight for now.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, pippin, rider));
		// Gimli is unbound and no knight; Boromir is a knight and Ring-bound; Barliman is a knight and an ally.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, boromir, barliman, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(pippin, rider);

		// The two chosen are exerted and hindered; nobody is asked a second time for knights.
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertTrue(scn.IsHindered(pippin));
		assertTrue(scn.IsHindered(rider));
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		assertFalse(scn.IsHindered(knight1));
		assertFalse(scn.IsHindered(knight2));

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, armory, gimli, knight1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(savage);

		assertAttachedTo(gate1, savage);
		assertNotInZone(Zone.SUPPORT, gate1);
		assertTrue(scn.IsHindered(savage));
		assertFalse(scn.IsHindered(troop));
		// The other copy has not moved, and a transfer by an effect costs no twilight.
		assertInZone(Zone.SUPPORT, gate2);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();

		// Two able knights and a minion are left, but this copy is no longer in the support area.
		assertFalse(scn.FreepsActionAvailable(gate1));
		assertTrue(scn.FreepsActionAvailable(gate2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheMinionIsThreeWeakerOnceItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate1");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsUseCardAction(gate1);
		// The only two knights are taken without asking.
		scn.FreepsChooseCard(troop);

		assertAttachedTo(gate1, troop);
		assertTrue(scn.IsHindered(troop));

		// The Shadow player reconciles before a second move is asked about, and his hindered minion is restored.
		scn.SkipToMovementDecision();

		assertFalse(scn.IsHindered(troop));
		assertAttachedTo(gate1, troop);
		// 13 - 3
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		// The knights are the Free Peoples player's, who has not reconciled.
		assertTrue(scn.IsHindered(knight1));
		assertTrue(scn.IsHindered(knight2));
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void NotOfferedWithOneUnboundKnightBesideAnUnboundManAndARingBoundKnight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate1");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		// One unbound knight. Aragorn is an unbound Man and no knight; Boromir is a knight and Ring-bound.
		scn.MoveCompanionsToTable(knight1, aragorn, boromir);
		scn.MoveCardsToSupportArea(gate1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(boromir, Keyword.KNIGHT));
		assertTrue(scn.HasKeyword(boromir, Keyword.RING_BOUND));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.HasKeyword(aragorn, Keyword.KNIGHT));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gate1));

		// A second unbound knight arrives, and another action is taken so that the game asks afresh.
		scn.MoveCompanionsToTable(knight2);
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(gate1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileOneOfTheOnlyTwoKnightsCannotExertOrCannotBeHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate1");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight1, knight2, aragorn);
		scn.MoveCardsToSupportArea(gate1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 3 with 2 wounds
		scn.AddWoundsToChar(knight2, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsExhausted(knight2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gate1));

		// One wound fewer, and another action taken so that the game asks afresh.
		scn.RemoveWoundsFromChar(knight2, 1);
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(gate1));

		// Able to exert, but hindered already.
		scn.HinderCard(knight2);
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();

		assertTrue(scn.IsHindered(knight2));
		assertFalse(scn.FreepsActionAvailable(gate1));

		// Restored, but made a knight who cannot be hindered: he could exert and still cannot pay.
		scn.RestoreCard(knight2);
		scn.ApplyAdHocModifier(new CantHinderModifier(null, "Can't be hindered", null, null, knight2, Filters.any));
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();

		assertFalse(scn.IsHindered(knight2));
		assertFalse(scn.IsExhausted(knight2));
		assertFalse(scn.FreepsActionAvailable(gate1));
		assertInZone(Zone.SUPPORT, gate1);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionLeftUnhinderedItIsStillOfferedAndItsCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate1");
		var gate2 = scn.GetFreepsCard("gate2");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight3 = scn.GetFreepsCard("knight3");
		var knight4 = scn.GetFreepsCard("knight4");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight1, knight2, knight3, knight4);
		scn.MoveCardsToSupportArea(gate1, gate2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(gate1);
		scn.FreepsChooseCards(knight1, knight2);
		// The only minion is taken without asking.
		assertAttachedTo(gate1, troop);
		assertTrue(scn.IsHindered(troop));
		scn.ShadowPass();

		// The cost can still be paid, so the second copy is on offer though no minion is left to move to.
		assertEquals(0, scn.GetWoundsOn(knight3));
		assertEquals(0, scn.GetWoundsOn(knight4));
		assertTrue(scn.FreepsActionAvailable(gate2));

		scn.FreepsUseCardAction(gate2);

		assertEquals(1, scn.GetWoundsOn(knight3));
		assertEquals(1, scn.GetWoundsOn(knight4));
		assertTrue(scn.IsHindered(knight3));
		assertTrue(scn.IsHindered(knight4));
		assertInZone(Zone.SUPPORT, gate2);
		assertNotAttachedTo(gate2, troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
