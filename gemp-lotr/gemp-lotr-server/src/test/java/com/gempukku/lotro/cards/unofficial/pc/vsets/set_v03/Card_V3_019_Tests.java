package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_019_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "103_19");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man, unbound; a skirmish ability never used here
					put("ranger", "7_115");     // Ranger of Minas Tirith: a second [gondor] Man, unbound; his text needs a skirmish
					put("eomer", "4_267");      // Éomer, Third Marshal of Riddermark: a [rohan] Man, unbound; his text needs a skirmish
					put("ithilien", "4_130");   // Ranger of Ithilien: a [gondor] Man who is Ring-bound; his text needs a skirmish
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man who is an ally; a fellowship ability never used here
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf, unbound and no Man
					put("deadman", "10_27");    // Dead Man of Dunharrow: a [gondor] companion who is a Wraith and no Man

					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man who is a minion, with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Aragorn, King of Gondor and Arnor
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Aragorn is strength +1 for each culture among other Men you can spot.
		 * 	Each time the fellowship moves, you may exert Aragorn and hinder another unbound Man to remove a threat or a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("King of Gondor and Arnor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForFiveAtStrengthEightAndNineOnceAnotherGondorManIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(aragorn, boromir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		scn.FreepsPlayCard(aragorn);

		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// He is a [gondor] Man himself, and does not count: only other Men do.
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(boromir);

		// 8 + 1 for [gondor], Boromir's culture
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCultureAmongOtherMenCountsOnceWhetherTheManIsACompanionAnAllyOrAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var ranger = scn.GetFreepsCard("ranger");
		var eomer = scn.GetFreepsCard("eomer");
		var ithilien = scn.GetFreepsCard("ithilien");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		var deadman = scn.GetFreepsCard("deadman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(aragorn));

		// A [gondor] companion who is a Wraith and a [dwarven] companion who is a Dwarf: no Man among them.
		scn.MoveCompanionsToTable(deadman, gimli);
		assertEquals(8, scn.GetStrength(aragorn));

		// [gondor]: the first Man down is Ring-bound, and "other Men" does not say unbound.
		scn.MoveCompanionsToTable(ithilien);
		assertEquals(9, scn.GetStrength(aragorn));

		// Two more [gondor] Men: the culture has been counted already.
		scn.MoveCompanionsToTable(boromir, ranger);
		assertEquals(9, scn.GetStrength(aragorn));

		// [gondor] and [rohan]
		scn.MoveCompanionsToTable(eomer);
		assertEquals(10, scn.GetStrength(aragorn));

		// [gondor], [rohan] and [gandalf]: Barliman is an ally.
		scn.MoveAlliesToTable(barliman);
		assertEquals(11, scn.GetStrength(aragorn));

		// [gondor], [rohan], [gandalf] and [dunland]: the text says "other Men", of either side.
		scn.MoveMinionsToTable(savage);
		assertEquals(12, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingMayExertAragornAndHinderAnotherUnboundManToRemoveAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		var ithilien = scn.GetFreepsCard("ithilien");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir, eomer, ithilien, gimli);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddThreats(2);
		scn.AddBurdens(2);

		// 8 + [gondor], [rohan], [gandalf] and [dunland]
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(eomer));

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsChooseOptionalTrigger(aragorn);

		// Another unbound Man: not Aragorn, not the Ring-bound Man, not the ally, not the minion, not the Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, eomer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, ithilien, barliman, savage, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eomer);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(eomer));
		assertFalse(scn.IsHindered(boromir));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());

		assertTrue(scn.FreepsChoiceAvailable("threat"));
		assertTrue(scn.FreepsChoiceAvailable("burden"));
		scn.FreepsChoose("threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		// Éomer was the only [rohan] Man, and a hindered Man cannot be spotted as one: 8 + [gondor], [gandalf] and [dunland]
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingTheBurdenRemovesABurdenAndLeavesTheThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(aragorn, boromir, eomer);

		scn.StartGame();
		scn.AddThreats(2);
		scn.AddBurdens(2);

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsChooseOptionalTrigger(aragorn);
		scn.FreepsChooseCard(boromir);

		assertTrue(scn.FreepsChoiceAvailable("threat"));
		assertTrue(scn.FreepsChoiceAvailable("burden"));
		scn.FreepsChoose("burden");

		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(eomer));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveOffersItAgainAndTheOneTokenThereIsToRemoveIsRemovedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(aragorn, boromir, eomer);

		scn.StartGame();
		// A burden and no threat.
		scn.AddBurdens(1);

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsChooseOptionalTrigger(aragorn);
		scn.FreepsChooseCard(boromir);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		// A threat and no burden.
		scn.AddThreats(1);
		scn.FreepsChooseToMove();

		// The fellowship moves a second time this turn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsChooseOptionalTrigger(aragorn);

		// Boromir is still hindered, which leaves Éomer as the only other unbound Man.
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(eomer));
		assertTrue(scn.IsHindered(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatAndNoBurdenItIsStillOfferedAndItsCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(aragorn, boromir, eomer);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();

		// It has a cost, and the cost can be paid.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsChooseOptionalTrigger(aragorn);
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(boromir));

		// Neither can be done in full, so the game leaves the player to name one, and nothing comes of it.
		scn.FreepsChoose("threat");

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(eomer));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesAragornTheMenTheThreatsAndTheBurdensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(aragorn, boromir, eomer);

		scn.StartGame();
		scn.AddThreats(2);
		scn.AddBurdens(2);

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(eomer));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyOtherMenAreRingBoundOrAnAllyAndTheOnlyOtherUnboundCompanionIsADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var ithilien = scn.GetFreepsCard("ithilien");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, ithilien, gimli);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.AddThreats(2);
		scn.AddBurdens(2);

		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(ithilien));
		assertFalse(scn.IsHindered(barliman));
		assertFalse(scn.IsHindered(gimli));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(aragorn, boromir, eomer);

		scn.StartGame();
		scn.AddThreats(2);
		scn.AddBurdens(2);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(aragorn, 3);

		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(eomer));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyOtherUnboundManIsAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		scn.StartGame();
		scn.AddThreats(2);
		scn.AddBurdens(2);
		scn.HinderCard(boromir);

		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
