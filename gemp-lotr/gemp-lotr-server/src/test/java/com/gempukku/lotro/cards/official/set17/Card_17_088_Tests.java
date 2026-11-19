package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "17_88");
					put("warrior2", "17_88");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text

					put("gandalf", "6_30");       // Gandalf, Mithrandir: [gandalf] Wizard companion, strength 10; wounded at each move
					put("glamdring", "1_75");     // Glamdring: strength +2, bearer must be Gandalf
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("treebeard", "4_104");    // Treebeard, Oldest Living Thing: an ally of strength 12
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rivendell Waterfall as site 3, with Aragorn, a ranger: the move limit is 3 there, so a turn has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "17_88");
					put("warrior2", "17_88");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a ranger; a maneuver ability only
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_342");   // Rivendell Waterfall
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 14
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a companion of strength 12 or more, this minion is strength +4. Each time the fellowship moves during the regroup phase, discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Orkish Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GandalfMadeStrength12ByGlamdringMakesEachWarriorStrength18AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(glamdring);

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Minions laid early, to be read as a Free Peoples card is played (Q-0228).
		scn.MoveMinionsToTable(warrior, warrior2, troop);
		scn.FreepsRefreshDecision();

		// An ally of 12, and minions of 13 and 14 (the Warriors themselves): no companion of 12 or more.
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(12, scn.GetStrength(treebeard));
		assertEquals(14, scn.GetStrength(warrior));
		assertEquals(14, scn.GetStrength(warrior2));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);

		// 10 + 2 for Glamdring.
		assertEquals(12, scn.GetStrength(gandalf));
		// 14 + 4, each Warrior once, and not the other minion.
		assertEquals(18, scn.GetStrength(warrior));
		assertEquals(18, scn.GetStrength(warrior2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionOf11IsNotEnoughAndOf12Is() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);

		// A strength that only arranges the board: 8 + 3.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(14, scn.GetStrength(warrior));

		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(18, scn.GetStrength(warrior));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFellowshipPhaseMoveDoesNotDiscardItAndARegroupMoveDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warrior = scn.GetShadowCard("warrior");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Laid early, so that it is in play at the move that ends the fellowship phase (Q-0228).
		scn.MoveMinionsToTable(warrior);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Aragorn, 8, loses to the Warrior, 14, and lives.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// Another minion, standing at the regroup move with it.
		scn.MoveMinionsToTable(troop);

		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior, troop);
		scn.FreepsChooseToMove();

		assertInZone(Zone.DISCARD, warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoRegroupMovesInOneTurnDiscardsTheWarriorThen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(warrior);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);

		// The first regroup move, to Rivendell Waterfall.
		scn.FreepsChooseToMove();
		assertInZone(Zone.DISCARD, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(warrior2);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior2);

		// The second regroup move of the same turn.
		scn.FreepsChooseToMove();
		assertInZone(Zone.DISCARD, warrior2);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveCount());
	}
}
