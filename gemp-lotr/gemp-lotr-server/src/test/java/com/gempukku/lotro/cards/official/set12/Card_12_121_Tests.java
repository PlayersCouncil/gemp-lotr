package com.gempukku.lotro.cards.official.set12;

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

public class Card_12_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flotsam", "12_121");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3; an assignment ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion, strength 3; enduring; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ALLY; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, home site 3, strength 4; a maneuver ability used only on purpose
					put("ring", "9_8");         // Ring of Guile: on a Dwarf, strength +1; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is on being played
					put("runner2", "1_178");
					put("scrabbler", "2_64");   // Goblin Scrabbler: [moria] Orc, strength 4; its texts are on being played and on roaming
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion whose race is Hobbit, vitality 4; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlotsamandJetsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Flotsam and Jetsam
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a companion (except a Hobbit) wins a skirmish, heal a Hobbit.
		*/

		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");

		assertEquals("Flotsam and Jetsam", flotsam.getBlueprint().getTitle());
		assertNull(flotsam.getBlueprint().getSubtitle());
		assertFalse(flotsam.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, flotsam.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, flotsam.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, flotsam.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(flotsam, Timeword.RESPONSE));
		assertEquals(0, flotsam.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAragornWinsAnyWoundedHobbitMayBeHealedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(flotsam);
		scn.MoveCompanionsToTable(aragorn, smeagol, pippin);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, ted);
		scn.AddWoundsToChar(ted, 1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(ted));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		// Ted is left over and stays out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(flotsam));

		// Aragorn, 8, beats the Runner, 5.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.FreepsPlayAvailable(flotsam));
		scn.FreepsPlayCard(flotsam);

		// Wounded Hobbits of each side, culture and kind; not the winner, nor a [shire] companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);

		// One wound of two.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertInDiscard(flotsam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWinOfAShireCompanionWhoIsNoHobbitIsAnsweredAndAWinOfAGondorHobbitIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(flotsam);
		scn.MoveCompanionsToTable(smeagol, pippin);
		scn.AddWoundsToChar(frodo, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(runner, runner2);
		// Each made 6, to beat a Runner, 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, pippin, null, 3));
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, runner}, new PhysicalCardImpl[]{smeagol, runner2});

		// The Hobbit of [gondor] wins: nothing is offered.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsPlayAvailable(flotsam));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The [shire] companion who is no Hobbit wins: offered, and Frodo, the one wounded Hobbit, is healed.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.FreepsPlayAvailable(flotsam));
		scn.FreepsPlayCard(flotsam);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(flotsam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWinOfAnAllyAndALossOfACompanionAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var ring = scn.GetFreepsCard("ring");
		var runner = scn.GetShadowCard("runner");
		var scrabbler = scn.GetShadowCard("scrabbler");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, ring);
		// Kept out of the deck the turns draw from.
		scn.MoveCardsToDiscard(flotsam);

		scn.StartGame();
		// Thrarin's home is site 3: the fellowship moves there from site 2.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(flotsam);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(flotsam);
		scn.MoveMinionsToTable(runner, scrabbler);
		// The Runner, 5 + 4, beats Aragorn, 8, who lives through the wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, scrabbler}, new PhysicalCardImpl[]{aragorn, runner});

		// Thrarin, 4 + 1 with the Ring, an ally who is no Hobbit, beats the Scrabbler, 4: nothing is offered.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInDiscard(scrabbler);
		assertFalse(scn.FreepsPlayAvailable(flotsam));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsPlayAvailable(flotsam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(flotsam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoHobbitWoundedAWinIsStillAnsweredAndNothingIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(flotsam);
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(smeagol, 1);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);

		// An event is played whatever its effect will do (CR 4.2 "effect").
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.FreepsPlayAvailable(flotsam));
		scn.FreepsPlayCard(flotsam);
		assertInDiscard(flotsam);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
