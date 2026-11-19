package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("disquiet", "2_2");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6, vitality 3; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, vitality 2; no text but its own play requirement
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion who is no Dwarf, strength 7; a skirmish ability only
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not reached by a cheat
					put("runner2", "1_178");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; its text answers a weapon played on it
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, no Orc; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DisquietofOurPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Disquiet of Our People
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a Dwarf to make that Dwarf <b>defender +1</b> (or <b>defender +2</b> if you spot an Orc) until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("disquiet");

		assertEquals("Disquiet of Our People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoOrcToSpotItExertsTheChosenDwarfToMakeHimDefenderPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var disquiet = scn.GetFreepsCard("disquiet");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, guard, boromir);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(disquiet);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Vitality 2 with 1 wound: the guard is exhausted and cannot exert.
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(disquiet));

		scn.FreepsPlayCard(disquiet);

		// A Dwarf companion and a Dwarf ally; not an exhausted Dwarf, nor a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// An Uruk-hai is no Orc: +1. The other Dwarf is not made a defender.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertInDiscard(disquiet);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAMoriaOrcToSpotTheDwarfIsDefenderPlusTwoUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var disquiet = scn.GetFreepsCard("disquiet");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);
		scn.MoveCardsToHand(disquiet);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		// Gimli is the only Dwarf, so he is not asked for.
		scn.FreepsPlayCard(disquiet);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner1}, new PhysicalCardImpl[]{gimli, runner2});

		// Boromir's skirmish first, and then Gimli's: still defender +2 in the second skirmish.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner2));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gone as the regroup phase starts.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithASauronOrcToSpotTheDwarfAllyIsDefenderPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var disquiet = scn.GetFreepsCard("disquiet");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(disquiet);
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.FreepsPlayCard(disquiet);
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(2, scn.GetKeywordCount(grimir, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var disquiet = scn.GetFreepsCard("disquiet");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);
		scn.MoveCardsToHand(disquiet);
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted. Boromir could exert, but is no Dwarf.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInHand(disquiet);
		assertFalse(scn.FreepsPlayAvailable(disquiet));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var disquiet = scn.GetFreepsCard("disquiet");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(disquiet);
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(disquiet);
		assertTrue(scn.FreepsPlayAvailable(disquiet));

		scn.SkipToPhase(Phase.ARCHERY);
		assertInHand(disquiet);
		assertFalse(scn.FreepsPlayAvailable(disquiet));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
