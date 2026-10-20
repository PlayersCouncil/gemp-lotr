package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_031_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "59_31");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				"59_31",                        // Boromir is the Ring-bearer
				// The Great Ring gives no strength and has no response to a wound, so nothing but Boromir's
				// own text is asked about when he is wounded; its abilities are used only on purpose.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Boromir, Bearer of Council
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Can be the Ring-bearer: True
		 * Game Text: While Boromir is the Ring-bearer, at the start of each skirmish involving him, add 2 burdens or wound him twice.
		 * 	Each time Boromir wins a skirmish, discard each minion he is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Bearer of Council", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void AsTheRingBearerBoromirAddsTwoBurdensAtTheStartOfHisOwnSkirmishAndDiscardsTheMinionHeBeats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var boromir = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage2}, new PhysicalCardImpl[]{boromir, savage});

		// A skirmish that does not involve Boromir, though he is assigned to another: nothing is asked.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(boromir));

		// Aragorn (8) beats his Uruk Savage (5), which takes its wound and stays: it was not Boromir who won.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInZone(Zone.SHADOW_CHARACTERS, savage, savage2);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsResolveSkirmish(boromir);
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Wound"));

		scn.FreepsChoose("burdens");
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir (7) beats the Uruk Savage (5, vitality 3): the one wound of the skirmish would not have
		// removed it, and it is in the discard pile.  The Savage that fought Aragorn stays.
		scn.PassSkirmishActions();
		// Boromir's own text answers the win before the skirmish's wound, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AsTheRingBearerBoromirMayBeWoundedTwiceInsteadOfAddingTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var boromir = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Wound"));

		scn.FreepsChoose("Wound");
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, savage);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTwoWoundsAreWoundsAndKillABoromirWhoCouldNotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var boromir = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 1 wound: two wounds can still be placed on him, though he could exert only once.
		scn.AddWoundsToChar(boromir, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		assertFalse(scn.GameIsFinished());

		scn.FreepsChoose("Wound");

		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void WithOneVitalityLeftBoromirCannotBeWoundedTwiceSoTheBurdensAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var boromir = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(boromir, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsResolveSkirmish(boromir);

		// Only the first of the two wounds could be placed, so that is not a choice he is fully capable of:
		// the burdens are what happens, and nothing is asked.
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AFierceMinionBringsTheChoiceAgainAtBoromirsSecondSkirmishAndLosingDiscardsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var boromir = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);

		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		scn.FreepsChoose("burdens");
		assertEquals(2, scn.GetBurdens());

		// 7 against 13: Boromir loses and takes the skirmish's wound; the Troop is not discarded.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(2, scn.GetBurdens());

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		scn.FreepsChoose("burdens");
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhileBoromirIsNotTheRingBearerNothingIsAskedAtHisSkirmishOrAtTheRingBearers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		// 3 + 1 for The Ruling Ring + 3 = 7 against the Uruk Savage's 5: Frodo wins his skirmish, so The Ruling
		// Ring's response to a wound is never in the way.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, savage}, new PhysicalCardImpl[]{boromir, troop});

		// The Ring-bearer's skirmish begins while Boromir is not in it and is not the Ring-bearer: nothing is
		// asked of Boromir.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(boromir));

		// 7 against 13: he loses, takes the one wound of the skirmish, and the Troop is not discarded.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBoromirWinsEachMinionHeIsSkirmishingIsDiscardedAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(boromir, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, savage2);
		// 7 + 12 = 19 against 13 + 5 = 18: he wins, and by less than double, so nothing is killed outright.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 12));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, troop}, new PhysicalCardImpl[]{aragorn, savage2});
		// The Uruk Savage left over is the Shadow player's to assign.
		scn.ShadowAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(19, scn.GetStrength(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage, savage2);

		scn.PassSkirmishActions();
		// Boromir's own text answers the win before the skirmish's wounds, with nothing to order (engine pass 54).

		// Both have vitality 3, so the wound each took for losing would have left them in play.
		assertInZone(Zone.DISCARD, troop, savage);
		// The Savage assigned to Aragorn is not a minion Boromir is skirmishing.
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertEquals(0, scn.GetWoundsOn(savage2));

		// Aragorn (8) beats it (5): a wound, and it stays.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASecondWinInTheSameTurnDiscardsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		// 7 against 5.
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, savage);

		// A second move, and a second Uruk Savage to fight.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(savage2);
		scn.ShadowRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage2);
		scn.FreepsResolveSkirmish(boromir);
		assertInZone(Zone.SHADOW_CHARACTERS, savage2);

		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertInZone(Zone.DISCARD, savage2);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
