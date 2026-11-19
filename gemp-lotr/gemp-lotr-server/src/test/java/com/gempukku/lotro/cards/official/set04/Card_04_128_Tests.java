package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("errand", "4_128");
					put("errand2", "4_128");
					put("boromir", "0_65");       // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7, vitality 3; keywords only
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man, strength 7; bars only Shadow skirmish actions
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound Man, strength 8; a maneuver ability only

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5; damage +1 only
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2, site 6; answers a weapon played on him
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4; harmless when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewErrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: New Errand
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Spot a Ring-bound Man to make a minion's site number +2. If that minion loses this skirmish, you may wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("errand");

		assertEquals("New Errand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AnyMinionsSiteNumberIsPlus2ForThisSkirmishAndNoWoundComesUnlessThatMinionLosesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var errand = scn.GetFreepsCard("errand");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToHand(errand);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(errand);
		scn.MoveMinionsToTable(savage, chieftain, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, savage},
				new PhysicalCardImpl[]{aragorn, chieftain});
		// The Runner is left in no skirmish.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(boromir);
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.FreepsPlayAvailable(errand));
		scn.FreepsPlayCard(errand);

		// Any minion: the one in this skirmish, one waiting for another, one in no skirmish; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, chieftain, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chieftain);
		assertInDiscard(errand);
		assertEquals(8, scn.GetMinionSiteNumber(chieftain));
		assertEquals(5, scn.GetMinionSiteNumber(savage));

		// Still in force at the next decision of this skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetMinionSiteNumber(chieftain));

		// 7 against 5: Boromir wins, but the minion that loses is not the one chosen.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Aragorn's skirmish is a phase of its own: the +2 is gone.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		// 8 against 7: the chosen minion loses, but not "this skirmish": no wound is offered.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IfThatMinionLosesThisSkirmishAMinionMayBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var errand = scn.GetFreepsCard("errand");
		var errand2 = scn.GetFreepsCard("errand2");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToHand(errand, errand2);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(errand, errand2);
		scn.MoveMinionsToTable(savage, chieftain, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, savage},
				new PhysicalCardImpl[]{aragorn, chieftain});
		scn.ShadowDeclineAssignments();

		// Boromir's skirmish: the Uruk-hai he skirmishes is chosen, and loses.  The wound is offered and declined.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPlayCard(errand);
		scn.FreepsChooseCard(savage);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Aragorn's skirmish: the Orc he skirmishes is chosen, and loses.  The wound is taken.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPlayCard(errand2);
		scn.FreepsChooseCard(chieftain);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Any minion, of three cultures and races, the exhausted ones among them; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, chieftain, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		// Vitality 1: the wound kills it.
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IfThatMinionWinsThisSkirmishNoWoundIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var errand = scn.GetFreepsCard("errand");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(errand);

		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(errand);
		scn.MoveMinionsToTable(chieftain, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(faramir);

		// Faramir is a Ring-bound Man of another title.
		assertTrue(scn.FreepsPlayAvailable(errand));
		scn.FreepsPlayCard(errand);
		scn.FreepsChooseCard(chieftain);
		assertEquals(8, scn.GetMinionSiteNumber(chieftain));

		// 7 against 7 goes to the Shadow side: the chosen minion wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutARingBoundMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var errand = scn.GetFreepsCard("errand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(errand);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(errand);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// Frodo is Ring-bound and no Man; Aragorn is a Man and unbound.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(errand));
		assertInHand(errand);
	}
}
