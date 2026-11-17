package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_154_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("riders", "11_154");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and nothing else
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions (or Eowyn)
					// can be spotted; his trigger answers a [rohan] Man exerting, and his ability is used only on purpose
					put("merry", "103_85");
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally; a Fellowship ability only
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, knight and nothing else
					// Erkenbrand, Master of Westfold: a [rohan] companion who may be played in a skirmish; his own trigger
					// is withheld with no Shadow possession in play
					put("erkenbrand", "0_59");

					put("troop", "8_106");      // Siege Troop: [sauron] Orc, vitality 4; a regroup ability used only on purpose
					put("wildmen", "4_4");      // Band of Wild Men: [dunland] Man, vitality 2; a trigger on its own win only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so always exhausted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RidersoftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Riders of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot 2 [rohan] companions.<br>You may play Riders of the Mark any time you could play a skirmish event.<br>When you play Riders of the Mark, you may exhaust a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("riders");

		assertEquals("Riders of the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilASecondRohanCompanionIsPlayedAndWithNoMinionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetFreepsCard("riders");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(merry, knight);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(riders, guard);

		scn.StartGame();

		// One [rohan] companion (Merry, a Hobbit), a [rohan] Man who is no companion (Guma) and a companion of
		// another culture (the Knight).  The Riders in hand do not spot themselves.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(riders));

		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);

		// Merry and the Guard.
		assertTrue(scn.FreepsPlayAvailable(riders));
		scn.FreepsPlayCard(riders);
		assertInZone(Zone.FREE_CHARACTERS, riders);

		// No minion is in play: the exhaust is not offered (Q-0224).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InASkirmishWithOneRohanCompanionItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetFreepsCard("riders");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(riders);
		assertInZone(Zone.HAND, riders);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		// The requirement holds on the play in a skirmish as well: the Guard is the one [rohan] companion.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.HAND, riders);
		assertFalse(scn.FreepsPlayAvailable(riders));
	}

	@Test
	public void PlayedInASkirmishButNotInTheManeuverPhaseAndTheExhaustMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetFreepsCard("riders");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		var wildmen = scn.GetShadowCard("wildmen");
		scn.MoveCompanionsToTable(guard, merry);
		scn.MoveCardsToHand(riders);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, wildmen);

		// No skirmish event could be played in the maneuver phase, so neither can the Riders.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(riders));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{merry, wildmen});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(riders));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(wildmen));

		scn.FreepsPlayCard(riders);
		// They join the fellowship, not the skirmish.
		assertInZone(Zone.FREE_CHARACTERS, riders);
		assertFalse(scn.IsCharSkirmishing(riders));
		assertTrue(scn.IsCharSkirmishingAgainst(guard, troop));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(riders));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(wildmen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedInASkirmishItExhaustsAChosenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var riders = scn.GetFreepsCard("riders");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		var wildmen = scn.GetShadowCard("wildmen");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard, merry);
		scn.MoveCardsToHand(riders);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, wildmen, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, wildmen}, new PhysicalCardImpl[]{merry, troop}, new PhysicalCardImpl[]{frodo, runner});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(wildmen));
		assertEquals(0, scn.GetWoundsOn(runner));

		scn.FreepsPlayCard(riders);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(riders));
		scn.FreepsAcceptOptionalTrigger();

		// A [dunland] Man in this skirmish and a [sauron] Orc in a skirmish still to come; not the Goblin Runner,
		// who is exhausted already (vitality 1), and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wildmen, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, guard, merry, riders, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Vitality 4: exhausted by 3 exertions (two would be "exert twice", one a wound).
		assertEquals(3, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(wildmen));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnotherRohanCompanionPlayedInASkirmishWithTheRidersInPlayExhaustsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetFreepsCard("riders");
		var guard = scn.GetFreepsCard("guard");
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(riders, guard);
		scn.MoveCardsToHand(erkenbrand);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(erkenbrand));

		scn.FreepsPlayCard(erkenbrand);
		assertInZone(Zone.FREE_CHARACTERS, erkenbrand);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(riders));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
