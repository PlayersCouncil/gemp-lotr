package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The requirement playerIs: whether a player's account name is one of the names a definition lists.
 *
 * No rule of the game knows a player by name.  It is how a card that speaks of a person is built: Race Text 94_5,
 * "If your opponent is the Dark Lord or a World Champion", lists the account of each.  Who holds such a title
 * changes, so that card's own tests leave the names alone and the check is tested here, with names that are
 * nobody's (Q-0190).  A real account name is letters, digits, "-" and "_", so a name with a space in it can be no
 * account.
 *
 * The check: the definition names a player ("player", the one using the card when it is left out: "you") and a
 * list of names; it is met when that player's account name is one of the list, whatever the capitals of either.
 * With the player "any" it is met when either player at the table has a listed name.
 *
 * Shown with cards of the tests' own, each a Shadow condition with one Shadow-phase ability that the requirement
 * is on: Test Roster 0_8024, "If you are the Warden of the Test Table or the Reeve of the Test Table, add (1)";
 * Test Summons 0_8025, "If the Free Peoples player is the Warden of the Test Table, add (1)"; and Test Census
 * 0_8026, "If any player is the Warden of the Test Table, add (1)".  They are Shadow cards so that the player
 * using one is not the Free Peoples player.  The table seats a player under a chosen name by the last two
 * arguments of its constructor (G-0039); null is the usual name, "Free Peoples Player" or "Shadow Player".
 *
 * That a definition which lists no name is not loaded is the builder's to show, with no game played:
 * PlayerIsRequirementDefinitionTests, beside the builder in the logic module.
 */
public class PlayerIsRequirementTests
{
	protected VirtualTableScenario GetScenario(String freepsPlayerName, String shadowPlayerName) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Roster (the tests' own card): "Shadow: If you are the Warden of the Test Table or the
					// Reeve of the Test Table, add (1)."
					put("roster", "0_8024");
					// Test Summons (the tests' own card): "Shadow: If the Free Peoples player is the Warden of the
					// Test Table, add (1)."
					put("summons", "0_8025");
					// Test Census (the tests' own card): "Shadow: If any player is the Warden of the Test Table,
					// add (1)."
					put("census", "0_8026");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				null, null, null, null,
				freepsPlayerName, shadowPlayerName
		);
	}

	@Test
	public void APlayerSeatedUnderAListedNameMeetsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario(null, "Warden of the Test Table");

		var roster = scn.GetShadowCard("roster");
		scn.MoveCardsToSupportArea(roster);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("Warden of the Test Table", scn.P2());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(roster));

		int twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(roster);

		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnyNameOfTheListWillDoAndEachCardHasItsOwnList() throws DecisionResultInvalidException, CardNotFoundException {
		// The Reeve is the second name of Test Roster's list, and is not in Test Census's.
		var scn = GetScenario(null, "Reeve of the Test Table");

		var roster = scn.GetShadowCard("roster");
		var census = scn.GetShadowCard("census");
		scn.MoveCardsToSupportArea(roster, census);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(roster));
		assertFalse(scn.ShadowActionAvailable(census));
	}

	@Test
	public void ANameIsMatchedWhateverItsCapitals() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario(null, "WARDEN of the test TABLE");

		var roster = scn.GetShadowCard("roster");
		scn.MoveCardsToSupportArea(roster);

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("WARDEN of the test TABLE", scn.P2());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(roster));
	}

	@Test
	public void ANameThatOnlyBeginsWithAListedNameDoesNotMeetIt() throws DecisionResultInvalidException, CardNotFoundException {
		// One letter more than the Warden's name.
		var scn = GetScenario(null, "Warden of the Test Tables");

		var roster = scn.GetShadowCard("roster");
		var census = scn.GetShadowCard("census");
		scn.MoveCardsToSupportArea(roster, census);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(roster));
		assertFalse(scn.ShadowActionAvailable(census));
	}

	@Test
	public void WithNoListedNameAtTheTableItIsMetForNobody() throws DecisionResultInvalidException, CardNotFoundException {
		// The usual players: "Free Peoples Player" and "Shadow Player".
		var scn = GetScenario(null, null);

		var roster = scn.GetShadowCard("roster");
		var summons = scn.GetShadowCard("summons");
		var census = scn.GetShadowCard("census");
		scn.MoveCardsToSupportArea(roster, summons, census);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(roster));
		assertFalse(scn.ShadowActionAvailable(summons));
		assertFalse(scn.ShadowActionAvailable(census));
	}

	@Test
	public void AskedOfYouItIsAskedOfThePlayerUsingTheCard() throws DecisionResultInvalidException, CardNotFoundException {
		// Player 1 is the Warden.  Each player has a Test Roster of his own in his support area.
		var scn = GetScenario("Warden of the Test Table", null);

		var wardensRoster = scn.GetFreepsCard("roster");
		var othersRoster = scn.GetShadowCard("roster");
		scn.MoveCardsToSupportArea(wardensRoster, othersRoster);

		scn.StartGame();
		scn.FreepsPass();

		// The first turn: player 2 is the Shadow player and is not the Warden, though the Warden sits across
		// from him.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(othersRoster));

		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();

		// The second turn: the Warden is the Shadow player.
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertTrue(scn.FreepsActionAvailable(wardensRoster));
	}

	@Test
	public void AskedOfTheFreePeoplesPlayerItIsAskedOfThePlayerWhoseTurnItIs() throws DecisionResultInvalidException, CardNotFoundException {
		// Player 1 is the Warden.  Each player has a Test Summons of his own in his support area.
		var scn = GetScenario("Warden of the Test Table", null);

		var wardensSummons = scn.GetFreepsCard("summons");
		var othersSummons = scn.GetShadowCard("summons");
		scn.MoveCardsToSupportArea(wardensSummons, othersSummons);

		scn.StartGame();
		scn.FreepsPass();

		// The first turn: the Warden is the Free Peoples player, and it is the other player who uses the card.
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(othersSummons));

		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();

		// The second turn: the Free Peoples player is the other player, and the Warden's own copy is not met.
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertFalse(scn.FreepsActionAvailable(wardensSummons));
	}

	@Test
	public void AskedOfAnyPlayerItIsMetWithTheNameInEitherSeat() throws DecisionResultInvalidException, CardNotFoundException {
		// The Warden across the table from the player using the card.
		var scn = GetScenario("Warden of the Test Table", null);

		var census = scn.GetShadowCard("census");
		var roster = scn.GetShadowCard("roster");
		scn.MoveCardsToSupportArea(census, roster);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(census));
		// Asked of "you" on the same board, it is not met.
		assertFalse(scn.ShadowActionAvailable(roster));

		// The Warden himself using the card.
		scn = GetScenario(null, "Warden of the Test Table");

		census = scn.GetShadowCard("census");
		scn.MoveCardsToSupportArea(census);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(census));
	}
}
