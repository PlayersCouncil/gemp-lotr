package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_144_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bloodlust", "1_144");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1; no other text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1; no other text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2, damage +1; no other text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability only

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukBloodlustStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Bloodlust
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Response:</b> If your Uruk-hai wins a skirmish, remove (1) to heal him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bloodlust");

		assertEquals("Uruk Bloodlust", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachUrukHaiThatWinsIsHealedOnceForOneTwilightAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bloodlust = scn.GetShadowCard("bloodlust");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var guard = scn.GetShadowCard("guard");
		var worker = scn.GetShadowCard("worker");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(bloodlust);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry, guard, worker);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(sentry, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(worker, 1);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(worker));

		// Gimli faces two Uruk-hai of two cultures and an [isengard] Orc; the Guard waits for Aragorn's skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.ShadowAssignToMinions(gimli, savage, sentry, worker);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(3);
		scn.PassSkirmishActions();

		// 5 + 8 + 5 against 6: Gimli is overwhelmed, and all three minions win.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust, "Uruk Savage"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust, "Cirith Ungol Sentry"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bloodlust, "Isengard Worker"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bloodlust, "Cirith Ungol Guard"));

		scn.ShadowChooseOptionalTrigger(bloodlust, "Uruk Savage");
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetWoundsOn(guard));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust, "Cirith Ungol Sentry"));
		scn.ShadowChooseOptionalTrigger(bloodlust, "Cirith Ungol Sentry");
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetWoundsOn(guard));

		// A response may be used again for the same win while its cost can be paid (CR 4.2 "response"):
		// the Savage's last wound is healed with the last twilight token.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust, "Uruk Savage"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust, "Cirith Ungol Sentry"));
		scn.ShadowChooseOptionalTrigger(bloodlust, "Uruk Savage");
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedWhenAnUrukHaiLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bloodlust = scn.GetShadowCard("bloodlust");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(bloodlust);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.PassSkirmishActions();

		// 5 against 8: the Savage loses and lives (vitality 3, now 2 wounds); the pool could pay.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bloodlust));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bloodlust = scn.GetShadowCard("bloodlust");
		var guard = scn.GetShadowCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(bloodlust);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(0);
		scn.PassSkirmishActions();

		// 9 against 6: the Guard wins, wounded, with nothing in the pool.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bloodlust));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUnwoundedWinnerIsOfferedTheResponseAndItHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bloodlust = scn.GetShadowCard("bloodlust");
		var guard = scn.GetShadowCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(bloodlust);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(3);
		scn.PassSkirmishActions();

		// The response has a cost, so it is offered though its winner has no wound to heal; taken, it costs (1).
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(guard));
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Offered again while the pool can pay; declined, the game goes on to the regroup phase.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
