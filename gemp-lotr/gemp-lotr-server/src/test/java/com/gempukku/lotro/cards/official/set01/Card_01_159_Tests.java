package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_159_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rampage", "1_159");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1; no other text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2, damage +1; no other text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1; no other text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability used only on purpose
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard (the culture without the race); each companion
					// is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiRampageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Rampage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an Uruk-hai. Plays to your support area.
		 * <b>Response:</b> If your Uruk-hai wins a skirmish, remove (3) to make him <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rampage");

		assertEquals("Uruk-hai Rampage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampage = scn.GetShadowCard("rampage");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(rampage, savage);

		scn.StartGame();
		// An [isengard] Orc and an [uruk-hai] Wizard: neither is an Uruk-hai.
		scn.MoveMinionsToTable(worker, saruman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rampage));

		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.ShadowPlayAvailable(rampage));

		scn.ShadowPlayCard(rampage);
		assertInZone(Zone.SUPPORT, rampage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedBesideAnUrukHaiOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampage = scn.GetShadowCard("rampage");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(rampage);

		scn.StartGame();
		// A [sauron] Uruk-hai alone.
		scn.MoveMinionsToTable(guard);
		scn.SetTwilight(10);
		assertInHand(rampage);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(rampage));

		scn.ShadowPlayCard(rampage);
		assertInZone(Zone.SUPPORT, rampage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachUrukHaiThatWinsMayBeMadeFierceForThreeAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampage = scn.GetShadowCard("rampage");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var sentry = scn.GetShadowCard("sentry");
		var worker = scn.GetShadowCard("worker");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(rampage);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, sentry, worker);

		// Gimli faces two Uruk-hai of two cultures and an [isengard] Orc; the Sentry waits for Aragorn's skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowAssignToMinions(gimli, savage, guard, worker);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(6);
		scn.PassSkirmishActions();

		// 5 + 9 + 5 against 6: Gimli is overwhelmed, and all three minions win.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage, "Uruk Savage"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage, "Cirith Ungol Guard"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rampage, "Isengard Worker"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rampage, "Cirith Ungol Sentry"));

		scn.ShadowChooseOptionalTrigger(rampage, "Uruk Savage");
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(worker, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage, "Cirith Ungol Guard"));
		scn.ShadowChooseOptionalTrigger(rampage, "Cirith Ungol Guard");
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(worker, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedWhenAnUrukHaiLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampage = scn.GetShadowCard("rampage");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rampage);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.PassSkirmishActions();

		// 5 against 8: the Savage loses and lives (vitality 3, 1 wound); the pool could pay.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rampage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyTwoTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampage = scn.GetShadowCard("rampage");
		var guard = scn.GetShadowCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rampage);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(2);
		scn.PassSkirmishActions();

		// 9 against 6: the Guard wins, with one token short of the cost in the pool.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rampage));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWinnerIsFierceThroughTheFierceSkirmishWhereItIsOfferedAgainAndNotInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rampage = scn.GetShadowCard("rampage");
		var guard = scn.GetShadowCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(rampage);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(3);
		scn.PassSkirmishActions();

		// 9 against 6: the Guard wins (Gimli takes 1 + damage +1 and lives).
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));

		// Made fierce, the Guard has a fierce skirmish.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));

		// Fierce already, the Guard wins again with (3) in the pool: the response is offered again, and the cost
		// is paid for a keyword he has.
		scn.SetTwilight(3);
		scn.PassFierceSkirmishActions();
		// 9 against 8: the Guard wins again.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rampage));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
	}
}
