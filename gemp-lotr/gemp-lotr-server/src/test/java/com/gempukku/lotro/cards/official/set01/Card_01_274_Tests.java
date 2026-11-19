package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_274_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defenses1", "1_274");
					put("defenses2", "1_274");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; adds (2) only when a weapon is played on him
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, an Orc who is not [sauron]; no text

					// The dead companions, of three cultures and races; none of their texts works from the dead pile.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: [elven] Elf, vitality 3; archer, and strength against a Nazgul only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, a character who is no companion
					put("dwarfguard", "1_7");   // Dwarf Guard: Dwarf companion, strength 4, vitality 2; its text is about playing it
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Durin's Secret to spot; a strength modifier only
					put("secret", "1_71");      // Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("stand1", "1_63");      // Stand Against Darkness: Maneuver: exert an Elf to discard a [sauron] minion, condition or possession
					put("stand2", "1_63");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronsDefensesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sauron's Defenses
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert a [sauron] Orc. Plays to your support area.
		 * 	While there are 3 or more companions in the dead pile, the move limit for this turn is -1 (to a minimum of 1).
		*/

		var scn = GetScenario();

		var defenses = scn.GetFreepsCard("defenses1");

		assertEquals("Sauron's Defenses", defenses.getBlueprint().getTitle());
		assertNull(defenses.getBlueprint().getSubtitle());
		assertFalse(defenses.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, defenses.getBlueprint().getSide());
		assertEquals(Culture.SAURON, defenses.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, defenses.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(defenses, Keyword.SUPPORT_AREA));
		assertEquals(1, defenses.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcToPlayToTheSupportAreaAndBesideThreeDeadCompanionsTheMoveLimitIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetShadowCard("defenses1");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(defenses);
		scn.MoveCardsToDeadPile(aragorn, gimli, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, sentry, troop);
		assertInHand(defenses);
		assertInZone(Zone.DEAD, aragorn, gimli, legolas);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowPlayAvailable(defenses));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.ShadowPlayCard(defenses);
		// Both [sauron] Orcs are offered; the [sauron] Uruk-hai and the [moria] Orc, each able to exert, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, defenses);
		// Three companions of three cultures in the dead pile: 2 - 1.
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetShadowCard("defenses1");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(defenses);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, troop);
		// Vitality 2 with 1 wound: exhausted.  The Uruk-hai and the [moria] Orc beside it could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertTrue(scn.IsExhausted(soldier));
		assertFalse(scn.IsExhausted(sentry));
		assertFalse(scn.IsExhausted(troop));
		assertInHand(defenses);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(defenses));
		assertInHand(defenses);
	}

	@Test
	public void TwoDeadCompanionsBesideTheNearMissesLeaveTheLimitAtTwoAndAThirdDeathEndsTheTurnAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetShadowCard("defenses1");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var boromir = scn.GetFreepsCard("boromir");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		// The Shadow player's own companions, killed on his own turns: "the dead pile" is the Free Peoples player's.
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var shadowGimli = scn.GetShadowCard("gimli");
		var shadowLegolas = scn.GetShadowCard("legolas");
		scn.MoveCardsToSupportArea(defenses);
		// Two companions and an ally in the dead pile, and a companion discarded, not killed.
		scn.MoveCardsToDeadPile(aragorn, gimli, thrarin, shadowAragorn, shadowGimli, shadowLegolas);
		scn.MoveCardsToDiscard(boromir);
		scn.MoveCompanionsToTable(dwarfguard);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, defenses);
		assertInZone(Zone.DEAD, aragorn, gimli, thrarin, shadowAragorn, shadowGimli, shadowLegolas);
		assertInDiscard(boromir);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwarfguard, troop);
		scn.FreepsResolveSkirmish(dwarfguard);
		assertEquals(2, scn.GetMoveLimit());

		// 13 against 4: Dwarf Guard is overwhelmed, the third companion in the dead pile.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwarfguard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ARaisedLimitIsLoweredByExactlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetShadowCard("defenses1");
		var secret = scn.GetFreepsCard("secret");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(defenses);
		scn.MoveCardsToDeadPile(aragorn, gimli, boromir);
		// Gandalf, a Dwarf and an Elf for Durin's Secret to spot.
		scn.MoveCompanionsToTable("gandalf", "dwarfguard", "legolas");
		scn.MoveCardsToHand(secret);

		scn.StartGame();
		assertInZone(Zone.DEAD, aragorn, gimli, boromir);

		// The limit cannot go below 1, so from 2 a penalty of 1 and a penalty of 2 read the same: raise the limit first.
		assertEquals(1, scn.GetMoveLimit()); // 2 - 1
		scn.FreepsPlayCard(secret);
		assertInDiscard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCopiesLeaveTheLimitAtOneAndDiscardingThemDoesNotGiveTheMovesBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses1 = scn.GetShadowCard("defenses1");
		var defenses2 = scn.GetShadowCard("defenses2");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		var secret = scn.GetFreepsCard("secret");
		var stand1 = scn.GetFreepsCard("stand1");
		var stand2 = scn.GetFreepsCard("stand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(defenses1, defenses2);
		scn.MoveCardsToDeadPile(aragorn, gimli, boromir);
		scn.MoveCompanionsToTable("gandalf", "dwarfguard", "legolas");
		scn.MoveCardsToHand(secret, stand1, stand2);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, defenses1, defenses2);
		assertInZone(Zone.DEAD, aragorn, gimli, boromir);
		// 2 - 1 - 1 would be 0: the minimum is 1.
		assertEquals(1, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(1, scn.GetMoveLimit()); // 2 + 1 - 1 - 1

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsPlayCard(stand1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(defenses1, defenses2));
		scn.FreepsChooseCard(defenses1);
		assertInDiscard(defenses1);
		assertInZone(Zone.SUPPORT, defenses2);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetMoveLimit());

		scn.ShadowPass();
		scn.FreepsPlayCard(stand2);
		assertInDiscard(defenses2);
		assertEquals(2, scn.GetWoundsOn(legolas));
		// Both copies gone, and both penalties still stand for the turn: 2 + 1 - 1 - 1, not 3.
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ThePenaltyIsGoneOnItsOwnersTurnAndGivenAgainOnTheNextFreePeoplesTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetShadowCard("defenses1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(defenses);
		scn.MoveCardsToDeadPile(aragorn, gimli, boromir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, defenses);
		assertInZone(Zone.DEAD, aragorn, gimli, boromir);
		assertEquals(1, scn.GetMoveLimit());

		// Player 2's turn: his own Shadow card is not active, and his dead pile is empty.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		// Player 1's next turn: the three are still in his dead pile, and the limit is 1 again.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.SUPPORT, defenses);
		assertEquals(1, scn.GetMoveLimit());
	}
}
