package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
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
		 * Game Text: Exert a Wraith to make a twilight minion strength +2. If that minion wins its skirmish you may
		 * 	exert it to add a burden.
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
	public void ExertsAWraithToMakeATwilightMinionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var cover = scn.GetShadowCard("cover");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(aragorn, deadman);
		scn.MoveCardsToHand(gaze);
		scn.MoveCardsToSupportArea(cover);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, nelya, undead);

		// Not in the assignment phase, the last one before the skirmishes in which the Shadow player may act.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gaze));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(gaze));
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(undead));
		assertEquals(7, scn.GetStrength(lights));
		assertEquals(10, scn.GetStrength(nelya));

		scn.ShadowPlayCard(gaze);
		// A Wraith: of either culture, twilight or not, a minion or the other player's companion.  Not a Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, undead, deadman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(undead);
		assertEquals(1, scn.GetWoundsOn(undead));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(deadman));

		// A twilight minion: of either culture and race, in the skirmish or not.  Not a Wraith that is not
		// twilight, not a twilight condition, not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(undead, cover, deadman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lights);
		assertEquals(9, scn.GetStrength(lights));
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
		scn.ShadowChooseCard(nelya);
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(7, scn.GetStrength(lights));
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
		// Corpse Lights is the only Wraith and exerts without a choice.
		assertEquals(1, scn.GetWoundsOn(lights));
		scn.ShadowChooseCard(nelya);
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
		scn.ShadowChooseCard(undead);
		scn.ShadowChooseCard(nelya);
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
		// The one Wraith pays without a choice, and may be the minion made stronger as well.
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lights, nelya));
		scn.ShadowChooseCard(lights);
		assertEquals(9, scn.GetStrength(lights));

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

		// Corpse Lights, vitality 2, exerts for the event and is the minion made stronger.
		scn.ShadowPlayCard(gaze);
		scn.ShadowChooseCard(lights);
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
	public void TheBonusAndTheOfferLastOnlyForTheSkirmishTheEventIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var wisp = scn.GetShadowCard("wisp");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, nelya, undead);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, wisp}, new PhysicalCardImpl[]{gimli, nelya});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Played in Aragorn's skirmish against the Wisp, on Nelya, who is waiting for hers.
		scn.ShadowPlayCard(gaze);
		scn.ShadowChooseCard(undead);
		scn.ShadowChooseCard(nelya);
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(wisp));

		// The Wisp's 8 against Aragorn's 8: the Shadow wins a tie.  The Wisp is another twilight minion
		// winning its skirmish, and brings no offer.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(wisp));

		// The skirmish the event was played in is over, and so is the event.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(10, scn.GetStrength(nelya));
		scn.FreepsPass();
		scn.ShadowPass();
		// 10 against Gimli's 6: Nelya wins her skirmish, and nothing is offered.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoTwilightMinionTheEventIsStillPlayedAndTheWraithExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze1");
		var undead = scn.GetShadowCard("undead");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gaze);

		scn.StartGame();
		// A Wraith to exert, and no twilight minion anywhere.
		scn.MoveMinionsToTable(undead);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, undead);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(gaze));
		assertEquals(9, scn.GetStrength(undead));
		scn.ShadowPlayCard(gaze);
		assertEquals(1, scn.GetWoundsOn(undead));
		// Enduring: strength +2 for the wound it now has, and nothing from the event.
		assertEquals(11, scn.GetStrength(undead));
		assertInDiscard(gaze);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 11 against 8: the Undead wins, and is not the minion of any event.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(undead));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
