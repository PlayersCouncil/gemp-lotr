package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

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

public class Card_V2_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gaze1", "102_66");
					put("gaze2", "102_66");

					// Corpse Lights: a twilight [sauron] Wraith, strength 7, vitality 2.  A skirmish ability, never used.
					put("lights", "6_99");
					// Wisp of Pale Sheen: a twilight [sauron] Wraith, strength 8, vitality 2.  A regroup ability, never used.
					put("wisp", "6_108");
					// Ulaire Nelya, Ringwraith in Twilight: twilight, of the [ringwraith] culture, and a Nazgul, not a
					// Wraith.  Strength 10, vitality 3.  Fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer.
					put("nelya", "2_84");
					// Undead of Angmar: a Wraith that is not twilight, of the [ringwraith] culture.  Strength 9,
					// vitality 3, enduring (strength +2 for each wound on it).  Its other texts need a wounded
					// companion and another [ringwraith] Wraith, or two exertions in the Shadow phase.
					put("undead", "16_6");
					// Cover of Darkness, Omen of Despair: a twilight [sauron] condition, and so twilight without
					// being a minion.  Its ability needs a Troll.
					put("cover", "103_93");

					// Dead Man of Dunharrow: the Free Peoples player's own Wraith, a [gondor] companion, vitality 3.
					// Enduring, and never in a skirmish here; the rest of his text is for when he is played.
					put("deadman", "10_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability, never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrappedinitsGazeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Trapped in its Gaze
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a Wraith to make a skirmishing twilight minion strength +2. If that minion wins its
		 * 	skirmish you may exert it to add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gaze1");

		assertEquals("Trapped in its Gaze", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAWraithToMakeASkirmishingTwilightMinionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var lights = scn.GetShadowCard("lights");
		var wisp = scn.GetShadowCard("wisp");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var cover = scn.GetShadowCard("cover");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(aragorn, gimli, deadman);
		scn.MoveCardsToHand(gaze);
		scn.MoveCardsToSupportArea(cover);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, wisp, nelya, undead);

		// Not in the assignment phase, the last one before the skirmishes in which the Shadow player may act.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gaze));
		scn.ShadowPass();

		// Corpse Lights, the Wisp and the Undead all fight Aragorn.  Nelya is assigned to Gimli, whose skirmish
		// has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lights}, new PhysicalCardImpl[]{gimli, nelya});
		scn.ShadowAssignToMinions(aragorn, wisp, undead);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(gaze));
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(undead));
		assertEquals(7, scn.GetStrength(lights));
		assertEquals(8, scn.GetStrength(wisp));
		assertEquals(10, scn.GetStrength(nelya));

		scn.ShadowPlayCard(gaze);
		// A Wraith: of either culture, twilight or not, a minion or the other player's companion, in the skirmish
		// or out of it.  Not a Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, wisp, undead, deadman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(undead);
		assertEquals(1, scn.GetWoundsOn(undead));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(deadman));

		// A skirmishing twilight minion.  Not Nelya, a twilight minion waiting for a later skirmish; not the
		// Undead, a minion in this skirmish that is not twilight; not a twilight condition; not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, wisp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya, undead, cover, deadman, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lights);
		assertEquals(9, scn.GetStrength(lights));
		assertEquals(8, scn.GetStrength(wisp));
		assertEquals(10, scn.GetStrength(nelya));
		// Enduring gives the Undead +2 for its wound; the event gives it nothing.
		assertEquals(11, scn.GetStrength(undead));
		assertInDiscard(gaze);
		// The event costs nothing.
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AWraithCompanionMayBeTheOneExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(aragorn, deadman);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(deadman));
		scn.ShadowPlayCard(gaze);
		// A Shadow player may exert another player's character to pay a cost of his Shadow card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, deadman));
		scn.ShadowChooseCard(deadman);
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(lights));
		// Corpse Lights is the only twilight minion in the skirmish and is made stronger without a choice.
		assertEquals(9, scn.GetStrength(lights));
		assertEquals(10, scn.GetStrength(nelya));
		assertInDiscard(gaze);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThatMinionMayExertToAddABurdenWhenItWinsItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, nelya, undead);
		// Aragorn at 13, so that the two minions together beat him without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		// Both twilight minions fight Aragorn, so that both are on the winning side.
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(gaze);
		scn.ShadowChooseCard(undead);
		scn.ShadowChooseCard(lights);
		assertEquals(1, scn.GetWoundsOn(undead));
		assertEquals(9, scn.GetStrength(lights));
		assertEquals(10, scn.GetStrength(nelya));

		// 9 + 10 against 13.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetBurdens());

		scn.ShadowAcceptOptionalTrigger();
		// The minion the event was played on exerts: not the Wraith that paid for the event, and not the other
		// twilight minion that won beside it.
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(1, scn.GetWoundsOn(undead));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBurdenMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(gaze);
		// Corpse Lights is the only Wraith and exerts without a choice; Nelya is the only twilight minion in the
		// skirmish and is made stronger without one.
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(12, scn.GetStrength(nelya));

		// 12 against Aragorn's 8.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenThatMinionLosesItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, nelya, undead);
		// Aragorn at 13, to beat Nelya's 10 + 2 without being anywhere near twice her strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(gaze);
		// The Undead pays.  Nelya is the only twilight minion in the skirmish and is not chosen.
		scn.ShadowChooseCard(undead);
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.FreepsPass();
		scn.ShadowPass();
		// Nelya lost and took the wound for it.  With vitality 3 she could exert, and is not asked to.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedOnceTheOnlyWraithIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze1 = scn.GetShadowCard("gaze1");
		var gaze2 = scn.GetShadowCard("gaze2");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gaze1, gaze2);

		scn.StartGame();
		// Corpse Lights is the only Wraith.  Nelya is twilight and can exert, and is no Wraith.
		scn.MoveMinionsToTable(lights, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(gaze1));
		assertTrue(scn.ShadowPlayAvailable(gaze2));
		assertEquals(0, scn.GetWoundsOn(lights));
		scn.ShadowPlayCard(gaze1);
		// The one Wraith pays without a choice, and is the minion made stronger as well: it is the only twilight
		// minion in the skirmish.
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(9, scn.GetStrength(lights));
		assertEquals(10, scn.GetStrength(nelya));

		// Vitality 2 with 1 wound: Corpse Lights can exert no more, so the second copy cannot be paid for.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gaze2));
	}

	@Test
	public void AWinnerThatCannotExertIsNotOfferedTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Corpse Lights, vitality 2, is the only Wraith and the only twilight minion in the skirmish: it exerts
		// for the event and is the minion made stronger.
		scn.ShadowPlayCard(gaze);
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(9, scn.GetStrength(lights));

		// 9 against 8: it wins, and cannot exert to add the burden.
		scn.FreepsPass();
		scn.ShadowPass();
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBonusAndTheOfferEndWithTheSkirmishTheEventIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, undead);
		// Nelya made fierce, so that she fights a second skirmish in the turn the event is played.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, nelya, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(gaze);
		// The Undead is the only Wraith and Nelya the only twilight minion in the skirmish.
		assertEquals(1, scn.GetWoundsOn(undead));
		assertEquals(12, scn.GetStrength(nelya));

		// 12 against Aragorn's 8: Nelya wins the skirmish the event was played in, and exerts for a burden.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetBurdens());

		// Her fierce skirmish is another skirmish: the bonus is gone.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(nelya));
		scn.FreepsPass();
		scn.ShadowPass();
		// 10 against 8: she wins again, with vitality 3 and 1 wound could exert again, and nothing is offered.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoTwilightMinionInTheSkirmishTheEventIsStillPlayedAndTheWraithExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		// A Wraith to exert, and the only twilight minion waiting for a later skirmish.
		scn.MoveMinionsToTable(undead, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, undead}, new PhysicalCardImpl[]{gimli, nelya});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(gaze));
		assertEquals(9, scn.GetStrength(undead));
		assertEquals(10, scn.GetStrength(nelya));
		scn.ShadowPlayCard(gaze);
		assertEquals(1, scn.GetWoundsOn(undead));
		// Enduring: strength +2 for the wound it now has, and nothing from the event.  Nelya is not in this
		// skirmish and gets nothing either.
		assertEquals(11, scn.GetStrength(undead));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInDiscard(gaze);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 11 against 8: the Undead wins, and is not the minion of any event.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(undead));

		// 10 against Gimli's 6: Nelya wins her own skirmish, and is not the minion of any event either.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(10, scn.GetStrength(nelya));
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
