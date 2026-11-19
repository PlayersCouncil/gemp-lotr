package com.gempukku.lotro.definitions;

import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * The requirement playerIs (whether a player's account name is one of the names a definition lists) must list at
 * least one name: a definition that lists none, or has no list, is not loaded.
 *
 * Kept as a guard of the loader (engine pass 27; Q-0273): no printed card can show a definition being refused, so
 * the three definitions are written here, in the test, and reach no library and no table.  The one printed card
 * with the requirement is Race Text 94_5; what the requirement does in a game is tested, card-agnostic, in
 * mechanics/actions/PlayerIsRequirementTests (Q-0271: C).  Nothing is played here: the library is handed three definitions and is read for
 * which it kept.  The text is JSON, which the library reads as it reads HJSON.  Plain JUnit, in the package for
 * tests of the definition language (Q-0233).
 */
public class PlayerIsRequirementDefinitionTests
{
	@Test
	public void ADefinitionThatListsNoNameIsNotLoaded() throws Exception {
		final String card = "{ \"title\": \"Test Card\", \"unique\": false, \"side\": \"Shadow\", \"culture\": \"Isengard\","
				+ " \"twilight\": 0, \"type\": \"Condition\", \"keywords\": \"Support Area\","
				+ " \"effects\": { \"type\": \"activated\", \"phase\": \"shadow\","
				+ " \"requires\": { \"type\": \"playerIs\" NAMES },"
				+ " \"effect\": { \"type\": \"addTwilight\", \"amount\": 1 } } }";
		final String definitions = "{"
				+ "\"0_9201\": " + card.replace("NAMES", ", \"names\": [ \"Warden of the Test Table\" ]") + ","
				+ "\"0_9202\": " + card.replace("NAMES", ", \"names\": [ ]") + ","
				+ "\"0_9203\": " + card.replace("NAMES", "")
				+ "}";

		var loaded = LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(),
				new ByteArrayInputStream(definitions.getBytes(StandardCharsets.UTF_8)));

		assertTrue(loaded.containsKey("0_9201"));
		assertFalse(loaded.containsKey("0_9202"));
		assertFalse(loaded.containsKey("0_9203"));
	}
}
