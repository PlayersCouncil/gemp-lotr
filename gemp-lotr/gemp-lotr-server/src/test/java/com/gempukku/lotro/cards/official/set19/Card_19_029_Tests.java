package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "19_29");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6; his skirmish ability is not used
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, no companion; a skirmish ability used only on purpose

					// [shire] cards of three types, for the discard pile
					put("intuition", "1_296");  // Hobbit Intuition: a [shire] Skirmish event
					put("tabag", "1_317");      // There and Back Again: a [shire] condition
					put("leaf", "1_300");       // Longbottom Leaf: a [shire] possession
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition, no [shire] card
					// Stone Trolls: a [shire] condition; the Shadow player's copy lies in his own discard pile
					put("trolls", "1_314");

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreeSkirmishesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "19_29");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; his skirmish ability is not used
					put("intuition", "1_296");  // Hobbit Intuition: a [shire] Skirmish event, for the discard pile
					put("tabag", "1_317");      // There and Back Again: a [shire] condition, for the discard pile
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
		 * Set: 19
		 * Name: Merry, Resolute Friend
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Fellowship</b>.<br><b>Skirmish:</b> If Merry is not assigned to a skirmish, take 2 [shire] cards from your discard pile and remove them from the game to make a companion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Resolute Friend", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void UnassignedMerryRemovesTwoShireCardsFromTheDiscardPileToMakeACompanionStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		var intuition = scn.GetFreepsCard("intuition");
		var tabag = scn.GetFreepsCard("tabag");
		var leaf = scn.GetFreepsCard("leaf");
		var saga = scn.GetFreepsCard("saga");
		var shadowTrolls = scn.GetShadowCard("trolls");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(merry, aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToDiscard(intuition, tabag, leaf, saga, shadowTrolls);

		scn.StartGame();
		assertInZone(Zone.DISCARD, intuition, tabag, leaf, saga, shadowTrolls);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		// The cost: exactly 2 of the Free Peoples player's own [shire] cards, of whatever type.
		assertTrue(scn.FreepsHasCardChoiceAvailable(intuition, tabag, leaf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saga, shadowTrolls));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(intuition, tabag);
		assertInZone(Zone.REMOVED, intuition, tabag);
		assertInZone(Zone.DISCARD, leaf, saga, shadowTrolls);

		// The effect: any companion, the one skirmishing, one assigned to a later skirmish and Merry himself.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, merry, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, savage1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(gimli));
		// Still in force at the Shadow player's answer in the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);

		// The next skirmish is a phase of its own: the bonus is gone.  One [shire] card is left in his discard pile,
		// beside a [gondor] card and the Shadow player's [shire] card: the cost cannot be paid.
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.IsCharAssigned(merry));
		assertInZone(Zone.DISCARD, leaf, saga, shadowTrolls);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void NotOfferedWhileMerryIsAssignedToASkirmishAndOfferedOnceHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeSkirmishesScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var intuition = scn.GetFreepsCard("intuition");
		var tabag = scn.GetFreepsCard("tabag");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(merry, aragorn, gimli);
		scn.MoveCardsToDiscard(intuition, tabag);

		scn.StartGame();
		assertInZone(Zone.DISCARD, intuition, tabag);
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{merry, runner2},
				new PhysicalCardImpl[]{gimli, runner3});

		// Aragorn's skirmish: Merry is assigned to a skirmish still to come.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssigned(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();

		// Nor in his own.  Then Merry, strength 3, loses to his Runner, strength 5, and takes a wound.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));

		// Gimli's skirmish: Merry's is over, he is assigned to nothing, and the ability is offered.
		scn.FreepsResolveSkirmish(gimli);
		assertFalse(scn.IsCharAssigned(merry));
		assertInZone(Zone.DISCARD, intuition, tabag);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));
	}
}
