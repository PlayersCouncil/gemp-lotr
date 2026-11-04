package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
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
					put("shelob", "18_34");
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

	protected VirtualTableScenario GetGollumScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "18_34");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4: he loses a tie twice and lives
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum], cost 2, strength 5; a regroup ability used only on purpose
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith], cost 2, strength 5; texts answer a Nazgul played and an assignment ability
					put("villain", "6_40");         // Gollum, Old Villain: the Free Peoples player's copy lies in his own discard pile
					put("runner", "1_178");         // Goblin Runner: [moria] minion of cost 1, payable, who is no Gollum
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

		var card = scn.GetFreepsCard("shelob");

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
		assertEquals(1, scn.GetBurdens());
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
		assertEquals(3, scn.GetBurdens());
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

	@Test
	public void WhenShelobLosesASkirmishNeitherOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGollumScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToDiscard(gollum);
		assertInDiscard(gollum);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(shelob);
		// Aragorn 8 + 3 beats Shelob (8) and does not overwhelm her.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		scn.SkipToAssignments();
		// Gollum costs 2 + 2 for roaming at site 2.
		scn.SetTwilight(4);
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// She lost, with burdens to spot and a Gollum she could pay for in her discard pile.
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.GetTwilight() >= 4);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertEquals(0, scn.GetThreats());
		assertInDiscard(gollum);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WinningSheMayPlayAGollumFromHerDiscardPileWhoIsStrengthPlusThreeAndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGollumScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var villain = scn.GetFreepsCard("villain");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(villain);
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(gollum, wraithgollum, runner);
		assertInDiscard(gollum, wraithgollum, runner, villain);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		// Gollum costs 2 + 2 for roaming at site 2.
		scn.SetTwilight(4);
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Shelob (8) has beaten Aragorn (8).  No burden, so only the Gollum offer is made.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Play Gollum"));
		scn.ShadowAcceptOptionalTrigger();

		// A Gollum of each culture; a minion who is no Gollum, and the Free Peoples player's own Gollum in his own
		// discard pile, a version her piles do not hold, are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, wraithgollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, villain));
		scn.ShadowChooseCard(gollum);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInDiscard(wraithgollum, runner, villain);
		// 5 + 3, and fierce; Shelob beside him is left as she was.
		assertEquals(8, scn.GetStrength(gollum));
		assertTrue(scn.HasKeyword(gollum, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DeclinedTheGollumStaysInTheDiscardPileAndIsOfferedAgainAtHerFierceWinUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGollumScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		scn.MoveCardsToDiscard(gollum, wraithgollum);
		assertInDiscard(gollum, wraithgollum);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		// Gollum costs 2 + 2 for roaming at site 2.
		scn.SetTwilight(4);
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Play Gollum"));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(gollum, wraithgollum);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// She wins the fierce skirmish too, and the offer is made again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		// Gollum costs 2 + 2 for roaming at site 2.
		scn.SetTwilight(4);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Play Gollum"));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(wraithgollum);

		assertInZone(Zone.SHADOW_CHARACTERS, wraithgollum);
		assertEquals(8, scn.GetStrength(wraithgollum));
		assertTrue(scn.HasKeyword(wraithgollum, Keyword.FIERCE));

		// Still so in the regroup phase: the minions are discarded before the turn ends.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(wraithgollum));
		assertTrue(scn.HasKeyword(wraithgollum, Keyword.FIERCE));
	}

	@Test
	public void WithTheTwilightShortOfGollumsCostTheGollumOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGollumScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToDiscard(gollum);
		assertInDiscard(gollum);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		// One short of Gollum's 2 + 2 for roaming at site 2.
		scn.SetTwilight(3);
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertInDiscard(gollum);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DeclinedNoThreatIsAddedAndAtTheFierceWinOneBurdenAllowsUpToOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Add threats"));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The fierce win: up to the one burden, filled in with one.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Add threats"));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(1), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BothOffersAtOneWinCanEachBeTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGollumScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToDiscard(gollum);
		assertInDiscard(gollum);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		// Gollum costs 2 + 2 for roaming at site 2.
		scn.SetTwilight(4);
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Add threats"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Play Gollum"));
		scn.ShadowChooseOptionalTrigger(shelob, "Play Gollum");
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		// Each text is a trigger of its own: the threat offer is still open.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob, "Add threats"));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("1");
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
