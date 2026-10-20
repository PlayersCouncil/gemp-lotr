package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_34");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "18_34");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4: he loses a tie twice and lives
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUpToScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "18_34");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4: he loses a tie twice and lives
					// Two more companions, so that with Frodo there are four and room for four threats
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Shelob, Menace
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 8
		 * Site Number: 8
		 * Game Text: <b>Fierce</b>.<br>Each time Shelob wins a skirmish, you may add threats up to the number of burdens you can spot.<br>Each time Shelob wins a skirmish, you may play Gollum from your discard pile; he is strength +3 and <b>fierce</b> until the end of turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Shelob", card.getBlueprint().getTitle());
		assertEquals("Menace", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(8, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "Threats up to the
	// number of burdens you can spot" is no threat with no burden.
	@Test
	public void TheThreatsAreOfferedWhileThereIsABurdenToSpotAndNotWithNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Shelob (8) has beaten Aragorn (8).  No burden; two companions, so a threat could be added.  No Gollum
		// is in the discard pile, so her other offer is not made either.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertEquals(0, scn.GetThreats());

		// A burden, before she wins the fierce skirmish.
		scn.AddBurdens(1);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowAcceptOptionalTrigger();
		// "Up to" the one burden: none or one (F-0419).
		scn.ShadowChoose("1");
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// F-0419 (Ketura, 2026-10-11): "as written, it should offer a choice, 0-N, ideally with a prompt defaulting
	// to N."  "You may add threats up to the number of burdens you can spot" added one for each burden unasked.
	@Test
	public void TheShadowPlayerNamesHowManyThreatsToAddFromNoneToTheBurdensSpottedAndNoMoreThanCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUpToScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("gimli"), scn.GetFreepsCard("guard"));
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Shelob (8) has beaten Aragorn (8).  Three burdens, no threat, four companions.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowAcceptOptionalTrigger();

		// None to three, filled in with three.
		assertTrue(scn.ShadowDecisionAvailable("Choose how many threats to add"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");
		assertEquals(2, scn.GetThreats());

		// She wins the fierce skirmish too.  Still three burdens, and room for only two threats more (four
		// companions, two threats): none to two, filled in with two.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("0");

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ShelobTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());
	}
}
