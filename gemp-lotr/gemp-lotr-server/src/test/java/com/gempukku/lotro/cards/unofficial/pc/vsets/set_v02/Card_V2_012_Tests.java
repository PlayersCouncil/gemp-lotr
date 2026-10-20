package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_012_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("friends", "102_12");

					// Two companions of each of the three races.  The first of each pair is strong enough to
					// beat a Goblin Runner; the second has no text once in play.
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: Man, strength 8; a maneuver ability
					put("guard", "5_83");        // Household Guard: Man, strength 6; "Valiant" is its whole text
					put("arwen", "1_30");        // Arwen, Daughter of Elrond: Elf, strength 6; stronger against a Nazgul
					put("lorienelf", "1_53");    // Lorien Elf: Elf, strength 4
					put("gimli", "1_13");        // Gimli, Son of Gloin: Dwarf, strength 6, damage +1; a skirmish ability
					put("dwarfguard", "1_7");    // Dwarf Guard: Dwarf, strength 4
					// A companion who is none of the three: a Wizard, strength 7, with a fellowship ability.
					put("gandalf", "1_364");     // Gandalf, The Grey Wizard

					// An ally of each race, all at home at site 3.
					put("erland", "2_21");       // Erland, Advisor to Brand: Man, strength 5; hides his owner's hand
					put("grimir", "1_17");       // Grimir, Dwarven Elder: Dwarf, strength 3; a fellowship ability
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: Elf, strength 8; heals site 3 allies each turn

					// Goblin Runner: strength 5, vitality 1.  Anyone of strength 6 or more beats it.
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					// Dunlending Brigand: a Man who is a minion, strength 8, no text.
					put("brigand", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YourFriendsAreWithYouStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Your Friends Are With You
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time your Man, Elf, or Dwarf wins a skirmish, make your Man, Elf, or Dwarf of another
		 * race strength +1 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("friends");

		assertEquals("Your Friends Are With You", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAnElfWinsASkirmishAManOrADwarfOfMineCompanionOrAllyIsMadeStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var friends = scn.GetFreepsCard("friends");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var erland = scn.GetFreepsCard("erland");
		var grimir = scn.GetFreepsCard("grimir");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(arwen, lorienelf, aragorn, gimli, gandalf);
		scn.MoveAlliesToTable(erland, grimir, elrond);

		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2, brigand);
		scn.SkipToAssignments();

		// Gandalf's skirmish is left for later, so that the regroup phase does not follow Arwen's at once.
		// The Brigand, a Man the opponent owns, stands by and fights nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { arwen, runner },
				new PhysicalCardImpl[] { gandalf, runner2 }
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(erland));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(brigand));

		// Arwen's 6 beats the Runner's 5.
		scn.PassSkirmishActions();
		// The condition answers the win before the Runner's wound, with nothing to order (engine pass 54).
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		// An Elf won, so it is a Man or a Dwarf of mine: companions and allies alike.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, erland, grimir));
		// Not the Elf who won, not another Elf, companion or ally; not a Wizard or a Hobbit; and not the
		// opponent's Man.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, lorienelf, elrond, gandalf, frodo, brigand));
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(erland);
		assertEquals(6, scn.GetStrength(erland));

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(brigand));

		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WhenAManWinsASkirmishAnElfOrADwarfIsChosenAndNotAnotherMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(aragorn, guard, arwen, lorienelf, gimli, dwarfguard);

		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		// A second skirmish is left for later, so that the regroup phase does not follow the first at once.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, runner },
				new PhysicalCardImpl[] { gimli, runner2 }
		);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(4, scn.GetStrength(lorienelf));

		// Aragorn's 8 beats the Runner's 5.
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).

		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, lorienelf, gimli, dwarfguard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guard));
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(lorienelf);
		assertEquals(5, scn.GetStrength(lorienelf));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(dwarfguard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(guard));

		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WhenADwarfWinsASkirmishAManOrAnElfIsChosenAndNotAnotherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(aragorn, guard, arwen, lorienelf, gimli, dwarfguard);

		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		// A second skirmish is left for later, so that the regroup phase does not follow the first at once.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { gimli, runner },
				new PhysicalCardImpl[] { aragorn, runner2 }
		);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(guard));

		// Gimli's 6 beats the Runner's 5.
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard, arwen, lorienelf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, dwarfguard));
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(guard);
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(dwarfguard));

		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AnAllyWhoWinsASkirmishAtHisHomeSiteSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(arwen, aragorn, gimli);
		scn.MoveAlliesToTable(elrond);

		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		// Elrond's home is site 3, and there he may be assigned to a skirmish.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner, runner2);
		scn.FreepsPass();
		scn.SkipToAssignments();

		// Aragorn's skirmish is left for later, so that the regroup phase does not follow Elrond's at once.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { elrond, runner },
				new PhysicalCardImpl[] { aragorn, runner2 }
		);
		scn.FreepsResolveSkirmish(elrond);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		// Elrond's 8 beats the Runner's 5.
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).

		// An Elf has won, though not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, arwen));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));

		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NothingHappensWhenAWizardWinsOrWhenAnElfLosesToTheOpponentsMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var gandalf = scn.GetFreepsCard("gandalf");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(gandalf, arwen, aragorn, gimli);

		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2, brigand);
		scn.SkipToAssignments();

		// Aragorn's skirmish is never fought: it keeps the regroup phase, which would end any bonus, away.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { gandalf, runner },
				new PhysicalCardImpl[] { arwen, brigand },
				new PhysicalCardImpl[] { aragorn, runner2 }
		);

		// Gandalf's 7 beats the Runner's 5: a companion of mine has won, but a Wizard.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		// Nothing is asked about the condition: the game has gone on to the next skirmish.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));

		// Arwen's 6 loses to the Brigand's 8: a Man has won a skirmish, but he is not mine.
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);

		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(brigand));
	}

	@Test
	public void EachSkirmishWonAddsAnotherPlus1AndAllOfItLastsThroughLaterSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(friends);
		scn.MoveCompanionsToTable(arwen, gimli, aragorn);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { arwen, runner1 },
				new PhysicalCardImpl[] { gimli, runner2 },
				new PhysicalCardImpl[] { aragorn, runner3 }
		);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));

		// The Elf wins, and Aragorn is chosen.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(9, scn.GetStrength(aragorn));

		// The Dwarf wins, and Aragorn is chosen again.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));

		// Two skirmish phases have ended, and in the third Aragorn fights with both: 8 + 1 + 1.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));

		// The Man wins in his turn, and this time Gimli is chosen.
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, gimli));
		scn.FreepsChooseCard(gimli);

		// That was the last skirmish, and the regroup phase ends all three.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));
	}
}
