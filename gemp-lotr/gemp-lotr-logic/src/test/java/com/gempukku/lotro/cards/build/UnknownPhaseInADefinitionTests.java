package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * A definition that names a phase the engine does not know is not loaded.
 *
 * The time an effect lasts until ("until") is written "start(<phase>)", "end(<phase>)", a bare "<phase>" (its
 * start), "end(current)", "endofturn", or "endofgame" / "permanent".  Inside the engine a time with no phase is
 * the end of the current phase, so a phase name that was not recognised ("end(turn)", for the end of the turn)
 * used to be accepted and read as "end(current)": the card loaded, and its effect ended a phase or a turn early
 * with nothing to say why.  Such a card is now refused when it is loaded, as a card with any other field the
 * engine cannot read is (the library logs the reason).
 *
 * The same holds for the phases of a DuplicateActionFromPhase modifier ("use special abilities ... as if it were
 * the Fellowship phase").
 *
 * Nothing is played here: each test hands the library a few definitions and reads which of them it kept.  The
 * texts are JSON, which the library reads as it reads HJSON.  No game and no table: plain JUnit, beside the
 * builder that reads a definition.
 */
public class UnknownPhaseInADefinitionTests
{
	// The definition of a Free Peoples condition: "Fellowship: Make a companion strength +1 until <the time>."
	private static String strengthUntil(String id, String until) {
		return condition(id, "\"type\": \"ModifyStrength\", \"select\": \"choose(companion)\", \"amount\": 1,"
				+ " \"until\": \"" + until + "\"");
	}

	// The definition of a Free Peoples condition with one fellowship ability, the fields of whose effect are given.
	private static String condition(String id, String effectFields) {
		return "\"" + id + "\": { \"title\": \"Test Card\", \"unique\": false, \"side\": \"Free Peoples\", \"culture\": \"Gondor\","
				+ " \"twilight\": 0, \"type\": \"Condition\", \"keywords\": \"Support Area\","
				+ " \"effects\": { \"type\": \"activated\", \"phase\": \"fellowship\", \"effect\": { " + effectFields + " } } }";
	}

	// A DuplicateActionFromPhase modifier added until the end of the regroup phase, with the phases given.
	private static String asIfItWere(String id, String oldPhase, String newPhase) {
		return condition(id, "\"type\": \"AddModifier\", \"until\": \"end(regroup)\", \"modifier\": {"
				+ " \"type\": \"DuplicateActionFromPhase\", \"filter\": \"pipe\","
				+ " \"oldPhase\": \"" + oldPhase + "\", \"newPhase\": \"" + newPhase + "\" }");
	}

	@Test
	public void ATimeWrittenAsTheEngineKnowsItIsLoaded() throws Exception {
		final String definitions = "{"
				+ strengthUntil("0_9101", "end(current)") + ","
				+ strengthUntil("0_9102", "end(regroup)") + ","
				+ strengthUntil("0_9103", "start(regroup)") + ","
				+ strengthUntil("0_9104", "regroup") + ","
				+ strengthUntil("0_9105", "endofturn") + ","
				// Case does not matter, in the word or in the name of the phase.
				+ strengthUntil("0_9106", "EndOfTurn") + ","
				+ strengthUntil("0_9107", "Start(Assignment)") + ","
				+ strengthUntil("0_9108", "endofgame") + ","
				+ strengthUntil("0_9109", "permanent")
				+ "}";

		var loaded = LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(),
				new ByteArrayInputStream(definitions.getBytes(StandardCharsets.UTF_8)));

		for (int i = 1; i <= 9; i++)
			assertTrue("0_910" + i, loaded.containsKey("0_910" + i));
	}

	@Test
	public void ATimeThatNamesNoPhaseIsRefusedWhenTheCardIsLoaded() throws Exception {
		final String definitions = "{"
				// The end of the turn is "endofturn": the turn is not a phase.
				+ strengthUntil("0_9111", "end(turn)") + ","
				+ strengthUntil("0_9112", "start(turn)") + ","
				// A phase misspelt.
				+ strengthUntil("0_9113", "end(regrup)") + ","
				+ strengthUntil("0_9114", "start(skirmishes)") + ","
				// Only the end of the current phase can be named without knowing which phase it is.
				+ strengthUntil("0_9115", "start(current)") + ","
				+ strengthUntil("0_9116", "end()") + ","
				// A bare word that is neither a phase nor one of the engine's own.
				+ strengthUntil("0_9117", "turn") + ","
				// One that is right, beside them in the same file, is kept.
				+ strengthUntil("0_9118", "end(skirmish)")
				+ "}";

		var loaded = LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(),
				new ByteArrayInputStream(definitions.getBytes(StandardCharsets.UTF_8)));

		for (int i = 1; i <= 7; i++)
			assertFalse("0_911" + i, loaded.containsKey("0_911" + i));
		assertTrue(loaded.containsKey("0_9118"));
	}

	@Test
	public void AnAsIfItWereAnotherPhaseModifierThatNamesNoPhaseIsRefusedWhenTheCardIsLoaded() throws Exception {
		final String definitions = "{"
				+ asIfItWere("0_9121", "Fellowship", "Regroup") + ","
				// The phase whose abilities are lent.
				+ asIfItWere("0_9122", "Felowship", "Regroup") + ","
				// The phase they are lent to.
				+ asIfItWere("0_9123", "Fellowship", "turn")
				+ "}";

		var loaded = LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(),
				new ByteArrayInputStream(definitions.getBytes(StandardCharsets.UTF_8)));

		assertTrue(loaded.containsKey("0_9121"));
		assertFalse(loaded.containsKey("0_9122"));
		assertFalse(loaded.containsKey("0_9123"));
	}
}
