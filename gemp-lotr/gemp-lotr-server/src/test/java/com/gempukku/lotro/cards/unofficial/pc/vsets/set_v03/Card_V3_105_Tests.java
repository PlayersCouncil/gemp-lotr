package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("driven", "103_105");
					put("merry", "1_302");
					put("sword", "1_299");
					put("sting", "1_313");
					put("runner", "1_178");
					put("savage", "1_151");
					put("gpt", "1_177");
					put("sam", "1_311");
					put("pippin", "1_306");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrivenByHerOwnCruelWillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Driven By Her Own Cruel Will
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a skirmishing Hobbit bearing a hand weapon (except the Ring-bearer) strength +X, where X is the total vitality of all characters in that skirmish.  At the end of that skirmish, hinder a minion involved in that skirmish if you cannot spot more than 4 companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("driven");

		assertEquals("Driven By Her Own Cruel Will", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrivenByHerOwnCruelWillRequiresHandWeaponOnSkirmishingHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(driven);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass(); // move to site 3

		// Assignment: Merry vs Runner
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(merry, runner);

		// In skirmish — Merry has no hand weapon, event should not work
		int str = scn.GetStrength(merry);
		scn.FreepsPlayCard(driven);
		assertEquals(str, scn.GetStrength(merry));
	}

	@Test
	public void DrivenByHerOwnCruelWillCannotTargetRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var sting = scn.GetFreepsCard("sting");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();

		// Give Frodo a hand weapon (Sting) — he's the only skirmishing Hobbit with one
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(driven);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass(); // move to site 3

		// Assignment: Frodo vs Runner
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(frodo, runner);

		// In skirmish — Frodo has Sting but is Ring-bearer, so event should not work
		int str = scn.GetStrength(frodo);
		scn.FreepsPlayCard(driven);
		assertEquals(str, scn.GetStrength(frodo));
	}

	@Test
	public void StrengthBonusCountsCurrentVitalityOfAllCharactersInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner, savage);
		scn.AddWoundsToChar(savage, 1);

		scn.FreepsPass(); // move to site 3

		// FP declines assignments, Shadow piles both minions on Merry
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(merry, runner, savage);
		scn.FreepsResolveSkirmish(merry);

		// In skirmish: play the event targeting Merry
		// Merry vit 4 + Runner vit 1 + Savage current vit (3 - 1 wound = 2) = 7
		// Merry strength: 3 (base) + 2 (sword) + 7 (event) = 12
		scn.FreepsPlayCard(driven);

		assertEquals(12, scn.GetStrength(merry));
	}

	@Test
	public void HindersMinionAtEndOfSkirmishWithFewCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var gpt = scn.GetShadowCard("gpt");

		// 2 companions total (Frodo + Merry), well under 5
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gpt);

		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(merry, gpt);

		// Play event in skirmish, then let skirmish resolve
		scn.FreepsPlayCard(driven);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// At end of skirmish, hinder trigger fires — Savage is only minion, auto-chosen
		assertTrue(scn.IsHindered(gpt));
	}

	@Test
	public void DoesNotHinderWith5OrMoreCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");

		// 5 companions: Frodo + Merry + Sam + Pippin + Aragorn
		scn.MoveCompanionsToTable(merry, sam, pippin, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(merry, savage);

		// Play event in skirmish, then let skirmish resolve
		scn.FreepsPlayCard(driven);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// Skirmish ends — with 5 companions, hinder should NOT trigger
		assertFalse(scn.IsHindered(savage));
	}
}
