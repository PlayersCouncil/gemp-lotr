package com.gempukku.lotro.cards.official.set06;

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

public class Card_06_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anborn", "6_48");
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man, strength 7, vitality 3; keywords only
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, site 4; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4; cheated in, so its play trigger never fires
					// Uruk Seeker: fierce, strength 6, vitality 2, site 5; its other text is only against a bearer of a search card
					put("seeker", "4_195");
					put("seeker2", "4_195");
					// Shelob, Last Child of Ungoliant: fierce, enduring, strength 8, vitality 8, site 8; lives through two lost skirmishes
					put("shelob", "8_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnbornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Anborn, Skilled Huntsman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>.<br>Each time Anborn wins a skirmish, you may make a minion's site number +3 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anborn");

		assertEquals("Anborn", card.getBlueprint().getTitle());
		assertEquals("Skilled Huntsman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WinningASkirmishHeMayMakeAnyMinionSiteNumberPlus3UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anborn = scn.GetFreepsCard("anborn");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(anborn, boromir);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{anborn, savage}, new PhysicalCardImpl[]{boromir, troop});
		scn.FreepsResolveSkirmish(anborn);
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		// 6 against 5: Anborn wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anborn));
		scn.FreepsAcceptOptionalTrigger();

		// Any minion: the one he beat, and one of another culture and race whose skirmish is still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(anborn, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(7, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Still +3 in the next skirmish.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetMinionSiteNumber(troop));

		// 13 against 7, not doubled: Boromir loses and lives; the regroup phase begins, and the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetMinionSiteNumber(troop));
	}

	@Test
	public void DecliningTheOfferChangesNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anborn = scn.GetFreepsCard("anborn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(anborn, boromir);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{anborn, savage}, new PhysicalCardImpl[]{boromir, troop});
		scn.FreepsResolveSkirmish(anborn);
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anborn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedWhenAnbornLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anborn = scn.GetFreepsCard("anborn");
		scn.MoveCompanionsToTable(anborn);
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(anborn, seeker);
		scn.FreepsResolveSkirmish(anborn);
		assertEquals(5, scn.GetMinionSiteNumber(seeker));

		// 6 against 6: a tie goes to the Shadow side, and Anborn lives through one wound.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(anborn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(anborn));
		assertEquals(5, scn.GetMinionSiteNumber(seeker));
		// The Seeker is fierce: the fierce assignment phase comes next.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenNoMinionIsLeftInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anborn = scn.GetFreepsCard("anborn");
		scn.MoveCompanionsToTable(anborn);
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(anborn, runner);
		scn.FreepsResolveSkirmish(anborn);

		// 6 against 5: Anborn wins, and the wound kills the Runner, the only minion.
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(anborn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinIsAnotherPlus3EvenOnTheSameMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anborn = scn.GetFreepsCard("anborn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(anborn, boromir);
		var shelob = scn.GetShadowCard("shelob");
		var seeker2 = scn.GetShadowCard("seeker2");

		scn.StartGame();
		// A strength that only arranges the board: 12 against Shelob's 8, then against her 10 (enduring, one wound),
		// without doubling either.
		scn.ApplyAdHocModifier(new StrengthModifier(null, anborn, null, 6));
		scn.MoveMinionsToTable(shelob, seeker2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{anborn, shelob}, new PhysicalCardImpl[]{boromir, seeker2});
		scn.FreepsResolveSkirmish(anborn);
		assertEquals(8, scn.GetMinionSiteNumber(shelob));

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anborn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(shelob);
		assertEquals(11, scn.GetMinionSiteNumber(shelob));

		// 7 against 6: Boromir wins, and the second Seeker lives through its wound.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker2));
		assertTrue(scn.InFierceRound());
		assertEquals(11, scn.GetMinionSiteNumber(shelob));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{anborn, shelob}, new PhysicalCardImpl[]{boromir, seeker2});
		scn.FreepsResolveSkirmish(anborn);
		scn.PassFierceSkirmishActions();

		// The fierce win offers it again: with Boromir's fierce skirmish still waiting, 8 + 3 + 3.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anborn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(shelob));
		scn.FreepsChooseCard(shelob);
		assertEquals(14, scn.GetMinionSiteNumber(shelob));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
