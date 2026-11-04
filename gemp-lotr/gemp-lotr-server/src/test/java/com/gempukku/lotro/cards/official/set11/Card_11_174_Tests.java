package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sworn", "11_174");
					put("gimli", "11_8");       // Gimli, Lively Combatant: [dwarven] companion, resistance 7; an optional draw on a win
					put("farin", "11_7");       // Farin, Emissary of Erebor: [dwarven] companion, resistance 6; muster only at a battleground or underground
					put("pippin", "103_31");    // Pippin, Trollslayer: a Hobbit companion who is [gondor], resistance 6; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, resistance 6; an assignment ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a Hobbit minion of [men]; his abilities are used only on purpose
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not set off by a cheat
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sworn", "11_174");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("grimir", "11_10");     // Grimir, Dwarven Emissary: [dwarven] companion, resistance 7; a response to a wound in a skirmish, not met here
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5; its play trigger is not set off by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,       // Gimli, Bearer of Grudges: a Dwarf Ring-bearer, so that no Hobbit is in play
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwornCompanionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Sworn Companion
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Hobbit strength +1 for each companion who has resistance 7 or more.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sworn");

		assertEquals("Sworn Companion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAHobbitOfAnyKindStrengthPlus1ForEachCompanionOfResistance7OrMoreUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sworn = scn.GetFreepsCard("sworn");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(gimli, farin, pippin, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sworn);
		assertInHand(sworn);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, runner1},
				new PhysicalCardImpl[]{gimli, runner2});
		// Ted Sandyman is left in play, assigned to no skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// Frodo (10) and Gimli (7) have resistance 7 or more; Farin, Pippin and Smeagol (6) have not.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(7, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(farin));
		assertEquals(4, scn.GetStrength(frodo));
		assertInHand(sworn);

		assertTrue(scn.FreepsPlayAvailable(sworn));
		scn.FreepsPlayCard(sworn);
		// Any Hobbit: the Ring-bearer in this skirmish, a [gondor] Hobbit companion in none, a Hobbit ally and the
		// opponent's Hobbit minion; not Smeagol, a [shire] companion who is no Hobbit, nor Gimli.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);

		assertInDiscard(sworn);
		// 3 + 1 for the Ring + 2 companions of resistance 7 or more
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Frodo, 6 against 5, wins; in the next skirmish his bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(frodo));
	}

	@Test
	public void ABurdenLeavesOneCompanionOfResistance7OrMoreAndTheBonusIsPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sworn = scn.GetFreepsCard("sworn");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var pippin = scn.GetFreepsCard("pippin");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(gimli, farin, pippin);
		scn.MoveCardsToHand(sworn);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, runner1);
		scn.FreepsResolveSkirmish(pippin);

		// One burden: Frodo 9 and Gimli 6, Farin 5.
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(farin));
		assertEquals(3, scn.GetStrength(pippin));

		scn.FreepsPlayCard(sworn);
		scn.FreepsChooseCard(pippin);

		assertInDiscard(sworn);
		assertEquals(4, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sworn = scn.GetFreepsCard("sworn");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(sworn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(sworn);
		assertFalse(scn.FreepsPlayAvailable(sworn));
	}

	@Test
	public void WithNoHobbitInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var gimli = scn.GetRingBearer();
		var sworn = scn.GetFreepsCard("sworn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(aragorn, grimir);
		scn.MoveCardsToHand(sworn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);
		// Grimir has resistance 7, so the count is 1 with nothing to make stronger.
		assertEquals(7, scn.GetResistance(grimir));
		assertEquals(8, scn.GetStrength(aragorn));
		// 6 + 1 for the Ring
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(grimir));

		assertTrue(scn.FreepsPlayAvailable(sworn));
		scn.FreepsPlayCard(sworn);

		assertInDiscard(sworn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(grimir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
