package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.hjson.JsonValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The requirement playerIs: whether a player's account name is one of the names a definition lists.
 *
 * No rule of the game knows a player by name.  It is how a card that speaks of a person is built: Race Text 94_5,
 * "If your opponent is the Dark Lord or a World Champion ...", lists the account of each, asked of this turn's Free
 * Peoples player ({@code player: freeps}).  Who holds such a title changes, so that card's own tests leave the names
 * alone (Q-0190, Ketura, 2026-10-09: "What's more important is that the player-name-checking code has card-agnostic
 * tests somewhere. Given that these will change over time if past world champions reveal their account names (or
 * if someone *finally* beats basmelis so he doesn't win a fourth time in a row), let's do B, but with the mechanic
 * test coverage.").
 *
 * THE ONE EXCEPTION to "no definition text made up for a test" (Ketura, 2026-10-11, Q-0241: no card is invented
 * for a test, and no definition text is stapled onto a card to stand in for one).  The requirement is built here,
 * in the test, from definition text written here, and asked directly: no card, no id, nothing loaded into a
 * library, nothing played.  Q-0271 asked what was to stand in place of the class of this name that engine pass 27
 * deleted with its invented cards, and offered as C "one card-agnostic test of the check with made-up names and
 * no card at all: the requirement is built in the test from its definition text and asked directly, at a table
 * seated under made-up names"; Ketura, 2026-10-12: "Yeah do C".  The exception is for this check alone, on the
 * strength of Q-0190, and is no precedent for any other.
 *
 * The text is 94_5's shape with a name that is nobody's: a real account name is letters, digits, "-" and "_", so a
 * name with a space in it can be no account.  The table seats a player under a chosen name by the last two
 * arguments of its constructor (G-0039; virtualtable/NamedSeatTests); null is the usual name, "Free Peoples
 * Player" or "Shadow Player".  The player asking is the Shadow player of the moment, whose card 94_5 is when it
 * applies, so that "the Free Peoples player" is never read as "you".
 *
 * Untested, as Q-0271 said: {@code player: any}, and a playerIs with no {@code player} ("you"); no printed card
 * writes either (Q-0274 kept them in the engine).  That a definition which lists no name is not loaded is the
 * builder's to show: definitions/PlayerIsRequirementDefinitionTests.
 */
public class PlayerIsRequirementTests
{
	// No card: the table is for its seats and its turns.
	protected VirtualTableScenario GetScenario(String freepsPlayerName, String shadowPlayerName) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				null, null, null, null,
				freepsPlayerName, shadowPlayerName
		);
	}

	@Test
	public void AListedNameIsMetWhateverItsCapitalsALongerNameIsNotAndTheFreePeoplesPlayerIsWhoeverTakesTheTurn() throws Exception {
		// HJSON, as a definition file writes it: an unquoted value runs to the end of its line.
		final String text = """
				{
					type: PlayerIs
					player: freeps
					names: [
						Warden of the Test Table
					]
				}
				""";
		final JSONObject definition = (JSONObject) new JSONParser().parse(JsonValue.readHjson(text).toString());
		final var builder = new LotroCardBlueprintBuilder();
		final Requirement warden = builder.getRequirementFactory().getRequirement(definition, builder);

		// A listed name is met.
		var scn = GetScenario("Warden of the Test Table", null);
		scn.StartGame();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(warden.accepts(new DefaultActionContext(scn.P2(), scn.game(), null, null, null))); // lint-ok: the requirement is asked directly (Q-0271: C)

		// Whatever its capitals.
		scn = GetScenario("wARDEN OF THE TEST TABLE", null);
		scn.StartGame();
		assertTrue(warden.accepts(new DefaultActionContext(scn.P2(), scn.game(), null, null, null))); // lint-ok: the requirement is asked directly (Q-0271: C)

		// A name that only begins with the listed one is not met, though the listed name sits in the other seat
		// and is the one asking.
		scn = GetScenario("Warden of the Test Tables", "Warden of the Test Table");
		scn.StartGame();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertFalse(warden.accepts(new DefaultActionContext(scn.P2(), scn.game(), null, null, null))); // lint-ok: the requirement is asked directly (Q-0271: C)

		// The Free Peoples player is whoever takes the turn: on the second player's, the Warden is he, and the player
		// asking is the first.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(warden.accepts(new DefaultActionContext(scn.P1(), scn.game(), null, null, null))); // lint-ok: the requirement is asked directly (Q-0271: C)
	}
}
