package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_029_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The fellowship phase
	// of turn 1 is at Bag End (site 1), the skirmishes at The Troll Hoard (site 2), whose only text is a
	// start-of-turn offer. A doubt is the Hobbit game's burden.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glamdring", "30_29");
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: strength 7, vitality 4; a fellowship ability
					                            // and texts about [dwarven] followers and Thorin, none laid
					put("radagast", "31_17");   // Radagast, The Brown: a [gandalf] Wizard ally who is no Gandalf; a
					                            // fellowship ability not used
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; its text needs 3 Ents

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an assignment ability not used
					put("goblin", "31_36");     // The Great Goblin: a fierce [moria] Orc, strength 10; a maneuver ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GlamdringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Glamdring
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Gandalf.
		 * 	Each time Gandalf wins a skirmish, you may remove a doubt.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glamdring");

		assertEquals("Glamdring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveAlliesToTable(radagast);

		scn.StartGame();
		scn.MoveCardsToHand(glamdring);
		scn.FreepsRefreshDecision();
		assertInHand(glamdring);

		// No Gandalf: neither the [gandalf] Wizard ally, nor the [gandalf] companion, nor Bilbo may bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(glamdring));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		scn.FreepsPlayCard(glamdring);

		// Gandalf is the one bearer it may have, so nothing is asked.
		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWinOfGandalfsMayRemoveOneDoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aggressor = scn.GetShadowCard("aggressor");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor, goblin);
		// Strength 12 by hand, so that Gandalf also beats The Great Goblin's 10 in the fierce round.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gandalf, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		// The Great Goblin is left out of the first round.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsAcceptOptionalTrigger();
		// One doubt of the two.
		assertEquals(1, scn.GetBurdens());

		// Gandalf takes on The Great Goblin in the fierce round and wins again: offered again.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, goblin);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: the doubt left stays.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GandalfLosingASkirmishRemovesNoDoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		// Strength 11 by hand, so that the Aggressor beats Gandalf's 9 and Gandalf lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aggressor, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GandalfWinningWithNoDoubtIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		assertEquals(0, scn.GetBurdens());
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// 7 + 2 for Glamdring against 6: Gandalf wins, and there is no doubt to remove.
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
