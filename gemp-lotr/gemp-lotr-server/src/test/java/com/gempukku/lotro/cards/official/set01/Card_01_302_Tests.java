package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_302_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_302");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; his maneuver ability is not used
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion, strength 6; his own skirmish ability is not used
					put("boromir", "1_97");     // Boromir, Son of Denethor: "Skirmish: Exert Boromir to make a Hobbit strength +3"
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally, who is no companion; a fellowship ability only
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Merry, Friend to Sam
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Skirmish:</b> If Merry is not assigned to a skirmish, exert him twice to add his strength to another companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Friend to Sam", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void SkirmishAbilityExertsMerryTwiceToAddHisStrengthToAnotherCompanionForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var maggot = scn.GetFreepsCard("maggot");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, aragorn, gimli);
		scn.MoveAlliesToTable(maggot);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{gimli, runner2});

		// Aragorn's skirmish.  Merry is assigned to no skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		// Another companion: the one skirmishing, one waiting for his skirmish and the Ring-bearer, who is in none.
		// Not Merry himself, not an ally and not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, maggot, runner1, runner2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// 8 + Merry's strength of 3
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn beats his Runner.  In the next skirmish, a phase of its own, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileMerryWaitsForHisSkirmishOrFightsItAndIsOnceItIsResolved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(merry, aragorn, gimli);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{merry, runner2},
				new PhysicalCardImpl[]{gimli, runner3});

		// Aragorn's skirmish: Merry is assigned to a skirmish that is still to come.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssigned(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();

		// Nor in Merry's own skirmish.  His Runner, strength 5, beats him and he takes a wound.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));

		// Gimli's skirmish: Merry's has been resolved, so he is assigned to none, and the ability is offered.
		scn.FreepsResolveSkirmish(gimli);
		assertFalse(scn.IsCharAssigned(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		scn.FreepsChooseCard(gimli);
		assertEquals(3, scn.GetWoundsOn(merry));
		// 6 + Merry's strength of 3
		assertEquals(9, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenMerryCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		// Vitality 4 with 2 wounds: he can exert once, not twice.
		scn.AddWoundsToChar(merry, 2);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(2, scn.GetVitality(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void AddsMerrysStrengthAsItIsWhenUsedAndNotWhatItBecomesLater() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(merry, aragorn, boromir);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		// Boromir makes Merry strength 6 before he uses his ability.
		scn.FreepsUseCardAction(boromir);
		scn.FreepsChooseCard(merry);
		assertEquals(6, scn.GetStrength(merry));
		scn.ShadowPass();

		scn.FreepsUseCardAction(merry);
		scn.FreepsChooseCard(aragorn);
		// 8 + Merry's strength of 6 at that time
		assertEquals(14, scn.GetStrength(aragorn));
		scn.ShadowPass();

		// Merry made stronger again later in the same skirmish: what Aragorn was given does not grow.
		scn.FreepsUseCardAction(boromir);
		scn.FreepsChooseCard(merry);
		assertEquals(9, scn.GetStrength(merry));
		assertEquals(14, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
