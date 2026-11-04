package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("master", "11_46");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, strength 3, resistance 6; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, strength 3, resistance 6; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5; his texts are used only on purpose
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,   // Frodo, Son of Drogo: resistance 10
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MasterCommandsItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Master Commands It
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make Sméagol strength +1 for each companion who has resistance 5 or more.
		*/

		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");

		assertEquals("Master Commands It", master.getBlueprint().getTitle());
		assertNull(master.getBlueprint().getSubtitle());
		assertFalse(master.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, master.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, master.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, master.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(master, Timeword.SKIRMISH));
		assertEquals(1, master.getBlueprint().getTwilightCost());
	}

	@Test
	public void InASkirmishSmeagolIsStrengthPlus1ForEachOfFourCompanionsOfResistance5OrMoreForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var master = scn.GetFreepsCard("master");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, aragorn, sam);
		scn.MoveCardsToHand(master);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);

		// No burden: every companion counts, Sam at exactly 5 and Sméagol himself among them.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(sam));

		// The Free Peoples player's assignment actions, the phase before: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(master));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, runner1}, new PhysicalCardImpl[]{aragorn, runner2});

		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(master));
		scn.FreepsPlayCard(master);

		// 3 + 4 companions.
		assertEquals(7, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(master);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in Aragorn's, the next, Sméagol is 3 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(smeagol));
	}

	@Test
	public void WithABurdenTheShireSmeagolIsStrengthPlus3AndACompanionOfResistance4DoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var master = scn.GetFreepsCard("master");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, aragorn, sam);
		scn.MoveCardsToHand(master);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(runner1, runner2);

		// One burden: Frodo 9, Sméagol 5, Aragorn 5 count; Sam, at 4, does not.
		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(smeagol));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));

		scn.SkipToAssignments();
		// Sméagol waits for his own skirmish while Sam's is resolved: the text does not ask him to be in it.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner1}, new PhysicalCardImpl[]{smeagol, runner2});
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(sam));
		scn.FreepsPlayCard(master);

		// 3 + 3 companions.
		assertEquals(6, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));
		assertInDiscard(master);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetFreepsCard("master");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(master);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(master));
		scn.FreepsPlayCard(master);

		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(master);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
